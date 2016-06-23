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
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.impl.RAMContext;
import com.flipkart.flux.representation.IllegalRepresentationException;
import com.flipkart.flux.representation.StateMachinePersistenceService;
import com.google.common.base.Functions;
import com.google.inject.Inject;
import org.apache.commons.lang3.mutable.MutableLong;
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
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * @understands Exposes APIs for end users
 */

@Singleton
@Path("/api/machines")
@Named
public class StateMachineResource {
	
	/** Single white space label to denote start of processing i.e. the Trigger*/
	private static final String TRIGGER = " ";

    StateMachinePersistenceService stateMachinePersistenceService;

    WorkFlowExecutionController workFlowExecutionController;

    StateMachinesDAO stateMachinesDAO;

    EventsDAO eventsDAO;

    ObjectMapper objectMapper;

    @Inject
    public StateMachineResource(EventsDAO eventsDAO, StateMachinePersistenceService stateMachinePersistenceService, StateMachinesDAO stateMachinesDAO, WorkFlowExecutionController workFlowExecutionController) {
        this.eventsDAO = eventsDAO;
        this.stateMachinePersistenceService = stateMachinePersistenceService;
        this.stateMachinesDAO = stateMachinesDAO;
        this.workFlowExecutionController = workFlowExecutionController;
        objectMapper = new ObjectMapper();
    }

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
        return Response.status(Response.Status.ACCEPTED.getStatusCode()).entity(objectMapper.writeValueAsString(triggeredStates)).build();
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
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFsmGraphData(@PathParam("machineId") Long machineId) throws IOException {
        return Response.status(200).entity(getGraphData(machineId))
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Headers", "Content-Type, Accept")
                .build();
    }

    private FsmGraph getGraphData(Long fsmId) throws IOException {
        StateMachine stateMachine = stateMachinesDAO.findById(fsmId);

        if(stateMachine == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        final FsmGraph fsmGraph = new FsmGraph();

        Map<String,Event> stateMachineEvents = eventsDAO.findBySMInstanceId(fsmId).stream().collect(
            Collectors.<Event, String, Event>toMap(Event::getName, (event -> event)));
        Set<String> allOutputEventNames = new HashSet<>();

        final RAMContext ramContext = new RAMContext(System.currentTimeMillis(), null, stateMachine);
        /* After this operation, we'll have nodes for each state and its corresponding output event along with the output event's dependencies mapped out*/
        for(State state : stateMachine.getStates()) {
            if(state.getOutputEvent() != null) {
                EventDefinition eventDefinition = objectMapper.readValue(state.getOutputEvent(), EventDefinition.class);
                final Event outputEvent = stateMachineEvents.get(eventDefinition.getName());
                final FsmGraphVertex vertex = new FsmGraphVertex(state.getId(), getDisplayName(state.getName()));
                fsmGraph.addVertex(vertex,
                    new FsmGraphEdge(getDisplayName(outputEvent.getName()), outputEvent.getStatus().name(),outputEvent.getEventSource()));
                final Set<State> dependantStates = ramContext.getDependantStates(outputEvent.getName());
                dependantStates.forEach((aState) -> fsmGraph.addOutgoingEdge(vertex, aState.getId()));
                allOutputEventNames.add(outputEvent.getName()); // we collect all output event names and use them below.
            } else {
                fsmGraph.addVertex(new FsmGraphVertex(state.getId(),this.getDisplayName(state.getName())),null);
            }
        }

        /* Handle states with no dependencies, i.e the states that can be triggered as soon as we execute the state machine */
        final Set<State> initialStates = ramContext.getInitialStates(Collections.emptySet());// hackety hack.  We're fooling the context to give us only events that depend on nothing
        if (!initialStates.isEmpty()) {
            final FsmGraphEdge initEdge = new FsmGraphEdge(TRIGGER, Event.EventStatus.triggered.name(),TRIGGER);
            initialStates.forEach((state) -> {
                initEdge.addOutgoingVertex(state.getId());
            });
            fsmGraph.addInitStateEdge(initEdge);
        }
        /* Now we handle events that were not "output-ed" by any state, which means that they were given to the workflow at the time of invocation or supplied externally*/
        final HashSet<String> eventsGivenOnWorkflowTrigger = new HashSet<>(stateMachineEvents.keySet());
        eventsGivenOnWorkflowTrigger.removeAll(allOutputEventNames);
        eventsGivenOnWorkflowTrigger.forEach((workflowTriggeredEventName) -> {
            final Event correspondingEvent = stateMachineEvents.get(workflowTriggeredEventName);
            final FsmGraphEdge initEdge = new FsmGraphEdge(this.getDisplayName(workflowTriggeredEventName), correspondingEvent.getStatus().name(),correspondingEvent.getEventSource());
            final Set<State> dependantStates = ramContext.getDependantStates(workflowTriggeredEventName);
            dependantStates.forEach((state) -> initEdge.addOutgoingVertex(state.getId()));
            fsmGraph.addInitStateEdge(initEdge);
        });
        return fsmGraph;
    }
    
    /** 
     * Helper method to return a display friendly name for the specified event name.
     * Returns just the name part from the Event FQN
     */
    private String getEventDisplayName(String eventName) {
    	return (eventName == null) ? null : this.getDisplayName(eventName.substring(eventName.lastIndexOf(".") + 1));
    }
    
    /**
     * Helper method to return a display friendly name for state names
     * Returns {@link #getDisplayName(String)} 
     */
    private String getStateDisplayName(String stateName) {
    	return this.getDisplayName(stateName);
    }

    /** 
     * Helper method to return a display friendly name for the specified label.
     * Returns a phrase containing single-space separated words that were split at Camel Case boundaries
     */
    private String getDisplayName(String label) {
    	if (label == null) {
    		return null;
    	}
    	String words = label.replaceAll( // Based on http://stackoverflow.com/questions/2559759/how-do-i-convert-camelcase-into-human-readable-names-in-java
    			String.format("%s|%s|%s",
    					"(?<=[A-Z])(?=[A-Z][a-z])",
    		    	    "(?<=[^A-Z])(?=[A-Z])",
    		    	    "(?<=[A-Za-z])(?=[^A-Za-z])"
    		    )," ");
    	StringBuffer sb = new StringBuffer();
        for (String s : words.split(" ")) {
            sb.append(Character.toUpperCase(s.charAt(0)));
            if (s.length() > 1) {
                sb.append(s.substring(1, s.length()).toLowerCase());
                sb.append(" "); // add the single space back. Used for wrapping words onto next line in the display
            }
        }
        return sb.toString().trim();
    }
    
}
