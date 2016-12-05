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

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.api.EventData;
import com.flipkart.flux.api.EventDefinition;
import com.flipkart.flux.api.ExecutionUpdateData;
import com.flipkart.flux.api.StateMachineDefinition;
import com.flipkart.flux.controller.WorkFlowExecutionController;
import com.flipkart.flux.dao.iface.AuditDAO;
import com.flipkart.flux.dao.iface.EventsDAO;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.dao.iface.StatesDAO;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.impl.RAMContext;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * <code>StateMachineResource</code> exposes APIs to perform state machine related operations. Ex: Creating SM, receiving event for a SM
 * @author shyam.akirala
 * @author yogesh
 * @author regunath.balasubramanian
 */

@Singleton
@Path("/api/machines")
@Named
public class StateMachineResource {

    /** Single white space label to denote start of processing i.e. the Trigger*/
	private static final String TRIGGER = " ";

    public static final String CORRELATION_ID = "correlationId";

    StateMachinePersistenceService stateMachinePersistenceService;

    WorkFlowExecutionController workFlowExecutionController;

    StateMachinesDAO stateMachinesDAO;

    StatesDAO statesDAO;

    EventsDAO eventsDAO;

    AuditDAO auditDAO;

    ObjectMapper objectMapper;

    @Inject
    public StateMachineResource(EventsDAO eventsDAO, StateMachinePersistenceService stateMachinePersistenceService,
                                AuditDAO auditDAO, StateMachinesDAO stateMachinesDAO, StatesDAO statesDAO, WorkFlowExecutionController workFlowExecutionController) {
        this.eventsDAO = eventsDAO;
        this.stateMachinePersistenceService = stateMachinePersistenceService;
        this.stateMachinesDAO = stateMachinesDAO;
        this.statesDAO = statesDAO;
        this.auditDAO = auditDAO;
        this.workFlowExecutionController = workFlowExecutionController;
        objectMapper = new ObjectMapper();
    }

    /** Logger instance for this class*/
    private static final Logger logger = LoggerFactory.getLogger(StateMachineResource.class);

    /**
     * Will instantiate a state machine in the flux execution engine
     * @param stateMachineDefinition User input for state machine
     * @return unique machineId of the instantiated state machine
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    @Timed
    public Response createStateMachine(StateMachineDefinition stateMachineDefinition) throws Exception {
        // 1. Convert to StateMachine (domain object) and save in DB
        if(stateMachineDefinition == null)
            throw new IllegalRepresentationException("State machine definition is empty");

        StateMachine stateMachine = stateMachinePersistenceService.createStateMachine(stateMachineDefinition);

        // 2. initialize and start State Machine
        workFlowExecutionController.initAndStart(stateMachine);

        logger.info("Created state machine with Id: {} and correlation Id: {}", stateMachine.getId(), stateMachine.getCorrelationId());

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
    @Timed
    public Response submitEvent(@PathParam("machineId") String machineId,
                                @QueryParam("searchField") String searchField,
                                EventData eventData
                            ) throws Exception {
        logger.info("Received event: {} for state machine: {}", eventData.getName(), machineId);

        if (searchField != null) {
            if (!searchField.equals(CORRELATION_ID)) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            workFlowExecutionController.postEvent(eventData, null, machineId);
        } else {
            workFlowExecutionController.postEvent(eventData, Long.valueOf(machineId), null);
        }
        return Response.status(Response.Status.ACCEPTED.getStatusCode()).build();
    }

    /**
     * Updates the status of the specified Task under the specified State machine
     * @param machineId the state machine identifier
     * @param stateId the task/state identifier
     * @param status the Status 
     * @return Response with execution status code
     * @throws Exception
     */
    @POST
    @Path("/{machineId}/{stateId}/status")
    @Transactional
    @Timed
    public Response updateStatus(@PathParam("machineId") Long machineId,
                                @PathParam("stateId") Long stateId,
                                ExecutionUpdateData executionUpdateData
                            ) throws Exception {
    	com.flipkart.flux.domain.Status updateStatus = null;
		switch (executionUpdateData.getStatus()) {
			case initialized:
				updateStatus = com.flipkart.flux.domain.Status.initialized;
				break;
			case running:
				updateStatus = com.flipkart.flux.domain.Status.running;
				break;
			case completed:
				updateStatus = com.flipkart.flux.domain.Status.completed;
				break;
			case cancelled:
				updateStatus = com.flipkart.flux.domain.Status.cancelled;
				break;
			case errored:
				updateStatus = com.flipkart.flux.domain.Status.errored;
				break;
			case sidelined:
				updateStatus = com.flipkart.flux.domain.Status.sidelined;
				break;
    	}
		this.workFlowExecutionController.updateExecutionStatus(machineId, stateId, updateStatus, executionUpdateData.getRetrycount(),
                executionUpdateData.getCurrentRetryCount(), executionUpdateData.getErrorMessage(), executionUpdateData.isDeleteFromRedriver());
    	return Response.status(Response.Status.ACCEPTED.getStatusCode()).build();
    }
    
    /**
     * Increments the retry count for the specified Task under the specified State machine
     * @param machineId the state machine identifier
     * @param stateId the task/state identifier
     * @return Response with execution status code
     * @throws Exception
     */
    @POST
    @Path("/{machineId}/{stateId}/retries/inc")
    @Transactional
    public Response incrementRetry(@PathParam("machineId") Long machineId,
                                @PathParam("stateId") Long stateId
                            ) throws Exception {
    	this.workFlowExecutionController.incrementExecutionRetries(machineId, stateId);
    	return Response.status(Response.Status.ACCEPTED.getStatusCode()).build();
    }

    /**
     * Triggers task execution if the task is stalled and no.of retries are not exhausted.
     * @param taskId the task/state identifier
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/redrivetask/{taskId}")
    @Timed
    public Response redriveTask(@PathParam("taskId") Long taskId) throws Exception {

        this.workFlowExecutionController.redriveTask(taskId);

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

    /**
     * Provides json data to build fsm status graph.
     * @param machineId
     * @return json representation of fsm
     * @throws JsonProcessingException
     */
    @GET
    @Path("/{machineId}/fsmdata")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFsmGraphData(@PathParam("machineId") String machineId) throws IOException {
        return Response.status(200).entity(getGraphData(machineId))
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Headers", "Content-Type, Accept")
                .build();
    }

    /**
     * Retrieves all errored states for the given range of state machine ids. Max range is 1,000,000.
     * @param fromStateMachineId starting id for the range
     * @param toStateMachineId ending id (inclusive)
     * @return json containing list of [state machine id,  state id, status]
     */
    @GET
    @Path("/{stateMachineName}/states/errored")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getErroredStates(@PathParam("stateMachineName") String stateMachineName,
                                     @QueryParam("fromSmId") Long fromStateMachineId,
                                     @QueryParam("toSmId") Long toStateMachineId) {
        if(fromStateMachineId == null || fromStateMachineId < 0) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).entity("start of the range not provided or invalid").build();
        }

        long limit = fromStateMachineId + 1_000_000;
        /* if toStateMachineId is invalid just use fromStateMachineId as the end, otherwise limit the range to max of 1,000,000 */
        toStateMachineId = (toStateMachineId == null || toStateMachineId < fromStateMachineId) ? fromStateMachineId : Math.min(limit, toStateMachineId);

        return Response.status(200).entity(statesDAO.findErroredStates(stateMachineName, fromStateMachineId, toStateMachineId)).build();
    }

    /**
     * This api unsidelines a single state and triggers its execution.
     * @param stateMachineId
     * @param stateId
     * @return
     */
    @PUT
    @Path("/{stateMachineId}/{stateId}/unsideline")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response unsidelineState(@PathParam("stateMachineId") Long stateMachineId, @PathParam("stateId") Long stateId) {
        this.workFlowExecutionController.unsidelineState(stateMachineId, stateId);

        return Response.status(Response.Status.ACCEPTED.getStatusCode()).build();
    }

    /** Retrieves fsm graph data based on FSM Id or correlation id*/
    private FsmGraph getGraphData(String machineId) throws IOException {
        Long fsmId = null;
        try {
            fsmId = Long.valueOf(machineId);
        } catch (NumberFormatException ignored) {}

        StateMachine stateMachine;

        if (fsmId != null) {
            stateMachine = stateMachinesDAO.findById(fsmId);
        } else { //if fsmId is null, that means the passed id is correlation id
            stateMachine = stateMachinesDAO.findByCorrelationId(machineId);
        }

        if(stateMachine == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("State machine with Id: "+machineId+" not found").build());
        }
        final FsmGraph fsmGraph = new FsmGraph();
        fsmGraph.setStateMachineId(stateMachine.getId());
        fsmGraph.setCorrelationId(stateMachine.getCorrelationId());
        fsmGraph.setFsmVersion(stateMachine.getVersion());
        fsmGraph.setFsmName(stateMachine.getName());

        Map<String,Event> stateMachineEvents = eventsDAO.findBySMInstanceId(stateMachine.getId()).stream().collect(
            Collectors.<Event, String, Event>toMap(Event::getName, (event -> event)));
        Set<String> allOutputEventNames = new HashSet<>();

        final RAMContext ramContext = new RAMContext(System.currentTimeMillis(), null, stateMachine);
        /* After this operation, we'll have nodes for each state and its corresponding output event along with the output event's dependencies mapped out*/
        for(State state : stateMachine.getStates()) {
            final FsmGraphVertex vertex = new FsmGraphVertex(state.getId(), this.getStateDisplayName(state.getName()));
            if(state.getOutputEvent() != null) {
                EventDefinition eventDefinition = objectMapper.readValue(state.getOutputEvent(), EventDefinition.class);
                final Event outputEvent = stateMachineEvents.get(eventDefinition.getName());
                fsmGraph.addVertex(vertex,
                    new FsmGraphEdge(getEventDisplayName(outputEvent.getName()), state.getStatus().name(),outputEvent.getEventSource()));
                final Set<State> dependantStates = ramContext.getDependantStates(outputEvent.getName());
                dependantStates.forEach((aState) -> fsmGraph.addOutgoingEdge(vertex, aState.getId()));
                allOutputEventNames.add(outputEvent.getName()); // we collect all output event names and use them below.
            } else {
                fsmGraph.addVertex(vertex,
                        new FsmGraphEdge(null, state.getStatus().name(),null));
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
            final FsmGraphEdge initEdge = new FsmGraphEdge(this.getEventDisplayName(workflowTriggeredEventName), correspondingEvent.getStatus().name(),correspondingEvent.getEventSource());
            final Set<State> dependantStates = ramContext.getDependantStates(workflowTriggeredEventName);
            dependantStates.forEach((state) -> initEdge.addOutgoingVertex(state.getId()));
            fsmGraph.addInitStateEdge(initEdge);
        });

        fsmGraph.setAuditData(auditDAO.findBySMInstanceId(stateMachine.getId()));

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
