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

import com.flipkart.flux.api.EventData;
import com.flipkart.flux.api.StateMachineDefinition;
import com.flipkart.flux.controller.WorkFlowExecutionController;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.representation.IllegalRepresentationException;
import com.flipkart.flux.representation.StateMachinePersistenceService;
import com.google.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * @understands Exposes APIs for end users
 */

@Singleton
@Path("/api/machines")
@Named
public class StateMachineResource {

    @Inject
    StateMachinePersistenceService stateMachinePersistenceService;

    @Inject
    WorkFlowExecutionController workFlowExecutionController;

    private static final Logger logger = LogManager.getLogger(StateMachineResource.class);

    /**
     * Will instantiate a state machine in the flux execution engine
     * @param stateMachineDefinition User input for state machine
     * @return unique machineId of the instantiated state machine
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response createStateMachine(StateMachineDefinition stateMachineDefinition) {
        // 1. Convert to StateMachine (domain object) and save in DB
        if(stateMachineDefinition == null)
            throw new IllegalRepresentationException("State machine definition is empty");

        StateMachine stateMachine = stateMachinePersistenceService.createStateMachine(stateMachineDefinition);

        // 2. initialize and start State Machine
        workFlowExecutionController.initAndStart(stateMachine);

        // 3. Return machineId
        return Response.status(Response.Status.CREATED.getStatusCode()).entity(stateMachine.getId()).build();
    }


    /**
     * Used to post Data corresponding to an event.
     * This data may be a result of a task getting completed or independently posted (manually, for example)
     * @param machineId machineId the event is to be submitted against
     * @param eventData Json representation of event
     *
     */

    @POST
    @Path("/{machineId}/context/events")
    @Transactional
    public Response submitEvent(@PathParam("machineId") Long machineId,
                            EventData eventData
                            ) {
        //retrieves states which are dependant on this event and starts execution of states which can be executable
        workFlowExecutionController.postEvent(eventData, machineId);
        return Response.status(Response.Status.ACCEPTED.getStatusCode()).build();
    }


    /**
     * Cancel a machine being executed.*
     * @param machineId The machineId to be cancelled
     */

    @PUT
    @Path("/{machineId}/cancel")
    public void cancelExecution(@PathParam("machineId") Long machineId) {
        // Trigger cancellation on all currently executing states
    }
}
