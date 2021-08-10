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

package com.flipkart.flux.eventscheduler.service;

import static com.flipkart.flux.Constants.METRIC_REGISTRY_NAME;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.codahale.metrics.InstrumentedScheduledExecutorService;
import com.codahale.metrics.SharedMetricRegistries;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.api.EventData;
import com.flipkart.flux.eventscheduler.dao.EventSchedulerDao;
import com.flipkart.flux.eventscheduler.model.ScheduledEvent;
import com.flipkart.flux.task.eventscheduler.EventSchedulerRegistry;
import com.google.inject.Singleton;

/**
 * <code>EventSchedulerService</code> reads ScheduledEvents from DB with a fixed delay, and triggers them if necessary. The event would be triggered
 * if the current time greater than or equal to the scheduledTime of the event.
 *
 * @author shyam.akirala
 */
@Singleton
public class EventSchedulerService {

    private static final Logger logger = LogManager.getLogger(EventSchedulerService.class);
    private static final String scheduledExectorSvcName = "event-scheduler-batch-read-executor-svc";

    private Integer batchReadInterval;
    private Integer batchSize;
    private Long initialDelay = 10000L;
    private ScheduledFuture scheduledFuture;
    private EventSchedulerDao eventSchedulerDao;
    private final EventSchedulerRegistry eventSchedulerRegistry;
    private final InstrumentedScheduledExecutorService scheduledExecutorService;
    private ObjectMapper objectMapper;

    @Inject
    public EventSchedulerService(EventSchedulerDao eventSchedulerDao, EventSchedulerRegistry eventSchedulerRegistry,
                                 @Named("eventScheduler.batchRead.intervalms") Integer batchReadInterval,
                                 @Named("eventScheduler.batchRead.batchSize") Integer batchSize,
                                 ObjectMapper objectMapper) {
        this.eventSchedulerDao = eventSchedulerDao;
        this.eventSchedulerRegistry = eventSchedulerRegistry;
        this.batchReadInterval = batchReadInterval;
        this.batchSize = batchSize;
        this.objectMapper = objectMapper;

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
                        triggerEvents();
                    } catch (Exception e) {
                        logger.error("Error while running triggerEvents", e);
                    }
                }, initialDelay, batchReadInterval, TimeUnit.MILLISECONDS);
                logger.info("EventSchedulerService started");
            }
        }
    }

    public void stop() {
        synchronized (this) {
            scheduledFuture.cancel(false);
            logger.info("EventSchedulerService stopped");
        }
    }

    public Boolean isRunning() {
        return (scheduledFuture != null && !scheduledFuture.isDone());
    }

    private void triggerEvents() {
        List<ScheduledEvent> events;
        //time in seconds
        long now = System.currentTimeMillis() / 1000;
        do {
            events = eventSchedulerDao.retrieveOldest(batchSize);
            events.stream().filter(e -> e.getScheduledTime() <= now).forEach(e -> {
                try {
                    EventData eventData = objectMapper.readValue(e.getEventData(), EventData.class);
                    eventSchedulerRegistry.triggerEvent(e.getEventName(), eventData.getData(), e.getCorrelationId(), eventData.getEventSource());
                    eventSchedulerRegistry.deregisterEvent(e.getCorrelationId(), e.getEventName());
                } catch (Exception ex) {
                }
            });
            // get next batch if we found batchSize events and all were triggered
        } while (events.size() == batchSize && events.get(events.size() - 1).getScheduledTime() <= now);
    }

    public void setInitialDelay(Long initialDelay) {
        this.initialDelay = initialDelay;
    }
}
