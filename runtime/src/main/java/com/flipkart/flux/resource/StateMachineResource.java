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
 */

package com.flipkart.flux.resource;

import com.flipkart.flux.api.StateMachineDefinition;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.representation.DomainTypeCreator;
import com.flipkart.flux.representation.IllegalRepresentationException;
import com.google.inject.Inject;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * @understands Exposes APIs for end users
 */
@Singleton
public class StateMachineResource<T> {

    /** Instance of {@link DomainTypeCreator} which converts entity definition to domain object*/
    @Inject
    DomainTypeCreator domainTypeCreator;

    /**
     * Will instantiate a state machine in the flux execution engine
     * @param stateMachineDefinition User input for state machine
     * @return unique machineId of the instantiated state machine
     */
    @POST
    @Path("/machines")
    @Transactional
    public String createStateMachine(StateMachineDefinition<T> stateMachineDefinition) {
        // 1. Convert to StateMachine (domain object) and save in DB
        if(stateMachineDefinition == null)
            throw new IllegalRepresentationException("State machine definition is empty");

        StateMachine stateMachine = domainTypeCreator.createStateMachine(stateMachineDefinition);

        // 2. workFlowExecutionController.init(stateMachine_domainObject)

        // 3. Return machineId
        return stateMachine.getId();
    }

    
    /**
     * Used to post Data corresponding to an event.
     * This data may be a result of a task getting completed or independently posted (manually, for example)
     * @param machineId machineId the event is to be submitted against
     * @param eventFqn fully qualified name of the event. Like java.lang.String_foo
     * @param eventDataJson Json representation of data
     */

    @POST
    @Path("/machines/{machineId}/context/events/{eventFqn}")
    public void submitEvent(@PathParam("machineId") Long machineId,
                            @PathParam("eventFqn") String eventFqn,
                            String eventDataJson
                            ) {
        // Controversial API. This assumes, for now, that events are pushed to flux
        // 1. Retrieve StateMachine's execution context
        // 2. Submit event to it - workFlowExecutionController.postEvent(context, eventFqn,eventDataJson)
    }


    /**
     * Cancel a machine being executed.*
     * @param machineId The machineId to be cancelled
     */

    @PUT
    @Path("/machines/{machineId}/cancel")
    public void cancelExecution(@PathParam("machineId") Long machineId) {
        // Trigger cancellation on all currently executing states
    }
}
