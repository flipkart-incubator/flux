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
import com.flipkart.flux.api.EventData;
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
import com.flipkart.flux.task.redriver.RedriverRegistry;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

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

    /** Constructor for this class */
    @Inject
    public WorkFlowExecutionController(EventsDAO eventsDAO, StateMachinesDAO stateMachinesDAO,
                                       StatesDAO statesDAO, AuditDAO auditDAO, RouterRegistry routerRegistry, RedriverRegistry redriverRegistry) {
        this.eventsDAO = eventsDAO;
        this.stateMachinesDAO = stateMachinesDAO;
        this.statesDAO = statesDAO;
        this.auditDAO = auditDAO;
        this.routerRegistry = routerRegistry;
        this.redriverRegistry = redriverRegistry;
    }

    /**
     * Perform init operations on a state machine and starts execution of states which are not dependant on any events.
     * @param stateMachine
     * @return List of states that do not have any event dependencies on them
     */
    public Set<State> initAndStart(StateMachine stateMachine) {

        //create context and dependency graph
        Context context = new RAMContext(System.currentTimeMillis(), null, stateMachine); //TODO: set context id, should we need it ?

        final List<String> triggeredEvents = eventsDAO.findTriggeredEventsNamesBySMId(stateMachine.getId());
        Set<State> initialStates = context.getInitialStates(new HashSet<>(triggeredEvents));
        executeStates(stateMachine, initialStates);

        return initialStates;
    }

    /**
     * Retrieves the states which are dependant on this event and starts the execution of eligible states (whose all dependencies are met).
     * @param eventData
     * @param stateMachineInstanceId
     * @param correlationId
     */
    public Set<State> postEvent(EventData eventData, Long stateMachineInstanceId, String correlationId) {
        StateMachine stateMachine = null;
        if (stateMachineInstanceId != null) {
            stateMachine = retrieveStateMachine(stateMachineInstanceId);
        } else if(correlationId != null) {
            stateMachine = retrieveStateMachineByCorrelationId(correlationId);
            stateMachineInstanceId = (stateMachine == null) ? null : stateMachine.getId();
        }
        if(stateMachine == null)
            throw new UnknownStateMachine("State machine with id: "+stateMachineInstanceId+ " or correlation id " + correlationId + " not found");
        //update event's data and status
        Event event = eventsDAO.findBySMIdAndName(stateMachineInstanceId, eventData.getName());
        if(event == null)
            throw new IllegalEventException("Event with stateMachineId: "+stateMachineInstanceId+", event name: "+ eventData.getName()+" not found");
        event.setStatus(Event.EventStatus.triggered);
        event.setEventData(eventData.getData());
        event.setEventSource(eventData.getEventSource());
        eventsDAO.updateEvent(event);

        //create context and dependency graph
        Context context = new RAMContext(System.currentTimeMillis(), null, stateMachine); //TODO: set context id, should we need it ?

        //get the states whose dependencies are met
        final Set<State> dependantStates = context.getDependantStates(eventData.getName());
        logger.debug("These states {} depend on event {}", dependantStates, eventData.getName());
        Set<State> executableStates = getExecutableStates(dependantStates, stateMachineInstanceId);
        logger.debug("These states {} are now unblocked after event {}", executableStates, eventData.getName());
        //start execution of the above states
        executeStates(stateMachine, executableStates, event);

        return executableStates;
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
        executableStates.forEach((state ->  {
            if(state.getStatus() != Status.completed) {
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
                logger.info("Sending msg to router: {} to execute state machine: {} task: {}", router.path(), stateMachine.getId(), msg.getTaskId());
            } else {
                logger.info("State machine: {} Task: {} execution request got discarded as the task is already completed", state.getStateMachineId(), state.getId());
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
        Set<String> receivedEvents = new HashSet<>(eventsDAO.findTriggeredEventsNamesBySMId(stateMachineInstanceId));

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
        State state = statesDAO.findById(taskId);

        if(state != null && isTaskRedrivable(state.getStatus()) && state.getAttemptedNoOfRetries() < state.getRetryCount()) {
            StateMachine stateMachine = retrieveStateMachine(state.getStateMachineId());
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
}
