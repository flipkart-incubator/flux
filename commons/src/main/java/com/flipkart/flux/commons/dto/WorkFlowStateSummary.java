package com.flipkart.flux.commons.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;

import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
public class WorkFlowStateSummary {

    @Getter
    List<StateSummary> stateSummaries;


    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    static class StateSummary {
        @JsonProperty
        private Map<String, VersionCount> summary ;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    static class VersionCount {
        @JsonProperty
        private String version;
        @JsonProperty
        private Integer count;

    }
}
