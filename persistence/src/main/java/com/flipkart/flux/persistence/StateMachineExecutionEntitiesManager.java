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
package com.flipkart.flux.persistence;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.PersistenceException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.api.AuditEvent;
import com.flipkart.flux.api.EventData;
import com.flipkart.flux.api.EventDefinition;
import com.flipkart.flux.api.StateDefinition;
import com.flipkart.flux.api.StateMachineDefinition;
import com.flipkart.flux.domain.AuditRecord;
import com.flipkart.flux.domain.Context;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.domain.StateTraversalPath;
import com.flipkart.flux.domain.Status;
import com.flipkart.flux.impl.RAMContext;
import com.flipkart.flux.persistence.dao.iface.AuditDAOV1;
import com.flipkart.flux.persistence.dao.iface.DAO;
import com.flipkart.flux.persistence.dao.iface.EventsDAOV1;
import com.flipkart.flux.persistence.dao.iface.StateMachinesDAOV1;
import com.flipkart.flux.persistence.dao.iface.StateTraversalPathDAOV1;
import com.flipkart.flux.persistence.key.FSMId;
import com.flipkart.flux.utils.SearchUtil;

/**
 * A persistence manager class for State Machine execution entities .
 * @author regu.b
 *
 */
public class StateMachineExecutionEntitiesManager extends MultiEntityManager {
	
	/**
	 * Logger instance for this class
	 */
	private static final Logger LOGGER = LogManager.getLogger(StateMachineExecutionEntitiesManager.class);
	
    private final ObjectMapper objectMapper;	
	
    private Integer maxRetryCount;
    
    private  SearchUtil searchUtil;
    
	/** Constructor */
	@SuppressWarnings("rawtypes")
	@Inject
	public StateMachineExecutionEntitiesManager(StateMachinesDAOV1 stateMachinesDAOV1, AuditDAOV1 auditDAOV1,
            StateTraversalPathDAOV1 stateTraversalPathDAOV1,
            EventsDAOV1 eventsDAOV1, @Named("task.maxTaskRetryCount") Integer maxRetryCount, SearchUtil searchUtil) {
		List<DAO> daos = new LinkedList<DAO>();
		daos.add(stateMachinesDAOV1);
		daos.add(auditDAOV1);
		daos.add(stateTraversalPathDAOV1);
		daos.add(eventsDAOV1);
		super.setDaos(daos);
		this.maxRetryCount = maxRetryCount;
		this.objectMapper = new ObjectMapper();
		this.searchUtil = searchUtil;
		System.out.println("Search util is : " + searchUtil);
	}
	
	public StateMachine createStateMachine(FSMId fsmId, StateMachineDefinition stateMachineDefinition) {
        validateReplayableStates(stateMachineDefinition);
        final Map<EventDefinition, EventData> eventDataMap = stateMachineDefinition.getEventDataMap();
        Set<Event> allEvents = createAllEvents(eventDataMap);
        Set<StateDefinition> stateDefinitions = stateMachineDefinition.getStates();
        Set<State> states = new HashSet<>();

        AtomicInteger stateId = new AtomicInteger(1);
        for (StateDefinition stateDefinition : stateDefinitions) {
            State state = convertStateDefinitionToState(stateDefinition, fsmId.statemachineId, stateId.longValue());
            states.add(state);
            stateId.incrementAndGet();
        }

        StateMachine stateMachine = new StateMachine( fsmId.statemachineId, stateMachineDefinition.getVersion(),
                stateMachineDefinition.getName(),
                stateMachineDefinition.getDescription(),
                states, stateMachineDefinition.getClientElbId());

        List<Object> createEntities = new LinkedList<Object>();
        // add the StateMachine for creation
        createEntities.add(stateMachine);
        // add the EventS for creation
        for (Event event : allEvents) {
            event.setStateMachineInstanceId(stateMachine.getId());
            createEntities.add(event);
        }
        //Add audit records for all the states for creation
        for (State state : stateMachine.getStates()) {
        	createEntities.add(new AuditRecord(stateMachine.getId(), state.getId(), 0L,
                    Status.initialized, null, null, 0L,
                    getDependentEvents(state.getDependencies(), 0L)));
        }
        // add StateTraversal path entities
        // Map to store result and return, this will help to test the functionality
        Map<Long, List<Long>> replayStateTraversalPath = new HashMap<>();
        Set<Long> replayableStateIds = filterReplayableStates(stateMachine.getStates());
        Context context = new RAMContext(System.currentTimeMillis(), null, stateMachine);
        for (Long replayableStateId : replayableStateIds) {
            List<Long> nextDependentStateIds = searchUtil.findStatesInTraversalPath(context, stateMachine,
                    replayableStateId);
            replayStateTraversalPath.put(replayableStateId, nextDependentStateIds);
            //create and store traversal path for given replayable state
            createEntities.add(new StateTraversalPath(stateMachine.getId(), replayableStateId,
                        replayStateTraversalPath.get(replayableStateId)));
        }
        
        // create the entities in one Transaction scope
        super.create((Object[])createEntities.toArray(new Object[0]));
        return stateMachine;
	}

    /***
     * 1. This method Validates that no two states have same replay event as a dependency.
     * 2. It also validates if a state has dependency of more than one replay event.
     * For the states which has at least one of the above conditions true, CreateStateMachineException is thrown.
     * @param stateMachineDefinition
     * @return
     */
    public static StateMachineDefinition validateReplayableStates(StateMachineDefinition stateMachineDefinition) {

        Set<StateDefinition> setOfStates = stateMachineDefinition.getStates();
        Map<String, Integer> replayEventCount = new HashMap<>();
        Map<String, Integer> replayableStates = new HashMap<>();
        for (StateDefinition state : setOfStates) {
            if (state.isReplayable() && !state.getDependencies().isEmpty()) {
                replayableStates.putIfAbsent(state.getName(), 0);
                List<EventDefinition> dependentEvents = state.getDependencies();
                for (EventDefinition dependentEvent : dependentEvents) {
                    if (dependentEvent.getEventSource() != null && dependentEvent.getEventSource().toLowerCase().contains(
                    		PersistenceConstants.REPLAY_EVENT.toLowerCase())){
                        replayableStates
                            .put(state.getName(), replayableStates.get(state.getName()) + 1);
                        if (replayEventCount.get(dependentEvent.getName()) != null) {
                            replayEventCount.put(dependentEvent.getName(), replayEventCount.get(dependentEvent.getName()) + 1);
                        }
                        else
                            replayEventCount.put(dependentEvent.getName(), 1);
                    }
                }
                if (replayableStates.get(state.getName()) != null
                    && replayableStates.get(state.getName()) > 1) {
                    LOGGER.error(
                        "There are 2 or more dependent replay events. "
                            + "To make state: {} as replayable, retry submitting workflow"
                            + " with only one dependent event as replayable.", state.getName(),
                        state.getName());
                    throw new PersistenceException("A single state cannot have multiple dependent replay events.");
                }
            }
        }
        // Check if any replay event is in dependency of more than one state(s)
        if(!replayEventCount.entrySet().stream()
            .filter(entry -> entry.getValue() > 1)
            .map(Entry::getKey)
            .collect(Collectors.toList()).isEmpty()) {
            throw new PersistenceException("Multiple states cannot have same Replay Event as dependency.");
        }
        return stateMachineDefinition;
    }
    
    /**
     * creates event domain objects from event definitions.
     *
     * @param eventDataMap
     * @return set of events
     */
    private Set<Event> createAllEvents(Map<EventDefinition, EventData> eventDataMap) {
        Set<Event> allEvents = new HashSet<>();
        for (Map.Entry<EventDefinition, EventData> currentEventDefinitionAndData : eventDataMap.entrySet()) {
            final Event currentEvent = this.convertEventDefinitionToEvent(
                    currentEventDefinitionAndData.getKey());
            if (currentEventDefinitionAndData.getValue() != null) {
                currentEvent.setEventData(currentEventDefinitionAndData.getValue().getData());
                currentEvent.setEventSource(currentEventDefinitionAndData.getValue().getEventSource());
                currentEvent.setStatus(Event.EventStatus.triggered);
            }
            allEvents.add(currentEvent);
        }
        return allEvents;
    }

    private Event convertEventDefinitionToEvent(EventDefinition eventDefinition) {
        if (eventDefinition.getEventSource() != null) {
          return new Event(eventDefinition.getName(), eventDefinition.getType(),
              Event.EventStatus.pending,
              null, null, eventDefinition.getEventSource(),
              0L);
        }
        return new Event(eventDefinition.getName(), eventDefinition.getType(),
            Event.EventStatus.pending,
            null, null, null, 0L);
    }
    
    /**
     * Converts state definition to state domain object.
     *
     * @param stateDefinition
     * @return state
     */
    private State convertStateDefinitionToState(StateDefinition stateDefinition, String stateMachineId, Long id) {
        try {
            List<EventDefinition> eventDefinitions = stateDefinition.getDependencies();
            List<String> events = new LinkedList<>();
            State state;
            if (eventDefinitions != null) {
                for (EventDefinition e : eventDefinitions) {
                    events.add(e.getName());
                }
            }
            // Limit MaxReplayableRetries for a state to RuntimeConstants.MAX_REPLAYABLE_RETRIES
            if (stateDefinition.getMaxReplayableRetries() != null && stateDefinition.getMaxReplayableRetries() > PersistenceConstants.MAX_REPLAYABLE_RETRIES) {
                stateDefinition.setMaxReplayableRetries(PersistenceConstants.MAX_REPLAYABLE_RETRIES);
                LOGGER.warn("MaxReplayableRetries has been reset to {} for state {}. Value provided by user was"
                    + " higher than threshold", PersistenceConstants.MAX_REPLAYABLE_RETRIES, stateDefinition.getName());
            }

            state = new State(stateDefinition.getVersion(),
                    stateDefinition.getName(),
                    stateDefinition.getDescription(),
                    stateDefinition.getOnEntryHook(),
                    stateDefinition.getTask(),
                    stateDefinition.getOnExitHook(),
                    events,
                    Math.min(stateDefinition.getRetryCount(), maxRetryCount),
                    stateDefinition.getTimeout(),
                    stateDefinition.getOutputEvent() != null ? objectMapper.writeValueAsString(
                            stateDefinition.getOutputEvent()) : null,
                    Status.initialized, null, 0l, stateMachineId, id,
                    stateDefinition.getMaxReplayableRetries() != null ? (short) stateDefinition.getMaxReplayableRetries() :  PersistenceConstants.MAX_REPLAYABLE_RETRIES,
                    (short) 0,
                    stateDefinition.isReplayable());

            return state;
        } catch (Exception e) {
            throw new PersistenceException("Unable to create state domain object", e);
        }
    }
    
    private String getDependentEvents(List<String> stateDependentEventNames, Long eventExecutionVersion) {
        List<AuditEvent> dependentEvents = new LinkedList<>();
        for(String dependentEventName : stateDependentEventNames) {
            dependentEvents.add(new AuditEvent(dependentEventName, eventExecutionVersion));
        }
        return dependentEvents.toString();
    }
	
    /**
     * For given set of states, filter states which are marked as replayable.
     * @param states
     * @return
     */
    private Set<Long> filterReplayableStates(Set<State> states) {
        Set<Long> replayableStateIds = new HashSet<>();
        for (State state : states) {
            if (state.getReplayable())
                replayableStateIds.add(state.getId());
        }
        return replayableStateIds;
    }
    
}
