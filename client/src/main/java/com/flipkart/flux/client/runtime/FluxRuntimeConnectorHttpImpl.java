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
 *
 */

package com.flipkart.flux.client.runtime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.api.EventData;
import com.flipkart.flux.api.ExecutionUpdateData;
import com.flipkart.flux.api.StateMachineDefinition;

/**
 * RuntimeConnector that connects to runtime over HTTP Internally, this uses Unirest as of now. This
 * makes it difficult to write unit tests for this class, but it does very little so its okay
 *
 * @author yogesh.nachnani
 */
public class FluxRuntimeConnectorHttpImpl implements FluxRuntimeConnector {

    public static final String EXTERNAL = "external";
    private final ObjectMapper objectMapper;
    private final FluxHttpClient fluxHttpClient;


    public FluxRuntimeConnectorHttpImpl(FluxHttpClient fluxHttpClient, ObjectMapper objectMapper) {
        this.fluxHttpClient = fluxHttpClient;
        this.objectMapper = objectMapper;

    }

    @Override
    public void submitNewWorkflow(StateMachineDefinition stateMachineDef) {
        fluxHttpClient.postOverHttp(stateMachineDef, "");
    }

    @Override
    public void submitEvent(EventData eventData, Long stateMachineId) {
        fluxHttpClient.postOverHttp(eventData, "/" + stateMachineId + "/context/events");
    }

    @Override
    public void submitEvent(String name, Object data, String correlationId, String eventSource) {
        final String eventType = data.getClass().getName();
        if (eventSource == null) {
            eventSource = EXTERNAL;
        }
        try {
            final EventData
                eventData =
                new EventData(name, eventType, objectMapper.writeValueAsString(data), eventSource);
           fluxHttpClient. postOverHttp(eventData,
                         "/" + correlationId + "/context/events?searchField=correlationId");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Interface method implementation. Updates the status in persistence store by invoking suitable
     * Flux runtime API
     *
     * @see com.flipkart.flux.client.runtime.FluxRuntimeConnector#updateExecutionStatus(ExecutionUpdateData)
     */
    public void updateExecutionStatus(ExecutionUpdateData executionUpdateData) {
       fluxHttpClient. postOverHttp(executionUpdateData,
                     "/" + executionUpdateData.getStateMachineId() + "/" + executionUpdateData
                         .getTaskId() + "/status");
    }

    /**
     * Interface method implementation. Increments the execution retries in persistence by invoking
     * suitable Flux runtime API
     *
     * @see com.flipkart.flux.client.runtime.FluxRuntimeConnector#incrementExecutionRetries(java.lang.Long,
     * java.lang.Long)
     */
    @Override
    public void incrementExecutionRetries(Long stateMachineId, Long taskId) {
        fluxHttpClient.postOverHttp(null, "/" + stateMachineId + "/" + taskId + "/retries/inc");
    }

    /**
     * Interface method implementation. Posts to Flux Runtime API to redrive a task.
     */
    @Override
    public void redriveTask(Long taskId) {
        fluxHttpClient.postOverHttp(null, "/redrivetask/" + taskId);
    }

}
