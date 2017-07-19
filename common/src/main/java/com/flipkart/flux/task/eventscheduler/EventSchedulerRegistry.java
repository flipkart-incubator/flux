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

package com.flipkart.flux.task.eventscheduler;

/**
 * <code>EventSchedulerRegistry</code> defines behaviour of Event Scheduler that is used to schedule an event processing at a future time.
 *
 * @author shyam.akirala
 */
public interface EventSchedulerRegistry {

    /**
     * Registers an event with EventScheduler, so that the event would be triggered at mentioned future time
     * @param correlationId correlationId of a state machine
     * @param eventName name of manual event that needs to be triggered
     * @param eventData String representation of com.flipkart.flux.api.EventData object
     * @param scheduledTime time in epoch format at which the event needs to be triggered
     */
    void registerEvent(String correlationId, String eventName, String eventData, Long scheduledTime);

    /**
     * Deletes scheduled event from DB, usually called once event is successfully triggered
     * @param correlationId correlationId of a state machine
     * @param eventName name of manual event that has been triggered
     */
    void deregisterEvent(String correlationId, String eventName);

    /**
     * Submits an event for processing
     * @param eventName name of manual event that needs to be triggered
     * @param data data the event is carrying
     * @param correlationId correlationId of a state machine
     * @param eventSource source of the event, supplied from client
     */
    void triggerEvent(String eventName, Object data, String correlationId, String eventSource);
}
