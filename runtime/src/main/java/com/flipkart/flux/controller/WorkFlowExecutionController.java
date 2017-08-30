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

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.api.EventAndExecutionData;
import com.flipkart.flux.api.EventData;
import com.flipkart.flux.api.EventDefinition;
import com.flipkart.flux.api.ExecutionUpdateData;
import com.flipkart.flux.dao.iface.AuditDAO;
import com.flipkart.flux.dao.iface.EventsDAO;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.dao.iface.StatesDAO;
import com.flipkart.flux.domain.*;
import com.flipkart.flux.exception.IllegalEventException;
import com.flipkart.flux.exception.UnknownStateMachine;
import com.flipkart.flux.impl.RAMContext;
import com.flipkart.flux.impl.message.TaskAndEvents;
import com.flipkart.flux.impl.task.registry.RouterRegistry;
import com.flipkart.flux.metrics.iface.MetricsClient;
import com.flipkart.flux.task.redriver.RedriverRegistry;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.*;

import static com.flipkart.flux.Constants.STATE_MACHINE_ID;
import static com.flipkart.flux.Constants.TASK_ID;

/**
 * <code>WorkFlowExecutionController</code> controls the execution flow of a given state machine
 * @author shyam.akirala
 */
@Singleton
public class WorkFlowExecutionController {

    /** Logger instance for this class*/
    private static final Logger logger = LoggerFactory.getLogger(WorkFlowExecutionController.class);

    /** FSM and Events DAOs*/
    private StateMachinesDAO stateMachinesDAO;
    private EventsDAO eventsDAO;

    /** The DAO for Task related DB operations*/
    private StatesDAO statesDAO;

    /** The DAO for AuditRecord related DB operations*/
    private AuditDAO auditDAO;

    /** The Router registry*/
    private RouterRegistry routerRegistry;

    /** The Redriver Registry for driving stalled Tasks*/
    private RedriverRegistry redriverRegistry;

    /** Metrics client for keeping track of task metrics*/
    private MetricsClient metricsClient;

    /** ObjectMapper instance to be used for all purposes in this class */
    private ObjectMapper objectMapper;

    /** Constructor for this class */
    @Inject
    public WorkFlowExecutionController(EventsDAO eventsDAO, StateMachinesDAO stateMachinesDAO,
                                       StatesDAO statesDAO, AuditDAO auditDAO, RouterRegistry routerRegistry,
                                       RedriverRegistry redriverRegistry, MetricsClient metricsClient) {
        this.eventsDAO = eventsDAO;
        this.stateMachinesDAO = stateMachinesDAO;
        this.statesDAO = statesDAO;
        this.auditDAO = auditDAO;
        this.routerRegistry = routerRegistry;
        this.redriverRegistry = redriverRegistry;
        this.metricsClient = metricsClient;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Perform init operations on a state machine and starts execution of states which are not dependant on any events.
     * @param stateMachine
     * @return List of states that do not have any event dependencies on them
     */
    public Set<State> initAndStart(StateMachine stateMachine) {

        //create context and dependency graph
        Context context = new RAMContext(System.currentTimeMillis(), null, stateMachine); //TODO: set context id, should we need it ?

        final List<String> triggeredEvents = eventsDAO.findTriggeredOrCancelledEventsNamesBySMId(stateMachine.getId());
        Set<State> initialStates = context.getInitialStates(new HashSet<>(triggeredEvents));
        executeStates(stateMachine, initialStates);

        return initialStates;
    }

    /**
     * Updates task status and retrieves the states which are dependant on this event and starts the execution of eligible states (whose all dependencies are met).
     * @param stateMachine
     * @param eventAndExecutionData
     */
    public void updateTaskStatusAndPostEvent(StateMachine stateMachine, EventAndExecutionData eventAndExecutionData) {
        Event event = updateTaskStatusAndPersistEvent(stateMachine, eventAndExecutionData);
        processEvent(event, stateMachine);
    }

    /**
     * Updates task status and persists the event in a single transaction. Keeping this method as protected so that guice can intercept it.
     * @param stateMachine
     * @param eventAndExecutionData
     */
    @Transactional
    protected Event updateTaskStatusAndPersistEvent(StateMachine stateMachine, EventAndExecutionData eventAndExecutionData) {
        updateTaskStatus(stateMachine.getId(), eventAndExecutionData.getExecutionUpdateData().getTaskId(), eventAndExecutionData.getExecutionUpdateData());
        return persistEvent(stateMachine, eventAndExecutionData.getEventData());
    }

    /**
     * Updates task status and cancels paths which are dependant on the current event. After the cancellation of path, executes the states which can be executed.
     * @param stateMachine
     * @param eventAndExecutionData
     */
    public void updateTaskStatusAndHandlePathCancellation(StateMachine stateMachine, EventAndExecutionData eventAndExecutionData) {
        Set<State> executableStates = updateTaskStatusAndCancelPath(stateMachine, eventAndExecutionData);
        executeStates(stateMachine, executableStates);
    }

    /**
     * Updates task status and cancels paths which are dependant on the current event
     * @param stateMachine
     * @param eventAndExecutionData
     * @return executable states after cancellation
     */
    @Transactional
    protected Set<State> updateTaskStatusAndCancelPath(StateMachine stateMachine, EventAndExecutionData eventAndExecutionData) {
        updateTaskStatus(stateMachine.getId(), eventAndExecutionData.getExecutionUpdateData().getTaskId(), eventAndExecutionData.getExecutionUpdateData());
        return cancelPath(stateMachine, eventAndExecutionData.getEventData());
    }

    /**
     * Cancels paths which are dependant on the current event, and returns set of states which can be executed after the cancellation.
     * @param stateMachine
     * @param eventData
     * @return executable states after cancellation
     */
    @Transactional
    protected Set<State> cancelPath(StateMachine stateMachine, EventData eventData) {
        Set<State> executableStates = new HashSet<>();

        //create context and dependency graph
        Context context = new RAMContext(System.currentTimeMillis(), null, stateMachine);

        // get all events of this state machine in map<eventName, eventStatus> with lock
        Map<String, Event.EventStatus> eventStatusMap = eventsDAO.getAllEventsNameAndStatus(stateMachine.getId(), true);

        // the events which need to marked as cancelled
        Queue<String> cancelledEvents = new LinkedList<>();

        // add the current event
        cancelledEvents.add(eventData.getName());

        // until the cancelled events is empty
        while(!cancelledEvents.isEmpty()) {

            // get event from queue
            String eventName = cancelledEvents.poll();

            // mark the event as cancelled in DB in local map
            eventsDAO.markEventAsCancelled(stateMachine.getId(), eventName);
            eventStatusMap.put(eventName, Event.EventStatus.cancelled);

            // fetch all states which are dependant on the current event
            final Set<State> dependantStates = context.getDependantStates(eventName);

            // for each state
            for(State state : dependantStates) {

                // fetch all event names this state is dependant on
                List<String> dependencies = state.getDependencies();

                boolean allCancelled = true;
                boolean allMet = true;
                for(String dependency : dependencies) {
                    if(eventStatusMap.get(dependency) != Event.EventStatus.cancelled) {
                        allCancelled = false;
                    }
                    if(!(eventStatusMap.get(dependency) == Event.EventStatus.cancelled || eventStatusMap.get(dependency) == Event.EventStatus.triggered)) {
                        allMet = false;
                    }
                }

                // if all dependencies are in cancelled state, then add the output event of the state to cancelled events and mark state as cancelled
                if(allCancelled) {
                    statesDAO.updateStatus(state.getId(), stateMachine.getId(), Status.cancelled);
                    auditDAO.create(new AuditRecord(stateMachine.getId(), state.getId(), state.getAttemptedNoOfRetries(), Status.cancelled, null, null));
                    EventDefinition eventDefinition = null;
                    if(state.getOutputEvent() != null) {
                        try {
                            eventDefinition = objectMapper.readValue(state.getOutputEvent(), EventDefinition.class);
                        } catch (IOException ex) {
                            throw new RuntimeException("Error occurred while deserializing task outputEvent for stateMachineInstanceId: " + stateMachine.getId() + " taskId: " + state.getId());
                        }
                        cancelledEvents.add(eventDefinition.getName());
                    }
                } else if(allMet) {
                    // if all dependencies are in cancelled or triggered state, then execute the state
                    executableStates.add(state);
                }
            }
        }
        return executableStates;
    }

    /**
     * This is called when an event is received with cancelled status. This cancels the particular path in state machine DAG.
     * @param stateMachine
     * @param eventData
     */
    public void handlePathCancellation(StateMachine stateMachine, EventData eventData) {
        Set<State> executableStates = cancelPath(stateMachine, eventData);
        executeStates(stateMachine, executableStates);
    }

    /**
     * Retrieves the states which are dependant on this event and starts the execution of eligible states (whose all dependencies are met).
     * @param eventData
     * @param stateMachine
     */
    public Set<State> postEvent(EventData eventData, StateMachine stateMachine) {
        Event event = persistEvent(stateMachine, eventData);
        return processEvent(event, stateMachine);
    }

    /**
     * Persists Event data and changes event status
     * @param stateMachine
     * @param eventData
     * @return
     */
    @Transactional
    public Event persistEvent(StateMachine stateMachine, EventData eventData) {
        //update event's data and status
        Event event = eventsDAO.findBySMIdAndName(stateMachine.getId(), eventData.getName());
        if(event == null)
            throw new IllegalEventException("Event with stateMachineId: "+stateMachine.getId()+", event name: "+ eventData.getName()+" not found");
        event.setStatus(eventData.getCancelled() != null && eventData.getCancelled() ? Event.EventStatus.cancelled : Event.EventStatus.triggered);
        event.setEventData(eventData.getData());
        event.setEventSource(eventData.getEventSource());
        eventsDAO.updateEvent(event);
        return event;
    }

    /**
     * Checks and triggers the states which are dependant on the current event
     * @param event
     * @param stateMachine
     * @return
     */
    public Set<State> processEvent(Event event, StateMachine stateMachine) {
        //create context and dependency graph
        Context context = new RAMContext(System.currentTimeMillis(), null, stateMachine); //TODO: set context id, should we need it ?

        //get the states whose dependencies are met
        final Set<State> dependantStates = context.getDependantStates(event.getName());
        logger.debug("These states {} depend on event {}", dependantStates, event.getName());
        Set<State> executableStates = getExecutableStates(dependantStates, stateMachine.getId());
        logger.debug("These states {} are now unblocked after event {}", executableStates, event.getName());
        //start execution of the above states
        executeStates(stateMachine, executableStates, event);

        return executableStates;
    }


    public void updateTaskStatus(Long machineId, Long stateId, ExecutionUpdateData executionUpdateData) {
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
        metricsClient.markMeter(new StringBuilder().
                append("stateMachine.").
                append(executionUpdateData.getStateMachineName()).
                append(".task.").
                append(executionUpdateData.getTaskName()).
                append(".status.").
                append(updateStatus.name()).
                toString());
        updateExecutionStatus(machineId, stateId, updateStatus, executionUpdateData.getRetrycount(),
                executionUpdateData.getCurrentRetryCount(), executionUpdateData.getErrorMessage(), executionUpdateData.isDeleteFromRedriver());
    }

    /**
     * Updates the execution status for the specified State machine's Task
     * @param stateMachineId the state machine identifier
     * @param taskId the Task identifier
     * @param status the Status to be updated to
     * @param retryCount the configured retry count for the task
     * @param currentRetryCount current retry count for the task
     * @param errorMessage the error message in case task has failed
     */
    public void updateExecutionStatus(Long stateMachineId,Long taskId, Status status, long retryCount, long currentRetryCount, String errorMessage, boolean deleteFromRedriver) {
        this.statesDAO.updateStatus(taskId, stateMachineId, status);
        this.auditDAO.create(new AuditRecord(stateMachineId, taskId, currentRetryCount, status, null, errorMessage));
        // cancel the redriver if the Task's original retry count is > 0 and deleteFromRedriver flag is true
        // Redriver would not have been registered if the retry count is 0
        if (retryCount > 0 && deleteFromRedriver) {
            this.redriverRegistry.deRegisterTask(taskId);
        }
    }

    /**
     * Unsidelines a state and triggers its execution.
     * @param stateMachineId
     * @param stateId
     */
    public void unsidelineState(Long stateMachineId, Long stateId) {
        State askedState = null;
        StateMachine stateMachine = retrieveStateMachine(stateMachineId);
        if(stateMachine == null )
            throw new UnknownStateMachine("State machine with id: "+ stateMachineId+ " not found");
        for (State state : stateMachine.getStates()){
            if(Objects.equals(state.getId(), stateId)){
                askedState = state;
                break;
            }
        }

        if(askedState == null){
            throw new IllegalStateException("State with the asked id: " + stateId + " not found in stateMachine with id: " + stateMachineId);
        }

        if (askedState.getStatus() == Status.sidelined || askedState.getStatus() == Status.errored) {
            askedState.setStatus(Status.unsidelined);
            askedState.setAttemptedNoOfRetries(0L);

            this.statesDAO.updateState(askedState);

            this.executeStates(stateMachine, Sets.newHashSet(Arrays.asList(askedState)));
        }


    }

    /**
     * Increments the no. of execution retries for the specified State machine's Task
     * @param stateMachineId the state machine identifier
     * @param taskId the Task identifier
     */
    public void incrementExecutionRetries(Long stateMachineId,Long taskId) {
        this.statesDAO.incrementRetryCount(taskId, stateMachineId);
    }

    private StateMachine retrieveStateMachineByCorrelationId(String correlationId) {
        return stateMachinesDAO.findByCorrelationId(correlationId);
    }

    /**
     * Wrapper function on {@link #executeStates(StateMachine, Set, Event)} which triggers the execution of executableStates using Akka router.
     */
    private void executeStates(StateMachine stateMachine, Set<State> executableStates) {
        executeStates(stateMachine, executableStates, null);
    }

    /**
     * Triggers the execution of executableStates using Akka router
     * @param stateMachine the state machine
     * @param executableStates states whose all dependencies are met
     */
    private void executeStates(StateMachine stateMachine, Set<State> executableStates, Event currentEvent) {
        MDC.put(STATE_MACHINE_ID, stateMachine.getId().toString());
        executableStates.forEach((state ->  {
            MDC.put(TASK_ID,state.getId().toString());
            // trigger execution if state is not in completed|cancelled state
            if(!(state.getStatus() == Status.completed || state.getStatus() == Status.cancelled)) {
                List<EventData> eventDatas;
                // If the state is dependant on only one event, that would be the event which came now, in that case don't make a call to DB
                if (currentEvent != null && state.getDependencies() != null && state.getDependencies().size() == 1 && currentEvent.getName().equals(state.getDependencies().get(0))) {
                    eventDatas = Collections.singletonList(new EventData(currentEvent.getName(), currentEvent.getType(), currentEvent.getEventData(), currentEvent.getEventSource()));
                } else {
                    eventDatas = eventsDAO.findByEventNamesAndSMId(state.getDependencies(), stateMachine.getId());
                }
                final TaskAndEvents msg = new TaskAndEvents(state.getName(), state.getTask(), state.getId(),
                        eventDatas.toArray(new EventData[]{}),
                        stateMachine.getId(), stateMachine.getName() ,state.getOutputEvent(), state.getRetryCount(), state.getAttemptedNoOfRetries());
                if (state.getStatus().equals(Status.initialized) || state.getStatus().equals(Status.unsidelined)) {
                    msg.setFirstTimeExecution(true);
                }

                // register the Task with the redriver
                if (state.getRetryCount() > 0) {
                    // Delay between retires is exponential (2, 4, 8, 16, 32.... seconds) as seen in AkkaTask.
                    // Redriver interval is set as 2 x ( 2^(retryCount+1) x 1s + (retryCount+1) x timeout)
                    long redriverInterval = 2 * ((int) Math.pow(2, state.getRetryCount() + 1) * 1000 + (state.getRetryCount() + 1) * state.getTimeout());
                    this.redriverRegistry.registerTask(state.getId(), redriverInterval);
                }

                //send the message to the Router
                String taskName = state.getTask();
                int secondUnderscorePosition = taskName.indexOf('_', taskName.indexOf('_') + 1);
                String routerName = taskName.substring(0, secondUnderscorePosition == -1 ? taskName.length() : secondUnderscorePosition); //the name of router would be classFQN_taskMethodName
                ActorRef router = this.routerRegistry.getRouter(routerName);
                router.tell(msg, ActorRef.noSender());
                metricsClient.incCounter(new StringBuilder().
                        append("stateMachine.").
                        append(msg.getStateMachineName()).
                        append(".task.").
                        append(msg.getTaskName()).
                        append(".queueSize").toString());
                logger.info("Sending msg to router: {} to execute state machine: {} task: {}", router.path(), stateMachine.getId(), msg.getTaskId());
            } else {
                logger.info("State machine: {} Task: {} execution request got discarded as the task is {}", state.getStateMachineId(), state.getId(), state.getStatus());
            }
        }));
    }

    private StateMachine retrieveStateMachine(Long stateMachineInstanceId) {
        return stateMachinesDAO.findById(stateMachineInstanceId);
    }

    /**
     * Given states which are dependant on a particular event, returns which of them can be executable (states whose all dependencies are met)
     * @param dependantStates
     * @param stateMachineInstanceId
     * @return executableStates
     */
    private Set<State> getExecutableStates(Set<State> dependantStates, Long stateMachineInstanceId) {
        // TODO : states can get triggered twice if we receive all their dependent events at roughly the same time.
        Set<State> executableStates = new HashSet<>();
        Set<String> receivedEvents = new HashSet<>(eventsDAO.findTriggeredOrCancelledEventsNamesBySMId(stateMachineInstanceId));

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
    public void redriveTask(Long taskId) {
        MDC.put(TASK_ID, taskId.toString());
        State state = statesDAO.findById(taskId);

        if(state != null && isTaskRedrivable(state.getStatus()) && state.getAttemptedNoOfRetries() < state.getRetryCount()) {
            StateMachine stateMachine = retrieveStateMachine(state.getStateMachineId());
            MDC.put(STATE_MACHINE_ID,stateMachine.getId().toString());
            logger.info("Redriving a task with Id: {} for state machine: {}", state.getId(), state.getStateMachineId());
                executeStates(stateMachine, Collections.singleton(state));
        } else {
            //cleanup the tasks which can't be redrived from redriver db
            this.redriverRegistry.deRegisterTask(taskId);
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
        stateMachine.getStates().stream().filter(state -> state.getStatus() == Status.initialized).forEach(state -> {
            this.statesDAO.updateStatus(state.getId(), stateMachine.getId(), Status.cancelled);
            this.auditDAO.create(new AuditRecord(stateMachine.getId(), state.getId(), state.getAttemptedNoOfRetries(), Status.cancelled, null, null));
        });
    }
}
