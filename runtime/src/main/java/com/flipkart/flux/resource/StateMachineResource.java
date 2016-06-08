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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.api.EventData;
import com.flipkart.flux.api.EventDefinition;
import com.flipkart.flux.api.StateMachineDefinition;
import com.flipkart.flux.controller.WorkFlowExecutionController;
import com.flipkart.flux.dao.iface.EventsDAO;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.representation.IllegalRepresentationException;
import com.flipkart.flux.representation.StateMachinePersistenceService;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;


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

    @Inject
    StateMachinesDAO stateMachinesDAO;

    @Inject
    EventsDAO eventsDAO;

    private static final Logger logger = LoggerFactory.getLogger(StateMachineResource.class);

    /**
     * Will instantiate a state machine in the flux execution engine
     * @param stateMachineDefinition User input for state machine
     * @return unique machineId of the instantiated state machine
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response createStateMachine(StateMachineDefinition stateMachineDefinition) throws Exception {
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
    public Response submitEvent(@PathParam("machineId") Long machineId,
                            EventData eventData
                            ) throws Exception {
        //retrieves states which are dependant on this event and starts execution of states which can be executable
        Set<State> triggeredStates = workFlowExecutionController.postEvent(eventData, machineId);
        return Response.status(Response.Status.ACCEPTED.getStatusCode()).entity(new ObjectMapper().writeValueAsString(triggeredStates)).build();
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

    /**
     * Provides json data to build fsm status graph.
     * @param machineId
     * @return json representation of fsm
     * @throws JsonProcessingException
     */
    @GET
    @Path("/{machineId}/fsmdata")
    public Response getFsmGraphData(@PathParam("machineId") Long machineId) throws IOException {
        return Response.status(200).entity(getGraphData(machineId))
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Headers", "Content-Type, Accept")
                .build();
    }

    /**
     * Provides json data to build fsm status graph.
     */
    private String getGraphData(Long fsmId) throws IOException {
        StateMachine stateMachine = stateMachinesDAO.findById(fsmId);

        if(stateMachine != null) {

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> eventSourceMap = new HashMap<>();
            Map<String, List<String>> fsmDataMap = new HashMap<>();
            Set<String> initialStates = new HashSet<>();

            Set<String> triggeredEvents = new HashSet<>(eventsDAO.findTriggeredEventsNamesBySMId(fsmId));
            for(State state : stateMachine.getStates()) {
                if(state.getOutputEvent() != null) {
                    EventDefinition eventDefinition = objectMapper.readValue(state.getOutputEvent(), EventDefinition.class);
                    eventSourceMap.put(eventDefinition.getName(), state.getName());
                }
            }

            for(State state: stateMachine.getStates()) {
                if(state.getDependencies() != null && state.getDependencies().size() > 0) {
                    for(String eventName : state.getDependencies()) {
                        if(triggeredEvents.contains(eventName)) {
                            if(fsmDataMap.get(eventSourceMap.get(eventName)) == null)
                                fsmDataMap.put(eventSourceMap.get(eventName), new ArrayList<>());
                            if(fsmDataMap.get(state.getName()) == null)
                                fsmDataMap.put(state.getName(), new ArrayList<>());
                            fsmDataMap.get(eventSourceMap.get(eventName)).add(state.getName() + ":" + (eventName));
                        }
                    }
                } else {
                    initialStates.add(state.getName());
                }
            }

            if(fsmDataMap.size() == 0) {
                for(String stateName : initialStates) {
                    fsmDataMap.put(stateName, null);
                }
            }

            fsmDataMap.remove(null);
            return new ObjectMapper().writeValueAsString(fsmDataMap);

        } else {
            return "{}";
        }
    }

}
