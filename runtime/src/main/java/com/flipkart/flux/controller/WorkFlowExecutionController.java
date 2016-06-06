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
import com.flipkart.flux.dao.iface.EventsDAO;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.domain.Context;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.exception.IllegalEventException;
import com.flipkart.flux.exception.UnknownStateMachine;
import com.flipkart.flux.impl.RAMContext;
import com.flipkart.flux.impl.message.TaskAndEvents;
import com.flipkart.flux.impl.task.registry.RouterRegistry;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

/**
 * <code>WorkFlowExecutionController</code> controls the execution flow of a given state machine
 * @author shyam.akirala
 */
@Singleton
public class WorkFlowExecutionController {

    private StateMachinesDAO stateMachinesDAO;

    private EventsDAO eventsDAO;

    private RouterRegistry routerRegistry;

    @Inject
    public WorkFlowExecutionController(EventsDAO eventsDAO, StateMachinesDAO stateMachinesDAO, RouterRegistry routerRegistry) {
        this.eventsDAO = eventsDAO;
        this.stateMachinesDAO = stateMachinesDAO;
        this.routerRegistry = routerRegistry;
    }

    /**
     * Perform init operations on a state machine and starts execution of states which are not dependant on any events.
     * @param stateMachine
     * @return List of states that do not have any event dependencies on them
     */
    public Set<State> initAndStart(StateMachine stateMachine) {

        //create context and dependency graph
        Context context = new RAMContext(System.currentTimeMillis(), null, stateMachine); //TODO: set context id, should we need it ?

        Set<State> initialStates = context.getInitialStates();

        executeStates(stateMachine.getId(),initialStates);

        return initialStates;
    }

    /**
     * Retrieves the states which are dependant on this event and starts the execution of eligible states (whose all dependencies are met).
     * @param eventData
     * @param stateMachineInstanceId
     */
    public Set<State> postEvent(EventData eventData, Long stateMachineInstanceId) {

        StateMachine stateMachine = retrieveStateMachine(stateMachineInstanceId);

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
        Set<State> executableStates = getExecutableStates(context.getDependantStates(eventData.getName()), stateMachineInstanceId);

        //start execution of the above states
        executeStates(stateMachineInstanceId, executableStates);

        return executableStates;
    }

    private void executeStates(Long stateMachineInstanceId, Set<State> executableStates) {
        // TODO - this always uses someRouter for now
        executableStates.forEach((state ->  {
            this.routerRegistry.getRouter("someRouter").tell(
                new TaskAndEvents(state.getTask(), eventsDAO.findByEventNamesAndSMId(state.getDependencies(), stateMachineInstanceId).toArray(new Event[]{}), stateMachineInstanceId, state.getOutputEvent()), ActorRef.noSender());
        }));
    }

    private StateMachine retrieveStateMachine(Long stateMachineInstanceId) {
        StateMachine stateMachine = stateMachinesDAO.findById(stateMachineInstanceId);
        if(stateMachine == null)
            throw new UnknownStateMachine("State machine with id: "+stateMachineInstanceId+ " not found");
        return stateMachine;
    }

    /**
     * Given states which are dependant on a particular event, returns which of them can be executable (states whose all dependencies are met)
     * @param dependantStates
     * @param stateMachineInstanceId
     * @return executableStates
     */
    private Set<State> getExecutableStates(Set<State> dependantStates, Long stateMachineInstanceId) {

        Set<State> executableStates = new HashSet<State>();

        //received events of a particular state machine by system so far
        Set<String> receivedEvents = null;

//      for each state
//        1. get the dependencies (events)
//        2. check whether all events are in triggered state
//        3. if all events are in triggered status, then add this state to executableStates
        for(State state : dependantStates) {
            Set<String> dependantEvents = state.getDependencies();
            if(dependantEvents.size() == 1) { //If state is dependant on only one event then that would be the current event
                executableStates.add(state);
            } else {
                if (receivedEvents == null)
                    receivedEvents = new HashSet<>(eventsDAO.findTriggeredEventsNamesBySMId(stateMachineInstanceId));
                boolean areAllEventsReceived = true;
                for(String dependantEvent : dependantEvents) {
                    if(!receivedEvents.contains(dependantEvent)) {
                        areAllEventsReceived = false;
                        break;
                    }
                }
                if(areAllEventsReceived)
                    executableStates.add(state);
            }
        }

        return executableStates;
    }

}
