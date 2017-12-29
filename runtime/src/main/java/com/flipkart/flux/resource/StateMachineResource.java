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
import com.flipkart.flux.api.*;
import com.flipkart.flux.client.runtime.EventProxyConnector;
import com.flipkart.flux.controller.WorkFlowExecutionController;
import com.flipkart.flux.dao.ParallelScatterGatherQueryHelper;
import com.flipkart.flux.dao.iface.AuditDAO;
import com.flipkart.flux.dao.iface.EventsDAO;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.dao.iface.StatesDAO;
import com.flipkart.flux.domain.*;
import com.flipkart.flux.domain.Status;
import com.flipkart.flux.exception.IllegalEventException;
import com.flipkart.flux.impl.RAMContext;
import com.flipkart.flux.metrics.iface.MetricsClient;
import com.flipkart.flux.persistence.DataSourceType;
import com.flipkart.flux.persistence.SelectDataSource;
import com.flipkart.flux.persistence.Storage;
import com.flipkart.flux.representation.IllegalRepresentationException;
import com.flipkart.flux.representation.StateMachinePersistenceService;
import com.flipkart.flux.task.eventscheduler.EventSchedulerRegistry;
import com.google.inject.Inject;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static com.flipkart.flux.Constants.STATE_MACHINE_ID;
import static com.flipkart.flux.Constants.TASK_ID;


/**
 * <code>StateMachineResource</code> exposes APIs to perform state machine related operations. Ex: Creating SM, receiving event for a SM
 *
 * @author shyam.akirala
 * @author yogesh
 * @author regunath.balasubramanian
 */
@Singleton
@Path("/api/machines")
@Named
public class StateMachineResource {

    /**
     * Single white space label to denote start of processing i.e. the Trigger
     */
    private static final String TRIGGER = " ";

    private static final String CORRELATION_ID = "correlationId";

    private StateMachinePersistenceService stateMachinePersistenceService;

    private WorkFlowExecutionController workFlowExecutionController;

    private StateMachinesDAO stateMachinesDAO;

    private StatesDAO statesDAO;

    private EventsDAO eventsDAO;

    private AuditDAO auditDAO;

    private EventSchedulerRegistry eventSchedulerRegistry;

    private ObjectMapper objectMapper;

    private MetricsClient metricsClient;

    private ParallelScatterGatherQueryHelper parallelScatterGatherQueryHelper;

    private EventProxyConnector eventProxyConnector;

    private String eventProxyEnabled;

    @Inject
    public StateMachineResource(EventsDAO eventsDAO, StateMachinePersistenceService stateMachinePersistenceService,
                                AuditDAO auditDAO, StateMachinesDAO stateMachinesDAO, StatesDAO statesDAO,
                                WorkFlowExecutionController workFlowExecutionController, MetricsClient metricsClient,
                                ParallelScatterGatherQueryHelper parallelScatterGatherQueryHelper,
                                EventSchedulerRegistry eventSchedulerRegistry,
                                EventProxyConnector eventProxyConnector,
                                @Named("eventProxyForMigration.enabled") String eventProxyEnabled) {
        this.eventsDAO = eventsDAO;
        this.stateMachinePersistenceService = stateMachinePersistenceService;
        this.stateMachinesDAO = stateMachinesDAO;
        this.statesDAO = statesDAO;
        this.auditDAO = auditDAO;
        this.eventSchedulerRegistry = eventSchedulerRegistry;
        this.workFlowExecutionController = workFlowExecutionController;
        this.objectMapper = new ObjectMapper();
        this.metricsClient = metricsClient;
        this.parallelScatterGatherQueryHelper = parallelScatterGatherQueryHelper;
        this.eventProxyConnector = eventProxyConnector;
        this.eventProxyEnabled = eventProxyEnabled;
    }

    /**
     * Logger instance for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(StateMachineResource.class);

    /**
     * Will instantiate a state machine in the flux execution engine
     *
     * @param stateMachineDefinition User input for state machine
     * @return unique machineId of the instantiated state machine
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed
    public Response createStateMachine(StateMachineDefinition stateMachineDefinition) throws Exception {

        if (stateMachineDefinition == null)
            throw new IllegalRepresentationException("State machine definition is empty");

        StateMachine stateMachine = null;


        final String stateMachineInstanceId;
        if (stateMachineDefinition.getCorrelationId() != null && !stateMachineDefinition.getCorrelationId().isEmpty()) {
            stateMachineInstanceId = stateMachineDefinition.getCorrelationId();
        } else {
            stateMachineInstanceId = UUID.randomUUID().toString();
        }

        try {
            stateMachine = createAndInitStateMachine(stateMachineInstanceId, stateMachineDefinition);
            metricsClient.markMeter(new StringBuilder().
                    append("stateMachine.").
                    append(stateMachine.getName()).
                    append(".started").toString());
        } catch (ConstraintViolationException ex) {
            //in case of Duplicate correlation key, return http code 409 conflict
            return Response.status(Response.Status.CONFLICT.getStatusCode()).entity(ex.getCause() != null ? ex.getCause().getMessage() : null).build();
        } catch (Exception ex) {
            logger.error("Failed During Creating or Initiating StateMachine with id {} {}", stateMachineInstanceId, ex.getStackTrace());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(ex.getCause() != null ? ex.getCause().getMessage() : null).build();
        }

        return Response.status(Response.Status.CREATED.getStatusCode()).entity(stateMachine.getId()).build();
    }

    /**
     * Creates and starts the state machine. Keeping this method as "protected" so that Transactional interceptor can intercept the call.
     */

    protected StateMachine createAndInitStateMachine(String stateMachineInstanceId, StateMachineDefinition stateMachineDefinition) throws Exception {

        // 1. Convert to StateMachine (domain object) and save in DB
        StateMachine stateMachine = stateMachinePersistenceService.createStateMachine(stateMachineInstanceId, stateMachineDefinition);
        MDC.clear();
        MDC.put(STATE_MACHINE_ID, stateMachine.getId().toString());
        logger.info("Created state machine with Id: {}", stateMachine.getId());

        // 2. initialize and start State Machine
        workFlowExecutionController.initAndStart(stateMachine);

        return stateMachine;
    }

    /**
     * Used to post Data corresponding to an event.
     * This data may be a result of a task getting completed or independently posted (manually, for example)
     *
     * @param machineId machineId the event is to be submitted against
     * @param eventData Json representation of event
     */
    @POST
    @Path("/{machineId}/context/events")
    @Timed
    public Response submitEvent(@PathParam("machineId") String machineId,
                                @QueryParam("searchField") String searchField,
                                @QueryParam("triggerTime") Long triggerTime,
                                EventData eventData
    ) throws Exception {
        MDC.clear();
        MDC.put(STATE_MACHINE_ID, machineId);
        logger.info("Received event: {} for state machine: {}", eventData.getName(), machineId);

        StateMachine stateMachine = null;
        stateMachine = stateMachinesDAO.findById(machineId);
        if (stateMachine == null) {
            if (eventProxyEnabled.equalsIgnoreCase("yes")) {
                logger.warn("StateMachine {} not found in this cluster. Forwarding this event to the old cluster.");
                if (triggerTime == null) {
                    try {
                        eventProxyConnector.submitEvent(eventData.getName(), eventData.getData(), machineId, eventData.getEventSource());
                    } catch (Exception ex) {
                        logger.error("Unable to forward to old endpoint, error {}", ex.getCause());
                    }

                } else {
                    try {
                        eventProxyConnector.submitScheduledEvent(eventData.getName(), eventData.getData(), machineId, eventData.getEventSource(), triggerTime);
                    } catch (Exception ex) {
                        logger.error("Unable to forward to old endpoint, error {}", ex.getCause());
                    }
                }
                return Response.status(Response.Status.ACCEPTED.getStatusCode()).entity("State Machine with Id: " + machineId + " not found on this cluster. Forwarding the event to the old cluster").build();
            } else {
                logger.error("StateMachine not found with id: {}, rejecting the event", machineId);
                return Response.status(Response.Status.NOT_FOUND).entity("StateMachine not found with id: " + machineId + ", rejecting the event").build();
            }
        }

        if (stateMachine.getStatus() == StateMachineStatus.cancelled) {
            logger.info("Discarding event: {} as State machine: {} is in cancelled state", eventData.getName(), stateMachine.getId());
            return Response.status(Response.Status.ACCEPTED.getStatusCode()).entity("State machine with Id: " + stateMachine.getId() + " is in 'cancelled' state. Discarding the event.").build();
        }

        if (triggerTime == null) {
            logger.info("Received event: {} for state machine: {}", eventData.getName(), machineId);
            try {
                if (eventData.getCancelled() != null && eventData.getCancelled()) {
                    workFlowExecutionController.handlePathCancellation(stateMachine, eventData);
                } else {
                    workFlowExecutionController.postEvent(eventData, stateMachine.getId());
                }
                return Response.status(Response.Status.ACCEPTED).build();
            } catch (IllegalEventException ex) {
                return Response.status(Response.Status.NOT_FOUND.getStatusCode()).entity(ex.getMessage()).build();
            }
        } else {
            logger.info("Received event: {} for state machine: {} with triggerTime: {}", eventData.getName(), machineId, triggerTime);
            if (searchField == null || !searchField.equals(CORRELATION_ID))
                return Response.status(Response.Status.BAD_REQUEST).entity("searchField=correlationId is missing in the request").build();

            //if trigger time is more than below value, it means the value has been passed in milliseconds, convert it to seconds and register
            if (triggerTime > 9999999999L)
                triggerTime = triggerTime / 1000;
            eventSchedulerRegistry.registerEvent(machineId, eventData.getName(), objectMapper.writeValueAsString(eventData), triggerTime);
            return Response.status(Response.Status.ACCEPTED).build();
        }
    }

    /**
     * Used to post Data corresponding to an event. This also updates the task status to completed which generated the event.
     *
     * @param machineId             machineId the event is to be submitted against
     * @param eventAndExecutionData Json representation of event and execution updation data
     */
    @POST
    @Path("/{machineId}/context/eventandstatus")
    @Timed
    public Response submitEvent(@PathParam("machineId") String machineId,
                                EventAndExecutionData eventAndExecutionData
    ) throws Exception {
        EventData eventData = eventAndExecutionData.getEventData();
        ExecutionUpdateData executionUpdateData = eventAndExecutionData.getExecutionUpdateData();
        MDC.clear();
        MDC.put(STATE_MACHINE_ID, machineId);
        MDC.put(TASK_ID, executionUpdateData.getTaskId().toString());
        logger.info("Received event: {} from state: {} for state machine: {}", eventData.getName(), executionUpdateData.getTaskId(), machineId);
        try {
            this.workFlowExecutionController.updateTaskStatus(machineId, executionUpdateData.getTaskId(), executionUpdateData);
        } catch (Exception ex) {
            logger.error("exception {} {}", ex.getMessage(), ex.getStackTrace());
        }
        return postEvent(machineId, null, eventData);
    }

    private Response postEvent(String machineId, String searchField, EventData eventData) {
        try {
            StateMachine stateMachine = null;
            if (searchField != null && !searchField.equals(CORRELATION_ID)) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            } else {
                stateMachine = stateMachinesDAO.findById(machineId);
            }

            if (stateMachine == null) {
                return Response.status(Response.Status.NOT_FOUND.getStatusCode()).entity("State machine with Id: " + machineId + " not found").build();
            }

            if (stateMachine.getStatus() == StateMachineStatus.cancelled) {
                logger.info("Discarding event: {} as State machine: {} is in cancelled state", eventData.getName(), stateMachine.getId());
                return Response.status(Response.Status.ACCEPTED.getStatusCode()).entity("State machine with Id: " + machineId + " is in 'cancelled' state. Discarding the event.").build();
            }

            workFlowExecutionController.postEvent(eventData, stateMachine.getId());
        } catch (IllegalEventException ex) {
            return Response.status(Response.Status.NOT_FOUND.getStatusCode()).entity(ex.getMessage()).build();

        }

        return Response.status(Response.Status.ACCEPTED).build();
    }

    /**
     * Updates the status of the specified Task under the specified State machine
     *
     * @param machineId the state machine identifier
     * @param stateId   the task/state identifier
     * @return Response with execution status code
     * @throws Exception
     */
    @POST
    @Path("/{machineId}/{stateId}/status")
    @Timed
    public Response updateStatus(@PathParam("machineId") String machineId,
                                 @PathParam("stateId") Long stateId,
                                 ExecutionUpdateData executionUpdateData
    ) throws Exception {
        this.workFlowExecutionController.updateTaskStatus(machineId, stateId, executionUpdateData);
        return Response.status(Response.Status.ACCEPTED).build();
    }


    /**
     * Increments the retry count for the specified Task under the specified State machine
     *
     * @param machineId the state machine identifier
     * @param stateId   the task/state identifier
     * @return Response with execution status code
     * @throws Exception
     */
    @POST
    @Path("/{machineId}/{stateId}/retries/inc")
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public Response incrementRetry(@PathParam("machineId") String machineId,
                                   @PathParam("stateId") Long stateId
    ) throws Exception {
        this.workFlowExecutionController.incrementExecutionRetries(machineId, stateId);
        return Response.status(Response.Status.ACCEPTED).build();
    }

    /**
     * Triggers task execution if the task is stalled and no.of retries are not exhausted.
     *
     * @param taskId the task/state identifier
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/redrivetask/{machineId}/taskId/{taskId}")
    @Timed
    public Response redriveTask(@PathParam("machineId") String machineId, @PathParam("taskId") Long taskId) throws Exception {

        this.workFlowExecutionController.redriveTask(machineId, taskId);

        return Response.status(Response.Status.ACCEPTED).build();
    }

    /**
     * Provides json data to build fsm status graph.
     *
     * @param machineId
     * @return json representation of fsm
     * @throws JsonProcessingException
     */
    @GET
    @Path("/{machineId}/fsmdata")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFsmGraphData(@PathParam("machineId") String machineId) throws IOException {
        return Response.status(200).entity(getGraphData(machineId)).build();
    }

    /**
     * Retrieves all errored states for the given range of state machine ids. Max range is 1,000,000.
     *
     * @param fromStateMachineId starting id for the range
     * @param toStateMachineId   ending id (inclusive)
     * @return json containing list of [state machine id,  state id, status]
     */
    @GET
    @Path("/{stateMachineName}/states/errored")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getErroredStates(@PathParam("stateMachineName") String stateMachineName,
                                     @QueryParam("fromSmId") String fromStateMachineId,
                                     @QueryParam("toSmId") String toStateMachineId) {
        if (fromStateMachineId == null || fromStateMachineId.length() <= 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Not a valid starting stateMachineId, either null or empty!!").build();
        }
        if (toStateMachineId == null || toStateMachineId.length() <= 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Not a valid ending stateMachineId, either null or empty!!").build();
        }

        return Response.status(200).entity(parallelScatterGatherQueryHelper.findErroredStates(stateMachineName, fromStateMachineId, toStateMachineId)).build();
    }

    /**
     * Retrieves all states for the given range of time for a particular state machine name.
     * Will also filter by status if it is given.
     *
     * @return json containing list of [state machine id, state id, status]
     */
    @GET
    @Path("/{stateMachineName}/states/listbytime")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatesByTime(@PathParam("stateMachineName") String stateMachineName,
                                    @QueryParam("fromTime") String fromTime,
                                    @QueryParam("toTime") String toTime,
                                    @QueryParam("stateName") String stateName,
                                    @QueryParam("statuses") final List<String> statusStrings) throws Exception {
        if (fromTime == null || toTime == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Required params fromTime/toTime are not provided").build();
        }

        Timestamp fromTimestamp = Timestamp.valueOf(fromTime);
        Timestamp toTimestamp = Timestamp.valueOf(toTime);

        if (fromTimestamp.after(toTimestamp)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("fromTime: " + fromTime + " should be before toTime: " + toTime).build();
        }
        List<Status> statuses = new ArrayList<Status>();
        if (statusStrings != null && !statusStrings.isEmpty()) {
            for (String status : statusStrings) {
                try {
                    statuses.add(Status.valueOf(status));
                } catch (IllegalArgumentException e) {
                    return Response.status(Response.Status.BAD_REQUEST).entity("status: " + status + " must be one of initialized, running, completed, cancelled, errored, sidelined, unsidelined").build();
                }
            }
        }
        return Response.status(200).entity(parallelScatterGatherQueryHelper.findStatesByStatus(stateMachineName, fromTimestamp, toTimestamp, stateName, statuses)).build();
    }

    /**
     * This api unsidelines a single state and triggers its execution.
     *
     * @param stateMachineId
     * @param stateId
     * @return
     */
    @PUT
    @Path("/{stateMachineId}/{stateId}/unsideline")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public Response unsidelineState(@PathParam("stateMachineId") String stateMachineId, @PathParam("stateId") Long stateId) {
        this.workFlowExecutionController.unsidelineState(stateMachineId, stateId);

        return Response.status(Response.Status.ACCEPTED).build();
    }

    @PUT
    @Path("/{stateMachineId}/cancel")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public Response cancelWorkflow(@PathParam("stateMachineId") String stateMachineId,
                                   @QueryParam("searchField") String searchField) {

        StateMachine stateMachine = null;
        stateMachine = stateMachinesDAO.findById(stateMachineId);

        if (stateMachine == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("State machine with id: " + stateMachineId + " not found").build();
        }

        workFlowExecutionController.cancelWorkflow(stateMachine);

        return Response.status(Response.Status.ACCEPTED).build();
    }

    @GET
    @Path("/{stateMachineId}/info")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public Response getStateMachine(@PathParam("stateMachineId") String stateMachineId,
                                    @QueryParam("searchField") String searchField) {
        StateMachine stateMachine = null;
        stateMachine = stateMachinesDAO.findById(stateMachineId);

        if (stateMachine == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("State machine with id: " + stateMachineId + " not found").build();
        }

        List<Event> events = eventsDAO.findBySMInstanceId(stateMachine.getId());
        List<AuditRecord> auditRecords = auditDAO.findBySMInstanceId(stateMachine.getId());

        Map<String, Object> stateMachineInfo = objectMapper.convertValue(stateMachine, Map.class);
        stateMachineInfo.put("events", events);
        stateMachineInfo.put("auditrecords", auditRecords);

        return Response.status(Response.Status.OK).entity(stateMachineInfo).build();
    }

    /**
     * Retrieves fsm graph data based on FSM Id or correlation id
     */
    private FsmGraph getGraphData(String machineId) throws IOException {
        String fsmId = machineId;

        StateMachine stateMachine;
        stateMachine = stateMachinesDAO.findById(fsmId);

        if (stateMachine == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("State machine with Id: " + machineId + " not found").build());
        }
        final FsmGraph fsmGraph = new FsmGraph();

        List<Long> erroredStateIds = new LinkedList<>();

        for (State state : stateMachine.getStates()) {
            if (state.getStatus() == Status.errored || state.getStatus() == Status.sidelined) {
                erroredStateIds.add(state.getId());
            }
        }

        Collections.sort(erroredStateIds);
        fsmGraph.setErroredStateIds(erroredStateIds);
        fsmGraph.setStateMachineId(stateMachine.getId());
        fsmGraph.setFsmVersion(stateMachine.getVersion());
        fsmGraph.setFsmName(stateMachine.getName());

        Map<String, Event> stateMachineEvents = eventsDAO.findBySMInstanceId(stateMachine.getId()).stream().collect(
                Collectors.<Event, String, Event>toMap(Event::getName, (event -> event)));
        Set<String> allOutputEventNames = new HashSet<>();

        final RAMContext ramContext = new RAMContext(System.currentTimeMillis(), null, stateMachine);
        /* After this operation, we'll have nodes for each state and its corresponding output event along with the output event's dependencies mapped out*/
        for (State state : stateMachine.getStates()) {
            final FsmGraphVertex vertex = new FsmGraphVertex(state.getId(), this.getStateDisplayName(state.getName()), state.getStatus().name());
            if (state.getOutputEvent() != null) {
                EventDefinition eventDefinition = objectMapper.readValue(state.getOutputEvent(), EventDefinition.class);
                final Event outputEvent = stateMachineEvents.get(eventDefinition.getName());
                fsmGraph.addVertex(vertex,
                        new FsmGraphEdge(getEventDisplayName(outputEvent.getName()), outputEvent.getStatus().name(), outputEvent.getEventSource(), outputEvent.getEventData(), outputEvent.getUpdatedAt()));
                final Set<State> dependantStates = ramContext.getDependantStates(outputEvent.getName());
                dependantStates.forEach((aState) -> fsmGraph.addOutgoingEdge(vertex, aState.getId()));
                allOutputEventNames.add(outputEvent.getName()); // we collect all output event names and use them below.
            } else {
                fsmGraph.addVertex(vertex,
                        new FsmGraphEdge(null, null, null, null, null));
            }
        }

        /* Handle states with no dependencies, i.e the states that can be triggered as soon as we execute the state machine */
        final Set<State> initialStates = ramContext.getInitialStates(Collections.emptySet());// hackety hack.  We're fooling the context to give us only events that depend on nothing
        if (!initialStates.isEmpty()) {
            final FsmGraphEdge initEdge = new FsmGraphEdge(TRIGGER, Event.EventStatus.triggered.name(), TRIGGER, null, null);
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
            final FsmGraphEdge initEdge = new FsmGraphEdge(this.getEventDisplayName(workflowTriggeredEventName), correspondingEvent.getStatus().name(), correspondingEvent.getEventSource(), correspondingEvent.getEventData(), correspondingEvent.getUpdatedAt());
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
                ), " ");
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
