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

    /**
     * Returns the state machine definition created for the current thread.
     * Ideally, we should prevent any modifications to the state machine definition after this method is called.
     * TODO Will implement safety features later
     * @return
     */
    public StateMachineDefinition getStateMachineDef() {
        return this.stateMachineDefinition.get();
    }

    /**
     * This is used to determine if the LocalContext had been called before to register a new Workflow (which would
     * happen as part of Workflow interception). If the current thread has not been called by the <code>WorkflowInterceptor</code>
     * then it is being called by the client runtime to execute actual user code.
     * @return
     */
    public boolean isWorkflowInterception() {
        return this.getStateMachineDef() != null;
    }
}
