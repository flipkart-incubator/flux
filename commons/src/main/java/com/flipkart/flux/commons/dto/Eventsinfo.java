package com.flipkart.flux.commons.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
public class Eventsinfo {

    @Getter
    @JsonProperty
    private Map<String, EventData> eventData;

    @NoArgsConstructor(access = AccessLevel.PACKAGE)
    @AllArgsConstructor
    @Getter
    static class EventData {
        @JsonProperty
        private String status;
        @JsonProperty
        private String data;
    }



}
