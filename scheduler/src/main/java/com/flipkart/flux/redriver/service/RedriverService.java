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

import static com.flipkart.flux.Constants.METRIC_REGISTRY_NAME;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.codahale.metrics.InstrumentedScheduledExecutorService;
import com.codahale.metrics.SharedMetricRegistries;
import com.flipkart.flux.redriver.model.ScheduledMessage;
import com.flipkart.flux.task.redriver.RedriverRegistry;

/**
 * The service uses a scheduler to read the oldest message with fixed delay and redrives them if necessary. The task will
 * be scheduled for execution if the current time is greater than or equal to scheduledTime.
 *
 * @author gaurav.ashok
 */
@Singleton
public class RedriverService {

    private static final Logger logger = LogManager.getLogger(RedriverService.class);
    private static final String scheduledExectorSvcName = "redriver-batch-read-executor-svc";

    private Integer batchReadInterval;
    private Integer batchSize;
    private Long initialDelay = 10000L;
    private MessageManagerService messageService;
    @SuppressWarnings("rawtypes")
	private ScheduledFuture scheduledFuture;
    private final RedriverRegistry redriverRegistry;


    private final InstrumentedScheduledExecutorService scheduledExecutorService;
    private ExecutorService asyncRedriveService;


    @Inject
    public RedriverService(MessageManagerService messageService,
                           RedriverRegistry redriverRegistry,
                           @Named("redriver.batchRead.intervalms") Integer batchReadInterval,
                           @Named("redriver.batchRead.batchSize") Integer batchSize) {
        this.redriverRegistry = redriverRegistry;
        this.batchReadInterval = batchReadInterval;
        this.batchSize = batchSize;
        this.messageService = messageService;
        asyncRedriveService = Executors.newFixedThreadPool(10);

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
                    } catch (Throwable e) {
                        logger.error("Error while running redrive {}", e);
                    }
                }, initialDelay, batchReadInterval, TimeUnit.MILLISECONDS);
                logger.info("RedriverService started");
            }
        }
    }

    public void stop() {
        synchronized (this) {
            scheduledFuture.cancel(false);
            try {
                asyncRedriveService.awaitTermination(10000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                logger.error("Forcefully terminated redrive jobs {}", e);
            }
            //       asyncRedriveService.shutdown();
            logger.info("RedriverService stopped");
        }
    }

    public Boolean isRunning() {
        return scheduledFuture != null && (!scheduledFuture.isDone());
    }

    private void redrive() {
        int offset = 0;
        List<ScheduledMessage> messages;
        ArrayList<Future<?>> tasksRedrived = new ArrayList<>();
        do {
            messages = messageService.retrieveOldest(offset, batchSize);
            logger.info("Retrieved {} messages to redrive", messages.size());

            messages.forEach(e -> {
                tasksRedrived.add(
                        asyncRedriveService.submit(() -> {
                            try {
                                redriverRegistry.redriveTask(e.getStateMachineId(), e.getTaskId() , e.getExecutionVersion());
                            } catch (Exception ex) {
                                logger.error("Something went wrong in redriving task:{} smId:{} with execution Version:{}, Error: {}", e.getTaskId(),
                                        e.getStateMachineId(), e.getExecutionVersion(), ex.getStackTrace());
                            }
                        }));
            });
            offset += batchSize;
            boolean allCompleted = false;
            while (!allCompleted) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    logger.warn("Error while sleeping before checking async redrive callbacks {}", e);
                }
                allCompleted = true;
                for (int i = 0; i < tasksRedrived.size(); i++)
                    if (tasksRedrived.get(i).isDone() || tasksRedrived.get(i).isCancelled())
                        continue;
                    else {
                        allCompleted = false;
                        break;
                    }
            }
            tasksRedrived.clear();
            // get next batch if we found batchSize tasks and all were redriven
        } while (messages.size() > 0);
    }

    public void setInitialDelay(Long initialDelay) {
        this.initialDelay = initialDelay;
    }
}