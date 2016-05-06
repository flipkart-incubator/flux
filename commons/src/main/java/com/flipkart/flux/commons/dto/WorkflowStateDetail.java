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

package com.flipkart.flux.commons.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * The <code>WorkflowStateDetail</code> class is a DTO for representing Workflow state
 *
 * @author ashish.bhutani
 *
 */

public class WorkflowStateDetail {

    private Map<String, StateDetail> statesDetail;

    /* Used for deserialisation by jackson */
    WorkflowStateDetail() {
    }

    public WorkflowStateDetail(Map<String, StateDetail> statesDetail) {
        this.statesDetail = statesDetail;
    }

    /* Getter/Setters */
    public Map<String, StateDetail> getStatesDetail() {
        return statesDetail;
    }

    public static class StateDetail {

        @JsonProperty
        private String version;
        @JsonProperty
        private String status;
        @JsonProperty
        private Integer retryCount;
        @JsonProperty
        private EventsInfo events;

        /* Used for deserialisation by jackson */
        public StateDetail() {
        }

        public StateDetail(EventsInfo events, Integer retryCount, String status, String version) {
            this.events = events;
            this.retryCount = retryCount;
            this.status = status;
            this.version = version;
        }

        /* Getter/Setters */

        public EventsInfo getEvents() {
            return events;
        }

        public Integer getRetryCount() {
            return retryCount;
        }

        public String getStatus() {
            return status;
        }

        public String getVersion() {
            return version;
        }
    }



}
