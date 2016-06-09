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

import com.fasterxml.jackson.core.JsonGenerationException;
import com.flipkart.flux.api.EventData;
import com.flipkart.flux.api.StateMachineDefinition;

import java.io.IOException;

/**
 * Used to connect with the core Flux Runtime
 * This class hides the actual API call to the Flux runtime
 *
 * @author yogesh.nachnani
 */
public interface FluxRuntimeConnector {
    /** Used to submit a new workflow to the core runtime */
    void submitNewWorkflow(StateMachineDefinition stateMachineDef);
    /* Post the event generated as a result of task execution back to the core runtime */
    void submitEvent(EventData eventData, Long stateMachineId);
}
