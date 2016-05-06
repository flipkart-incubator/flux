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
 * The <code>WorkflowVersionStatus</code> class is a DTO for representing status per version
 *
 * @author ashish.bhutani
 *
 */
public class WorkflowVersionStatus {

    @JsonProperty
    private Map<String, WorkflowStatusCount> versionStatus;

    WorkflowVersionStatus() {
    }

    public WorkflowVersionStatus(Map<String, WorkflowStatusCount> versionStatus) {
        this.versionStatus = versionStatus;
    }

    /* Getter/Setter Methods */
    public Map<String, WorkflowStatusCount> getVersionStatus() {
        return versionStatus;
    }
}
