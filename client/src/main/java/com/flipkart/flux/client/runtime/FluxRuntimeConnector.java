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

import com.flipkart.flux.api.EventData;
import com.flipkart.flux.api.ExecutionUpdateData;
import com.flipkart.flux.api.StateMachineDefinition;

/**
 * Used to connect with the core Flux Runtime
 * This class hides the actual API call to the Flux runtime
 *
 * @author yogesh.nachnani
 */
public interface FluxRuntimeConnector {

    /** Used to submit a new workflow to the core runtime */
    void submitNewWorkflow(StateMachineDefinition stateMachineDef);

    /* Post the event generated as a result of task execution back to the core runtime, also updates the state status to completed which generated this event*/
    void submitEventAndUpdateStatus(EventData eventData, Long stateMachineId, ExecutionUpdateData executionUpdateData);

    /**
     * Post an arbitrary event against a previously registered correlationId
     * @param name name of the event. Should be the same as the name given using <code>ExternalEvent</code> annotation
     * @param data data to post against the given event name
     * @param correlationId the string used to identify a workflow instance (as passed using <code>CorrelationId</code> annotation
     * @param eventSource optional string to denote an event source
     */
    void submitEvent(String name, Object data,String correlationId,String eventSource);
    
    /**
     * Updates the status of the Task identified by the specified Task ID 
     * @param executionUpdateData the execution update data
     */
    void updateExecutionStatus(ExecutionUpdateData executionUpdateData);
    
    /**
     * Increments the attempted retries count for the Task identified by the specified task Id
     * @param stateMachineId the state machine identifier
     * @param taskId identifier for the Task whose retry count is to be updated
     */
    void incrementExecutionRetries(Long stateMachineId, Long taskId);

    /**
     * Posts to Flux Runtime by connecting over Http to redrive a task.
     * @param taskId the task/state identifier
     */
    void redriveTask(Long taskId);
}
