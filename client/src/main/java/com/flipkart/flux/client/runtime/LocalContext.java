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

import com.flipkart.flux.api.EventDefinition;
import com.flipkart.flux.api.StateDefinition;
import com.flipkart.flux.api.StateMachineDefinition;

import java.util.HashSet;
import java.util.Set;

/**
 * Maintains all local flux related context
 * @author yogesh.nachnani
 */
public class LocalContext {
    private ThreadLocal<StateMachineDefinition> stateMachineDefinition;

    public LocalContext() {
        this(new ThreadLocal<>());
    }

    LocalContext(ThreadLocal<StateMachineDefinition> stateMachineDefinition) {
        this.stateMachineDefinition = stateMachineDefinition;
    }

    /**
     * Creates a new, local Workflow Definition instance
     * @return
     * @param methodIdentifier
     * @param version
     * @param description
     */
    public void registerNew(String methodIdentifier, long version, String description) {
        if (this.stateMachineDefinition.get() != null) {
            /* This ensures we don't compose workflows within workflows */
            throw new IllegalStateException("A single thread cannot execute more than one workflow");
        }
        stateMachineDefinition.set(new StateMachineDefinition(description,methodIdentifier, version, new HashSet<StateDefinition>()));
    }

    public void registerNewState(Long version,
                                 String name, String description,
                                 String hookIdentifier, String taskIdentifier,
                                 Long retryCount, Long timeout,
                                 Set<EventDefinition> eventDefinitionSet
                                 ) {
        final StateDefinition stateDefinition = new StateDefinition(version, name, description,
            hookIdentifier, taskIdentifier, hookIdentifier,
            retryCount, timeout, eventDefinitionSet);
        this.stateMachineDefinition.get().addState(stateDefinition);
    }

    /**
     * Resets the LocalContext so that it is ready to work on the next request
     */
    public void reset() {
        this.stateMachineDefinition.remove();
    }
}
