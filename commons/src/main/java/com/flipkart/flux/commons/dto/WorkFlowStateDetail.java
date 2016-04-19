package com.flipkart.flux.commons.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@Getter
public class WorkFlowStateDetail {

    private Map<String, StateDetail> stateDetail;

    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    @Getter
    @AllArgsConstructor
    static class StateDetail {

        @JsonProperty
        private String version;
        @JsonProperty
        private String status;
        @JsonProperty
        private Integer reTried;
        @JsonProperty
        private Eventsinfo events;

    }



}
