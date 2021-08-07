package com.flipkart.flux.examples.benchmark;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EventTypeInteger implements com.flipkart.flux.client.model.Event {
    @JsonProperty
    Integer value;

    EventTypeInteger() {
    }

    public EventTypeInteger(Integer x) {
        value = x;
    }

    public Integer getValue() {
        return value;
    }
}
