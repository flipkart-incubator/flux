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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;

import java.util.List;
import java.util.Map;

/**
 * The <code>WorkflowStateSummary</code> class is a DTO for representing summary for work flow
 *
 * @author ashish.bhutani
 *
 */

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
public class WorkflowStateSummary {

    @Getter
    List<StateSummary> stateSummaries;


    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class StateSummary {
        @JsonProperty
        private Map<String, VersionCount> summary ;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class VersionCount {
        @JsonProperty
        private String version;
        @JsonProperty
        private Integer count;

    }
}
