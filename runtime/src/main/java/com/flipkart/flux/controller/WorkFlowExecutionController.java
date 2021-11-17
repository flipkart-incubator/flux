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

package com.flipkart.flux.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.api.*;
import com.flipkart.flux.api.core.TaskExecutionMessage;
import com.flipkart.flux.dao.iface.AuditDAO;
import com.flipkart.flux.dao.iface.EventsDAO;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.dao.iface.StateTraversalPathDAO;
import com.flipkart.flux.dao.iface.StatesDAO;
import com.flipkart.flux.domain.*;
import com.flipkart.flux.domain.Status;
import com.flipkart.flux.exception.IllegalEventException;
import com.flipkart.flux.exception.UnknownStateMachine;
import com.flipkart.flux.impl.RAMContext;
import com.flipkart.flux.impl.message.TaskAndEvents;
import com.flipkart.flux.metrics.iface.MetricsClient;
import com.flipkart.flux.persistence.DataSourceType;
import com.flipkart.flux.persistence.SelectDataSource;
import com.flipkart.flux.persistence.Storage;
import com.flipkart.flux.representation.ClientElbPersistenceService;
import com.flipkart.flux.task.redriver.RedriverRegistry;
import com.flipkart.flux.taskDispatcher.ExecutionNodeTaskDispatcher;
import com.flipkart.flux.utils.LoggingUtils;
import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.*;


/**
 * <code>WorkFlowExecutionController</code> controls the execution flow of a given state machine
 *
 * @author shyam.akirala
 */
@Singleton
public class WorkFlowExecutionController {

    /**
     * Logger instance for this class
     */
    private static final Logger logger = LogManager.getLogger(WorkFlowExecutionController.class);

    /**
     * FSM and Events DAOs
     */
    private StateMachinesDAO stateMachinesDAO;
    private EventsDAO eventsDAO;

    /**
     * The DAO for Task related DB operations
     */
    private StatesDAO statesDAO;

    /**
     * The DAO for AuditRecord related DB operations
     */
    private AuditDAO auditDAO;

    /**
     * The DAO for StateTraversalPath related DB operations
     */
    private StateTraversalPathDAO stateTraversalPathDAO;

    /*
     * Connector to forward the TaskAndEvents akka Message to client Remote Machine For Execution
     */
    private ExecutionNodeTaskDispatcher executionNodeTaskDispatcher;
    /**
     * The Redriver Registry for driving stalled Tasks
     */
    private RedriverRegistry redriverRegistry;

    /**
     * Metrics client for keeping track of task metrics
     */
    private MetricsClient metricsClient;

    /**
     * ObjectMapper instance to be used for all purposes in this class
     */
    private ObjectMapper objectMapper;

    /**
     * ClientElbPersistenceService to retrieve ClientElb URL from ClientElbDAO.
     * This service searches in in-memory cache first, in case of miss hits DAO/DB layer.
     * ClientElbUrl is the one where a particular state machines states is supposed to be executed.
     */
    private ClientElbPersistenceService clientElbPersistenceService;


    /**
     * Constructor for this class
     */
    @Inject
    public WorkFlowExecutionController(EventsDAO eventsDAO, StateMachinesDAO stateMachinesDAO,
                                       StatesDAO statesDAO, AuditDAO auditDAO,
                                       StateTraversalPathDAO stateTraversalPathDAO,
                                       ExecutionNodeTaskDispatcher executionNodeTaskDispatcher,
                                       RedriverRegistry redriverRegistry, MetricsClient metricsClient,
                                       ClientElbPersistenceService clientElbPersistenceService) {
        this.eventsDAO = eventsDAO;
        this.stateMachinesDAO = stateMachinesDAO;
        this.statesDAO = statesDAO;
        this.auditDAO = auditDAO;
        this.stateTraversalPathDAO = stateTraversalPathDAO;
        this.executionNodeTaskDispatcher = executionNodeTaskDispatcher;
        this.redriverRegistry = redriverRegistry;
        this.metricsClient = metricsClient;
        this.objectMapper = new ObjectMapper();
        this.clientElbPersistenceService = clientElbPersistenceService;
    }

    /**
     * Perform init operations on a state machine and starts execution of states which are not dependant on any events.
     *
     * @param stateMachine
     * @return List of states that do not have any event dependencies on them
     */
    public Set<State> initAndStart(StateMachine stateMachine) {

        //create context and dependency graph
        Context context = new RAMContext(System.currentTimeMillis(), null, stateMachine); //TODO: set context id, should we need it ?

        final List<String> triggeredEvents = eventsDAO.findTriggeredOrCancelledEventsNamesBySMId(stateMachine.getId());
        Set<State> initialStates = context.getInitialStates(new HashSet<>(triggeredEvents));
        executeStates(stateMachine, initialStates, false);

        return initialStates;
    }

    /**
     * Updates task status and retrieves the states which are dependant on this event and starts the execution of eligible states (whose all dependencies are met).
     *
     * @param stateMachine
     * @param eventAndExecutionData
     */
    public void updateTaskStatusAndPostEvent(StateMachine stateMachine, EventAndExecutionData eventAndExecutionData) {
        Event event = updateTaskStatusAndPersistEvent(stateMachine, eventAndExecutionData);
        processEvent(event, stateMachine.getId());
    }

    /**
     * Updates task status and persists the event in a single transaction. Keeping this method as protected so that guice can intercept it.
     *
     * @param stateMachine
     * @param eventAndExecutionData
     */
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    protected Event updateTaskStatusAndPersistEvent(StateMachine stateMachine, EventAndExecutionData eventAndExecutionData) {
        updateTaskStatus(stateMachine.getId(), eventAndExecutionData.getExecutionUpdateData().getTaskId(),
                eventAndExecutionData.getExecutionUpdateData().getTaskExecutionVersion(),
                eventAndExecutionData.getExecutionUpdateData());
        return persistEvent(stateMachine.getId(), eventAndExecutionData.getEventData());
    }

    /**
     * Updates task status and cancels paths which are dependant on the current event. After the cancellation of path, executes the states which can be executed.
     *
     * @param stateMachineId
     * @param eventAndExecutionData
     */
    public void updateTaskStatusAndHandlePathCancellation(String stateMachineId, EventAndExecutionData eventAndExecutionData) {
        Set<State> executableStates = updateTaskStatusAndCancelPath(stateMachineId, eventAndExecutionData);
        logger.info("Path cancellation is done for state machine: {} event: {} which has come from task: {}",
                stateMachineId, eventAndExecutionData.getEventData().getName(), eventAndExecutionData.getExecutionUpdateData().getTaskId());
        StateMachine stateMachine = stateMachinesDAO.findById(stateMachineId);
        executeStates(stateMachine, executableStates, false);
    }

    /**
     * Updates task status and cancels paths which are dependant on the current event
     *
     * @param stateMachineId
     * @param eventAndExecutionData
     * @return executable states after cancellation
     */
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    protected Set<State> updateTaskStatusAndCancelPath(String stateMachineId, EventAndExecutionData eventAndExecutionData) {
        updateTaskStatus(stateMachineId, eventAndExecutionData.getExecutionUpdateData().getTaskId(),
                eventAndExecutionData.getExecutionUpdateData().getTaskExecutionVersion(),
                eventAndExecutionData.getExecutionUpdateData());
        return cancelPath(stateMachineId, eventAndExecutionData.getEventData());
    }

    /**
     * Cancels paths which are dependant on the current event, and returns set of states which can be executed after the cancellation.
     *
     * @param stateMachineId
     * @param eventData
     * @return executable states after cancellation
     */
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    protected Set<State> cancelPath(String stateMachineId, EventData eventData) {
        Set<State> executableStates = new HashSet<>();
        StateMachine stateMachine = stateMachinesDAO.findById(stateMachineId);
        //create context and dependency graph
        Context context = new RAMContext(System.currentTimeMillis(), null, stateMachine);

        // get all events of this state machine in map<eventName, eventStatus> with lock
        Map<String, Event.EventStatus> eventStatusMap = eventsDAO.getAllEventsNameAndStatus(stateMachine.getId(), true);

        // the events which need to marked as cancelled
        Queue<String> cancelledEvents = new LinkedList<>();

        // add the current event
        cancelledEvents.add(eventData.getName());

        // until the cancelled events is empty
        while (!cancelledEvents.isEmpty()) {

            // get event from queue
            String eventName = cancelledEvents.poll();

            // mark the event as cancelled in DB in local map
            eventsDAO.markEventAsCancelled(stateMachine.getId(), eventName);
            eventStatusMap.put(eventName, Event.EventStatus.cancelled);

            // fetch all states which are dependant on the current event
            final Set<State> dependantStates = context.getDependantStates(eventName);

            // for each state
            for (State state : dependantStates) {

                // fetch all event names this state is dependant on
                List<String> dependencies = state.getDependencies();

                boolean allCancelled = true;
                boolean allMet = true;
                for (String dependency : dependencies) {
                    if (eventStatusMap.get(dependency) != Event.EventStatus.cancelled) {
                        allCancelled = false;
                    }
                    if (!(eventStatusMap.get(dependency) == Event.EventStatus.cancelled || eventStatusMap.get(dependency) == Event.EventStatus.triggered)) {
                        allMet = false;
                    }
                }

                // if all dependencies are in cancelled state, then add the output event of the state to cancelled events and mark state as cancelled
                if (allCancelled) {
                    statesDAO.updateStatus(state.getStateMachineId(), state.getId(), Status.cancelled);
                    auditDAO.create(state.getStateMachineId(), new AuditRecord(stateMachine.getId(), state.getId(), state.getAttemptedNoOfRetries(), Status.cancelled, null, null));
                    EventDefinition eventDefinition = null;
                    if (state.getOutputEvent() != null) {
                        try {
                            eventDefinition = objectMapper.readValue(state.getOutputEvent(), EventDefinition.class);
                        } catch (IOException ex) {
                            throw new RuntimeException("Error occurred while deserializing task outputEvent for stateMachineInstanceId: " + stateMachine.getId() + " taskId: " + state.getId());
                        }
                        cancelledEvents.add(eventDefinition.getName());
                    }
                } else if (allMet) {
                    // if all dependencies are in cancelled or triggered state, then execute the state
                    executableStates.add(state);
                }
            }
        }
        return executableStates;
    }

    /**
     * This is called when an event is received with cancelled status. This cancels the particular path in state machine DAG.
     *
     * @param stateMachineId
     * @param eventData
     */
    public void handlePathCancellation(String stateMachineId, EventData eventData) {
        Set<State> executableStates = cancelPath(stateMachineId, eventData);
        logger.info("Path cancellation is done for state machine: {} event: {}",
                stateMachineId, eventData.getName());
        executeStates(stateMachinesDAO.findById(stateMachineId), executableStates, false);
    }

    /**
     * Helper method to JSON serialize the output event
     */
    private String getOutputEventName(String outputEvent) throws java.io.IOException {
        return outputEvent != null ? objectMapper.readValue(outputEvent, EventDefinition.class).getName() : null;
    }

    /**
     * TODO : Test cases needs to be added.
     * TODO : Use dependantStates as a tuple/pair of composite key <stateId, stateMachineId>
     * TODO : Rephrase this description
     * Retrieves the states which are dependant on this replay event and starts the execution of eligible states (replayable is true).
     * 0. Verify eligible dependant state(replayable is true) on this replay event.
     * 1. Retrieves set of states and events occurring in the topological path with triggering of this replay event using BFS.
     * 2. Process and persist replay event
     * 3. Submit replay event to execute eligible dependent states(replayable is true) on this replay event.
     *
     * @param eventData
     * @param stateMachine
     */
    public void postReplayEvent(EventData eventData, StateMachine stateMachine) throws IllegalEventException, IOException {

        Context context = new RAMContext(System.currentTimeMillis(), null, stateMachine);

        // TODO : Add a check on client side so as not to allow a replay event being a dependency of 2 or more states.
        //Get the dependant state on this replay event.
        Long dependantStateId = statesDAO.findStateByDependentReplayEvent(stateMachine.getId(), eventData.getName());
        State dependantStateOnReplayEvent = statesDAO.findById(stateMachine.getId(), dependantStateId);
        // TODO : Add null check for dependantStateOnReplayEvent
        logger.info("This state {} depends on replay event {}", dependantStateOnReplayEvent.getName(), eventData.getName());
        if(!dependantStateOnReplayEvent.getReplayable() || dependantStateOnReplayEvent.getStatus() != Status.completed) {
            throw new IllegalEventException("Dependant state:"+ dependantStateOnReplayEvent.getName() +" with stateMachineId:" +
                    stateMachine.getId() +" and replay event:"+ eventData.getName() + " is not replayable.");
        }

        // Build states set and events names list in topological path of replay event
        // All these states and events will be marked as invalid.
        // TODO: Need to add test cases for this.
        Set<State> dependantStates = new HashSet<>();
        List<String> dependantEvents = new ArrayList<>();

        // add initial state dependant on replay event
        dependantStates.add(dependantStateOnReplayEvent);

        // add initial state's output event
        String initialStateOutputEventName = getOutputEventName(dependantStateOnReplayEvent.getOutputEvent());
        dependantEvents.add(initialStateOutputEventName);

        // add previous executionVersion replay event to be marked as invalid.
        dependantEvents.add(eventData.getName());

        // Using BFS, for each state in State Machine, find path between initial dependant state -> current state
        Optional<StateTraversalPath> traversalPathStates = stateTraversalPathDAO.findById(
                stateMachine.getId(), dependantStateId);

        if(traversalPathStates.isPresent()) {

            List<Long> nextDependentStateIds = traversalPathStates.get().getNextDependentStates();
            for (Long stateId : nextDependentStateIds) {
                String outputEvent = statesDAO.findById(stateMachine.getId(), stateId).getOutputEvent();

                if(outputEvent != null) {
                    String outputEventName = getOutputEventName(outputEvent);
                    if (!dependantEvents.contains(outputEventName.toString()))
                        dependantEvents.add(outputEventName);
                }
            }
            logger.info("Building set of states and list of events in dependancy path of replay event:{} for SMId:{} is done.",
                    eventData.getName(), stateMachine.getId());

            //Remove external events from being marked as invalid.
            List<String> tempDependantEvents = new ArrayList<>();
            tempDependantEvents.addAll(dependantEvents);
            for (String eventName : tempDependantEvents) {
                // Using default execution version "0L" because it's the only executionVersion which will have all
                // event's entries
                if (eventsDAO.findByStateMachineIdAndExecutionVersionAndName(stateMachine.getId(), eventName, 0L)
                        .getEventSource().equals("external")) {
                    dependantEvents.remove(eventName);
                }
            }
            logger.info("Set of States:{} , List of Events:{}", dependantStates, dependantEvents);

            // TODO : Handle error responses
            Event currentEvent = persistAndProcessReplayEvent(eventData, dependantStates, dependantEvents, stateMachine.getId());

            Set<State> executableStates = new HashSet<>();

            // TODO : Need to call execute state from outside this transaction. We can register it in redriver.
            dependantStateOnReplayEvent.setStatus(Status.initialized);
            executableStates.add(dependantStateOnReplayEvent);
            executeStates(stateMachine, executableStates, currentEvent, false);
        } else {
            throw new IllegalEventException("No traversal path found for replayable state id:" + dependantStateId +
                    " and stateMachineId: " + stateMachine.getId());
        }
    }

    /**
     * TODO : Rephrase this description
     * In a Transaction :
     *  1. Increment, update and read executionVersion for this State Machine.
     *  2. Mark all states and Update executionVersion for all states(marked as invalid) retrieved in step 1.
     *  3. Mark dependant events as invalid.
     *  4. Create new event entries in pending status including replay event as triggered containing event data with executionVersion
     *     read in step 2.
     * @param eventData
     * @param dependantStates
     * @param dependantEvents
     * @param stateMachineId
     */
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public Event persistAndProcessReplayEvent(EventData eventData, Set<State> dependantStates,
                                              List<String> dependantEvents, String stateMachineId) {

        // TODO : This will be updated to return updated value in same call. Need to add tests for it.
        Long smExecutionVersion = stateMachinesDAO.findById(stateMachineId).getExecutionVersion() + 1;
        stateMachinesDAO.incrementExecutionVersion(stateMachineId, smExecutionVersion);

        ArrayList<State> states = new ArrayList<>(dependantStates);
        statesDAO.updateStatus(stateMachineId,states,Status.initialized);
        statesDAO.updateExecutionVersion(stateMachineId,states,smExecutionVersion);

        // TODO : This should be moved to EventPersistenceService
        eventsDAO.markEventsAsInvalid(stateMachineId, dependantEvents);

        // Remove replay event. Purpose was to mark all it's previous version invalid.
        dependantEvents.remove(eventData.getName());

        for (String eventName : dependantEvents) {
            // To retrieve event meta data (type) for creating new event with new smExecutionVersion
            // Retrieving for executionVersion 0 is fine because type of event doesn't change with executionVersion
            Event currentEvent = eventsDAO.findByStateMachineIdAndExecutionVersionAndName(stateMachineId, eventName, 0L);
            Event event = new Event(currentEvent.getName(), currentEvent.getType(), Event.EventStatus.pending,
                    stateMachineId, null, null, smExecutionVersion);
            eventsDAO.create(stateMachineId, event);
        }

        //Persist replay event
        Event replayEvent = new Event(eventData.getName(), eventData.getType(), Event.EventStatus.triggered, stateMachineId,
                eventData.getData(), eventData.getEventSource(), smExecutionVersion);
        return eventsDAO.create(stateMachineId, replayEvent);
    }

    /**
     * Retrieves the states which are dependant on this event and starts the execution of eligible states (whose all dependencies are met).
     *
     * @param eventData
     * @param stateMachineInstanceId
     */
    public Set<State> postEvent(EventData eventData, String stateMachineInstanceId) {
        Event event = persistEvent(stateMachineInstanceId, eventData);
        return processEvent(event, stateMachineInstanceId);
    }

    /**
     * Persists Event data and changes event status
     *
     * @param stateMachineInstanceId
     * @param eventData
     * @return
     */
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public Event persistEvent(String stateMachineInstanceId, EventData eventData) {
        //update event's data and status
        Event event = eventsDAO.findByStateMachineIdAndExecutionVersionAndName(stateMachineInstanceId, eventData.getName(),
                eventData.getExecutionVersion());
        if (event == null)
            throw new IllegalEventException("Event with stateMachineId: " + stateMachineInstanceId + ", event name: " + eventData.getName() + " not found");
        event.setStatus(eventData.getCancelled() != null && eventData.getCancelled() ? Event.EventStatus.cancelled : Event.EventStatus.triggered);
        event.setEventData(eventData.getData());
        event.setEventSource(eventData.getEventSource());
        eventsDAO.updateEvent(event.getStateMachineInstanceId(), event);
        return event;
    }

    /**
     * Checks and triggers the states which are dependant on the current event
     *
     * @param event
     * @param stateMachineInstanceId
     * @return
     */
    public Set<State> processEvent(Event event, String stateMachineInstanceId) {
        //create context and dependency graph
        StateMachine stateMachine = null;
        stateMachine = stateMachinesDAO.findById(stateMachineInstanceId);
        if (stateMachine == null) {
            logger.error("stateMachine with id not found while processing event {} ", stateMachineInstanceId, event.getName());
            throw new RuntimeException("StateMachine with id " + stateMachineInstanceId + " not found while processing event " + event.getName());
        }
        Context context = new RAMContext(System.currentTimeMillis(), null, stateMachine); //TODO: set context id, should we need it ?

        //get the states whose dependencies are met
        final Set<State> dependantStates = context.getDependantStates(event.getName());
        logger.debug("These states {} depend on event {}", dependantStates, event.getName());
        Set<State> executableStates = getExecutableStates(dependantStates, stateMachine.getId());
        logger.debug("These states {} are now unblocked after event {}", executableStates, event.getName());
        //start execution of the above states
        executeStates(stateMachine, executableStates, event, false);

        return executableStates;
    }

    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public void updateTaskStatus(String machineId, Long stateId, Long taskExecutionVersion,
                                 ExecutionUpdateData executionUpdateData) {
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
            case unsidelined:
                updateStatus = com.flipkart.flux.domain.Status.sidelined;
                break;
        }
        metricsClient.markMeter(new StringBuilder().
                append("stateMachine.").
                append(executionUpdateData.getStateMachineName()).
                append(".task.").
                append(executionUpdateData.getTaskName()).
                append(".executionVersion.").
                append(executionUpdateData.getTaskExecutionVersion()).
                append(".status.").
                append(updateStatus.name()).
                toString());
        updateExecutionStatus(machineId, stateId, taskExecutionVersion, updateStatus,
                executionUpdateData.getRetrycount(),
                executionUpdateData.getCurrentRetryCount(), executionUpdateData.getErrorMessage(),
                executionUpdateData.isDeleteFromRedriver());
    }

    /**
     * Updates the execution status for the specified State machine's Task
     *
     * @param stateMachineId    the state machine identifier
     * @param taskId            the Task identifier
     * @param taskExecutionVersion  the Task Execution Version
     * @param status            the Status to be updated to
     * @param retryCount        the configured retry count for the task
     * @param currentRetryCount current retry count for the task
     * @param errorMessage      the error message in case task has failed
     */
    public void updateExecutionStatus(String stateMachineId, Long taskId, Long taskExecutionVersion, Status status,
                                      long retryCount, long currentRetryCount, String errorMessage,
                                      boolean deleteFromRedriver) {
        // TODO: Handle deletion from redriver when executionVersion is added to redriver table.
        // TODO: Once this check is handled in all it's callers, it can be removed from here.
        if(taskExecutionVersion == this.statesDAO.findById(stateMachineId, taskId).getExecutionVersion()) {
            this.statesDAO.updateStatus(stateMachineId, taskId, status);
            AuditRecord auditRecord = new AuditRecord(stateMachineId, taskId, currentRetryCount, status,
                    null, errorMessage);
            auditRecord.setTaskExecutionVersion(taskExecutionVersion);
            // TODO: Need to add dependent events for executed state in auditRecords.
            this.auditDAO.create(stateMachineId, auditRecord);
            if (deleteFromRedriver) {
                this.redriverRegistry.deRegisterTask(stateMachineId, taskId, taskExecutionVersion);
            }
        }
        else {
            logger.info("Input taskExecutionVersion: {} is invalid, update task execution status denied for taskId: {}," +
                    " stateMachineId: {}.", taskExecutionVersion, taskId, stateMachineId);
        }

    }


    /*
     *  Audit entry in AuditRecord.
     * Default values: [{machineId}, 0, 0, null, null, {EventUpdateAudit} String]
     */
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public void updateEventData(String machineId, EventData eventData) {
        persistEvent(machineId, eventData);
        String EventUdpateAudit = "Event data updated for event: " + eventData.getName();
        this.auditDAO.create(machineId, new AuditRecord(machineId, Long.valueOf(0), Long.valueOf(0),
                null, null, EventUdpateAudit));
        logger.info("Updated event data persisted for event: {} and stateMachineId: {}", eventData.getName(), machineId);
    }

    /**
     * Unsidelines a state and triggers its execution.
     *
     * @param stateMachineId
     * @param stateId
     */
    public void unsidelineState(String stateMachineId, Long stateId) throws UnknownStateMachine, IllegalStateException {
        State askedState = null;
        StateMachine stateMachine = retrieveStateMachine(stateMachineId);
        if (stateMachine == null)
            throw new UnknownStateMachine("State machine with id: " + stateMachineId + " not found");
        for (State state : stateMachine.getStates()) {
            if (Objects.equals(state.getId(), stateId)) {
                askedState = state;
                break;
            }
        }

        if (askedState == null) {
            throw new IllegalStateException("State with the asked id: " + stateId +
                    " not found in stateMachine with id: " + stateMachineId);
        }

        Set<State> checkExecutableState = new HashSet<>();
        checkExecutableState.add(askedState);
        if (getExecutableStates(checkExecutableState, stateMachineId).isEmpty()) {
            logger.error("Cannot unsideline state: {}, at least one of the dependent events is in pending status.",
                    askedState.getName());
            return;
        } else if (askedState.getStatus() == Status.initialized || askedState.getStatus() == Status.sidelined
                || askedState.getStatus() == Status.errored) {
            askedState.setStatus(Status.unsidelined);
            askedState.setAttemptedNoOfRetries(0L);
            statesDAO.updateState(stateMachineId, askedState);
            executeStates(stateMachine, Sets.newHashSet(Arrays.asList(askedState)), false);
        }
    }

    /**
     * Increments the no. of execution retries for the specified State machine's Task
     *
     * @param stateMachineId the state machine identifier
     * @param taskId         the Task identifier
     */
    public void incrementExecutionRetries(String stateMachineId, Long taskId, Long taskExecutionVersion) {
        if(taskExecutionVersion == this.statesDAO.findById(stateMachineId, taskId).getExecutionVersion()) {
            this.statesDAO.incrementRetryCount(stateMachineId, taskId);
        }
        else {
            logger.info("Input taskExecutionVersion: {} is invalid, increment execution retries denied for taskId: {}," +
                    " stateMachineId: {}.", taskExecutionVersion, taskId, stateMachineId);
        }
    }

    /**
     * Wrapper function on {@link #executeStates(StateMachine, Set, Event, boolean)} which triggers the execution of executableStates using Akka router.
     */
    private void executeStates(StateMachine stateMachine, Set<State> executableStates, boolean redriverTriggered) {
        executeStates(stateMachine, executableStates, null, redriverTriggered);
    }

    /**
     * Triggers the execution of executableStates using Akka router
     *
     * @param stateMachine     the state machine
     * @param executableStates states whose all dependencies are met
     */
    private void executeStates(StateMachine stateMachine, Set<State> executableStates, Event currentEvent,
                               boolean redriverTriggered) {
        try {
            LoggingUtils.registerStateMachineIdForLogging(stateMachine.getId().toString());
            executableStates.forEach((state -> {
                // trigger execution if state is not in completed|cancelled|invalid state
                if (!(state.getStatus() == Status.completed || state.getStatus() == Status.cancelled ||
                state.getStatus() == Status.invalid)) {

                    List<EventData> eventDatas;
                    // Reading only replay event's data, ignoring all other dependant event's data.
                    if(currentEvent != null && currentEvent.getEventSource() != null &&
                            currentEvent.getEventSource().equalsIgnoreCase("replay")) {
                        eventDatas = Collections.singletonList(new EventData(currentEvent.getName(),
                                currentEvent.getType(), currentEvent.getEventData(), currentEvent.getEventSource(),
                                currentEvent.getExecutionVersion()));
                    }
                    // If the state is dependant on only one event, that would be the event which came now, in that case don't make a call to DB
                    else if (currentEvent != null && state.getDependencies() != null && state.getDependencies().size() == 1
                            && currentEvent.getName().equals(state.getDependencies().get(0))) {
                        eventDatas = Collections.singletonList(new EventData(currentEvent.getName(),
                                currentEvent.getType(), currentEvent.getEventData(), currentEvent.getEventSource(),
                                currentEvent.getExecutionVersion()));
                    } else {
                        eventDatas = eventsDAO.findByEventNamesAndSMId(stateMachine.getId(), state.getDependencies());
                    }
                    final TaskAndEvents msg = new TaskAndEvents(state.getName(), state.getTask(), state.getId(),
                            state.getExecutionVersion(), eventDatas.toArray(new EventData[]{}),
                            stateMachine.getId(), stateMachine.getName(), state.getOutputEvent(),
                            state.getRetryCount(), state.getAttemptedNoOfRetries());
                    if (state.getStatus().equals(Status.initialized) || state.getStatus().equals(Status.unsidelined)) {
                        msg.setFirstTimeExecution(true);
                    }

                    // register the Task with the redriver
                    // Delay between retires is exponential (2, 4, 8, 16, 32.... seconds) as seen in AkkaTask.
                    // Redriver interval is set as 2 x ( 2^(retryCount+1) x 1s + (retryCount+1) x timeout)
                    long redriverInterval;
                    if (redriverTriggered && state.getStatus() == Status.initialized) {
                        redriverInterval = 2 * ((int) Math.pow(2, 7) * 1000);
                    }
                    else {
                        redriverInterval = 2 * ((int) Math.pow(2, state.getRetryCount() + 1) * 1000 + (state.getRetryCount() + 1) * state.getTimeout());
                    }
                    this.redriverRegistry.registerTask(state.getId(), state.getStateMachineId(), redriverInterval, state.getExecutionVersion());

                    // Send the message to Akka Router
                    String taskName = state.getTask();
                    String routerName = getRouterName(taskName);
                    /*
                     *  sending message to remote Execution Node for execution
                     *  Endpoint to be fetched from Cache or DB
                     * */
                    TaskExecutionMessage taskExecutionMessage = new TaskExecutionMessage(routerName, msg);

                    String clientElbUrl = clientElbPersistenceService.findByIdClientElb(stateMachine.getClientElbId());
                    String endPoint = clientElbUrl + "/api/execution";
                    long startTime = System.currentTimeMillis();
                    int statusCode = executionNodeTaskDispatcher.forwardExecutionMessage(endPoint, taskExecutionMessage);
                    long finishTime = System.currentTimeMillis();
                    if (statusCode == 202) {
                        logger.info("Successfully forwarded the taskExecutionMsg for smId:{} taskId:{} for" +
                                        " remoteExecution to host {} took {}ms", msg.getStateMachineId(),
                                msg.getTaskId(), endPoint, finishTime - startTime);
                    } else {
                        logger.error("Failed to succesfully send task for Execution smId:{} taskId:{}," +
                                        " should be retried by Redriver after {} ms.",
                                msg.getStateMachineId(), msg.getTaskId(), redriverInterval);

                    }

                } else {
                    logger.info("State machine: {} Task: {} execution request got discarded as the task is {}", state.getStateMachineId(), state.getId(), state.getStatus());
                }
            }));
        } finally {
            LoggingUtils.deRegisterStateMachineIdForLogging();
        }

    }

    private StateMachine retrieveStateMachine(String stateMachineInstanceId) {
        return stateMachinesDAO.findById(stateMachineInstanceId);
    }

    /**
     * Given states which are dependant on a particular event, returns which of them can be executable (states whose all dependencies are met)
     *
     * @param dependantStates
     * @param stateMachineInstanceId
     * @return executableStates
     */
    private Set<State> getExecutableStates(Set<State> dependantStates, String stateMachineInstanceId) {
        // TODO : states can get triggered twice if we receive all their dependent events at roughly the same time.
        Set<State> executableStates = new HashSet<>();
        Set<String> receivedEvents = new HashSet<>(eventsDAO.findTriggeredOrCancelledEventsNamesBySMId(stateMachineInstanceId));

        // Since replay event is optional, not required to check it's eventStatus. It's considered as dependency met.
        Set<String> replayEvents = new HashSet<>(eventsDAO.findReplayEventsNamesBySMId(stateMachineInstanceId));
        receivedEvents.addAll(replayEvents);
        // for each state
        // 1. get the dependencies (events)
        // 2. check whether all events are in triggered state
        // 3. if all events are in triggered status, then add this state to executableStates
        dependantStates.stream().filter(state1 -> state1.isDependencySatisfied(receivedEvents)).forEach(executableStates::add);
        return executableStates;
    }

    /**
     * Performs task execution if the task is stalled and no.of retries are not exhausted
     */
    public void redriveTask(String machineId, Long taskId, Long executionVersion) {
        try {
            State state = statesDAO.findById(machineId, taskId);

            if (state != null && isTaskRedrivable(state.getStatus()) && state.getAttemptedNoOfRetries() <= state.getRetryCount()) {
                StateMachine stateMachine = retrieveStateMachine(state.getStateMachineId());
                LoggingUtils.registerStateMachineIdForLogging(stateMachine.getId());
                logger.info("Redriving a task with Id: {} and execution version: {} for state machine: {}", state.getId(), executionVersion, state.getStateMachineId());
                executeStates(stateMachine, Collections.singleton(state), true);
            } else {
                //cleanup the tasks which can't be redrived from redriver db
                this.redriverRegistry.deRegisterTask(machineId, taskId, executionVersion);
            }
        } finally {
            LoggingUtils.deRegisterStateMachineIdForLogging();
        }
    }

    /**
     * Returns whether a task is redrivable based on it's status.
     */
    private boolean isTaskRedrivable(Status taskStatus) {
        return !(taskStatus.equals(Status.completed) ||
                taskStatus.equals(Status.sidelined) ||
                taskStatus.equals(Status.cancelled));
    }

    /**
     * Cancels the corresponding state machine (marks statemachine's and its states' statuses as cancelled)
     */
    public void cancelWorkflow(StateMachine stateMachine) {
        stateMachinesDAO.updateStatus(stateMachine.getId(), StateMachineStatus.cancelled);
        stateMachine.getStates().stream().filter(state -> state.getStatus() == Status.initialized ||
                state.getStatus() == Status.errored || state.getStatus() == Status.sidelined).forEach(state -> {
            this.statesDAO.updateStatus(stateMachine.getId(), state.getId(), Status.cancelled);
            this.auditDAO.create(stateMachine.getId(), new AuditRecord(stateMachine.getId(), state.getId(), state.getAttemptedNoOfRetries(), Status.cancelled, null, null));
        });
    }

    /*
     * Returns a routerName for the task, given TaskName
     *
     * */
    public static String getRouterName(String taskName) {
        int secondUnderscorePosition = taskName.indexOf('_', taskName.indexOf('_') + 1);
        String routerName = taskName.substring(0, secondUnderscorePosition == -1 ? taskName.length() : secondUnderscorePosition);
        return routerName;
    }
}