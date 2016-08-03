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

package com.flipkart.flux.impl.task;

/**
 * <code>SerialisedEventData</code> acts as DTO to transfer the data from LocalJVMTask to TaskExecutor.
 * @author shyam.akirala
 */
public class SerializedEvent {

    /** Type of the event */
    private String eventType;

    /** Serialized data of th event */
    private String serializedEventData;

    public SerializedEvent(String eventType, String serializedEventData) {
        this.eventType = eventType;
        this.serializedEventData = serializedEventData;
    }

    public String getEventType() {
        return eventType;
    }

    public String getSerializedEventData() {
        return serializedEventData;
    }
}
