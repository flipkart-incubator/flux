/*
 * Copyright 2012-2016, the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flipkart.flux.redriver.service;

import com.codahale.metrics.InstrumentedScheduledExecutorService;
import com.codahale.metrics.SharedMetricRegistries;
import com.flipkart.flux.redriver.model.ScheduledMessage;
import com.flipkart.flux.task.redriver.RedriverRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.flipkart.flux.Constants.METRIC_REGISTRY_NAME;

/**
 * The service uses a scheduler to read the oldest message with fixed delay and redrives them if necessary. The task will
 * be scheduled for execution if the current time is greater than or equal to scheduledTime.
 *
 * @author gaurav.ashok
 */
@Singleton
public class RedriverService {

    private static final Logger logger = LoggerFactory.getLogger(RedriverService.class);
    private static final String scheduledExectorSvcName = "redriver-batch-read-executor-svc";

    private Integer batchReadInterval;
    private Integer batchSize;
    private Long initialDelay = 10000L;
    private MessageManagerService messageService;
    private ScheduledFuture scheduledFuture;
    private final RedriverRegistry redriverRegistry;
    private final InstrumentedScheduledExecutorService scheduledExecutorService;

    @Inject
    public RedriverService(MessageManagerService messageService,
                           RedriverRegistry redriverRegistry,
                           @Named("redriver.batchread.intervalms") Integer batchReadInterval,
                           @Named("redriver.batchread.batchsize") Integer batchSize) {
        this.redriverRegistry = redriverRegistry;
        this.batchReadInterval = batchReadInterval;
        this.batchSize = batchSize;
        this.messageService = messageService;

        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        // remove the task from scheduler on cancel
        executor.setRemoveOnCancelPolicy(true);
        scheduledExecutorService =
                new InstrumentedScheduledExecutorService(Executors.unconfigurableScheduledExecutorService(executor),
                        SharedMetricRegistries.getOrCreate(METRIC_REGISTRY_NAME), scheduledExectorSvcName);
    }

    public void start() {

        synchronized (this) {
            if (scheduledFuture == null || scheduledFuture.isDone()) {
                scheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
                    try {
                        redrive();
                    }
                    catch(Exception e) {
                        logger.error("Error while running redrive", e);
                    }
                }, initialDelay, batchReadInterval, TimeUnit.MILLISECONDS);
                logger.info("RedriverService started");
            }
        }
    }

    public void stop() {
        synchronized (this) {
            scheduledFuture.cancel(false);
            logger.info("RedriverService stopped");
        }
    }

    public Boolean isRunning() {
        return scheduledFuture != null && (!scheduledFuture.isDone());
    }

    private void redrive() {
        int offset = 0;
        List<ScheduledMessage> messages;
        long now = System.currentTimeMillis();
        do {
            messages = messageService.retrieveOldest(offset, batchSize);
            messages.stream().filter(e -> e.getScheduledTime() < now).forEach(e -> {
                try {
                    redriverRegistry.redriveTask(e.getTaskId());
                } catch (Exception ex) {}
            });
            offset += batchSize;
            // get next batch if we found batchSize tasks and all were redriven
        } while (messages.size() == batchSize && messages.get(messages.size() - 1).getScheduledTime() < now);
    }

    public void setInitialDelay(Long initialDelay) {
        this.initialDelay = initialDelay;
    }
}
