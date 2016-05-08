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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;


/**
 * The <code>EventsInfo</code> class is a DTO for representing Events
 *
 * @author ashish.bhutani
 *
 */
public class EventsInfo {

    @JsonProperty
    /***
     * @eventsData Key here is the event id.
     */
    private Map<String, EventData> eventsData;

    /* Used for deserialisation by jackson */
    EventsInfo() {
    }

    public EventsInfo(Map<String, EventData> eventsData) {
        this.eventsData = eventsData;
    }

    /* Getter/Setters */
    public Map<String, EventData> getEventsData() {
        return eventsData;
    }

    public static class EventData {
        @JsonProperty
        private String status;
        @JsonProperty
        private String data;

        /* Used for deserialisation by jackson */
        EventData() {
        }

        public EventData(String data, String status) {
            this.data = data;
            this.status = status;
        }

        public String getData() {
            return data;
        }

        public String getStatus() {
            return status;
        }
    }



}
