package com.flipkart.flux.integration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flipkart.flux.client.model.CorrelationId;
import com.flipkart.flux.client.model.Event;

public class StartEvent implements Event {

    @CorrelationId @JsonProperty
    String id;

    public StartEvent() {
    }

    public StartEvent(String id) {
        this.id = id;
    }

}