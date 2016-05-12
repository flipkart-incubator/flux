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

package com.flipkart.flux.api;

/**
 * <code>EventData</code> represents the event which would be submitted to flux runtime from Akka system.
 * This is useful for data transfer purpose only.
 * @author shyam.akirala
 */
public class EventData {

    /** Name of the event */
    private String name;

    /** Type of the event */
    private String type;

    /** Data the event is carrying */
    private Object data;

    /** Source who generated this event, might be state name or external */
    private String eventSource;

    /** Used by jackson */
    EventData() {}

    /** constructor */
    public EventData(String name, String type, Object data, String eventSource) {
        this.name = name;
        this.type = type;
        this.data = data;
        this.eventSource = eventSource;
    }

    /** Accessor/Mutator methods*/
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public Object getData() {
        return data;
    }
    public void setData(Object data) {
        this.data = data;
    }
    public String getEventSource() {
        return eventSource;
    }
    public void setEventSource(String eventSource) {
        this.eventSource = eventSource;
    }

}
