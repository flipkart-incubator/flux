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

package com.flipkart.flux.representation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.api.AuditEvent;
import com.flipkart.flux.api.EventData;
import com.flipkart.flux.api.EventDefinition;
import com.flipkart.flux.api.StateDefinition;
import com.flipkart.flux.api.StateMachineDefinition;
import com.flipkart.flux.client.constant.ClientConstants;
import com.flipkart.flux.constant.RuntimeConstants;
import com.flipkart.flux.dao.iface.AuditDAO;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.dao.iface.StateTraversalPathDAO;
import com.flipkart.flux.domain.AuditRecord;
import com.flipkart.flux.domain.Context;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.domain.StateTraversalPath;
import com.flipkart.flux.domain.Status;
import com.flipkart.flux.impl.RAMContext;
import com.flipkart.flux.persistence.DataSourceType;
import com.flipkart.flux.persistence.SelectDataSource;
import com.flipkart.flux.persistence.Storage;
import com.flipkart.flux.utils.SearchUtil;

/**
 * <code>StateMachinePersistenceService</code> class converts user provided state machine entity definition to domain
 * type object and stores in DB.
 *
 * @author shyam.akirala
 */
@Singleton
public class StateMachinePersistenceService {
    private final ObjectMapper objectMapper;
    private StateMachinesDAO stateMachinesDAO;
    private AuditDAO auditDAO;
    private StateTraversalPathDAO stateTraversalPathDAO;

    private EventPersistenceService eventPersistenceService;
    private Integer maxRetryCount;
    private static Logger logger = LogManager.getLogger(StateMachinePersistenceService.class);

    @Inject
    public StateMachinePersistenceService(StateMachinesDAO stateMachinesDAO, AuditDAO auditDAO,
                                          StateTraversalPathDAO stateTraversalPathDAO,
                                          EventPersistenceService eventPersistenceService,
                                          @Named("task.maxTaskRetryCount") Integer maxRetryCount) {
        this.stateMachinesDAO = stateMachinesDAO;
        this.auditDAO = auditDAO;
        this.stateTraversalPathDAO = stateTraversalPathDAO;
        this.eventPersistenceService = eventPersistenceService;
        this.maxRetryCount = maxRetryCount;
        objectMapper = new ObjectMapper();
    }

    /***
     * This method Validates if a state has dependency of more than one replay event
     * For the states which have dependency of multiple replay event are marked as replayable false
     * @param stateMachineDefinition
     * @return
     */
    public static StateMachineDefinition validateStateWithMultipleReplayEvent(StateMachineDefinition stateMachineDefinition) {

        Set<StateDefinition> setOfStates = stateMachineDefinition.getStates();
        Map<String, Integer> replayableStates = new HashMap<>();
        setOfStates.forEach(state -> {
            if (state.isReplayable() && !state.getDependencies().isEmpty()) {
                replayableStates.putIfAbsent(state.getName(), 0);
                List<EventDefinition> dependentEvents = state.getDependencies();
                for (EventDefinition dependentEvent : dependentEvents) {
                    if (dependentEvent.getEventSource() != null && dependentEvent.getEventSource().toLowerCase().contains(ClientConstants.REPLAY_EVENT.toLowerCase())) {
                        replayableStates.put(state.getName(), replayableStates.get(state.getName()) + 1);
                    }
                }
                if (replayableStates.get(state.getName()) != null && replayableStates.get(state.getName()) > 1) {
                    state.setReplayable(Boolean.FALSE);
                    logger.info("Marked state: {} as not replayable because there are 2 or more dependent"
                        + " replay events. To make state: {} as replayable, retry submitting workflow"
                        + " with only one dependent event as replayable.", state.getName(), state.getName());
                }
            }
        });
        return stateMachineDefinition;
    }

    /***
     * This method Validates that no two states have same replay event as a dependency
     * For the states which which has same replay event as that of any other state, replayable is marked to false.
     * @param stateMachineDefinition
     * @return
     */
    public static StateMachineDefinition validateMultipleStatesWithSameReplayEvent(StateMachineDefinition stateMachineDefinition) {

        Set<StateDefinition> setOfStates = stateMachineDefinition.getStates();
        Map<String, Integer> replayEventCount = new HashMap<>();
        setOfStates.forEach(state -> {
            if (state.isReplayable() && !state.getDependencies().isEmpty()) {
                List<EventDefinition> dependentEvents = state.getDependencies();
                for (EventDefinition dependentEvent : dependentEvents) {
                    if (dependentEvent.getEventSource() != null && dependentEvent.getEventSource().toLowerCase().contains(ClientConstants.REPLAY_EVENT.toLowerCase())){
                        if (replayEventCount.get(dependentEvent.getName()) != null) {
                            replayEventCount.put(dependentEvent.getName(), replayEventCount.get(dependentEvent.getName()) + 1);
                        }
                        else
                            replayEventCount.put(dependentEvent.getName(), 1);
                    }
                }
            }
        });
        setOfStates.forEach(state -> {
            if (state.isReplayable() && !state.getDependencies().isEmpty()) {
                List<EventDefinition> dependentEvents = state.getDependencies();
                for (EventDefinition dependentEvent : dependentEvents) {
                    if (replayEventCount.get(dependentEvent.getName()) != null && replayEventCount.get(dependentEvent.getName()) > 1) {
                        state.setReplayable(Boolean.FALSE);
                        logger.info("Marked state: {} as not replayable because there are 2 or more states"
                            + " dependent on same replay event: {}. To make state: {} as replayable,"
                            + " retry submitting workflow with one replay event being a dependency of only one state.",
                            state.getName(), dependentEvent, state.getName());
                    }
                }
            }
        });
        return stateMachineDefinition;
    }

    /**
     * Converts state machine definition to state machine domain object and saves in DB.
     *
     * @param stateMachineDefinition
     * @return saved state machine object
     */
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public StateMachine createStateMachine(String stateMachineId, StateMachineDefinition stateMachineDefinition) {

        validateMultipleStatesWithSameReplayEvent(stateMachineDefinition);
        validateStateWithMultipleReplayEvent(stateMachineDefinition);
        final Map<EventDefinition, EventData> eventDataMap = stateMachineDefinition.getEventDataMap();
        Set<Event> allEvents = createAllEvents(eventDataMap);
        Set<StateDefinition> stateDefinitions = stateMachineDefinition.getStates();
        Set<State> states = new HashSet<>();

        AtomicInteger stateId = new AtomicInteger(1);
        for (StateDefinition stateDefinition : stateDefinitions) {
            State state = convertStateDefinitionToState(stateDefinition, stateMachineId, stateId.longValue());
            states.add(state);
            stateId.incrementAndGet();
        }

        StateMachine stateMachine = new StateMachine(stateMachineId, stateMachineDefinition.getVersion(),
                stateMachineDefinition.getName(),
                stateMachineDefinition.getDescription(),
                states, stateMachineDefinition.getClientElbId());

        stateMachinesDAO.create(stateMachineId, stateMachine);
        for (Event event : allEvents) {
            event.setStateMachineInstanceId(stateMachine.getId());
            eventPersistenceService.persistEvent(event);
        }

        //create audit records for all the states
        for (State state : stateMachine.getStates()) {
            auditDAO.create(stateMachine.getId(), new AuditRecord(stateMachine.getId(), state.getId(), 0L,
                    Status.initialized, null, null, 0L,
                    getDependentEvents(state.getDependencies(), 0L)));
        }
        return stateMachine;
    }

    /**
     * Computes traversal path for all replayable states in a state machine and stores in DB
     * @param stateMachineId
     * @param stateMachine
     * @throws RuntimeException
     */
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public Map<Long, List<Long>> createAndPersistStateTraversal(String stateMachineId, StateMachine stateMachine)
            throws RuntimeException {

        // Map to store result and return, this will help to test the functionality
        Map<Long, List<Long>> replayStateTraversalPath = new HashMap<>();

        SearchUtil searchUtil = new SearchUtil();

        //create context and dependency graph < event -> dependent states >
        Context context = new RAMContext(System.currentTimeMillis(), null, stateMachine);

        Set<Long> replayableStateIds = filterReplayableStates(stateMachine.getStates());

        for (Long replayableStateId : replayableStateIds) {
            List<Long> nextDependentStateIds = searchUtil.findStatesInTraversalPath(context, stateMachine,
                    replayableStateId);

            replayStateTraversalPath.put(replayableStateId, nextDependentStateIds);
            //create and store traversal path for given replayable state
            stateTraversalPathDAO.create(stateMachineId,
                    new StateTraversalPath(stateMachineId, replayableStateId, nextDependentStateIds));
        }
        return replayStateTraversalPath;
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

    /**
     * creates event domain objects from event definitions.
     *
     * @param eventDataMap
     * @return set of events
     */
    private Set<Event> createAllEvents(Map<EventDefinition, EventData> eventDataMap) {
        Set<Event> allEvents = new HashSet<>();
        for (Map.Entry<EventDefinition, EventData> currentEventDefinitionAndData : eventDataMap.entrySet()) {
            final Event currentEvent = eventPersistenceService.convertEventDefinitionToEvent(
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
            if (stateDefinition.getMaxReplayableRetries() != null && stateDefinition.getMaxReplayableRetries() > RuntimeConstants.MAX_REPLAYABLE_RETRIES) {
                stateDefinition.setMaxReplayableRetries(RuntimeConstants.MAX_REPLAYABLE_RETRIES);
                logger.warn("MaxReplayableRetries has been reset to {} for state {}. Value provided by user was"
                    + " higher than threshold", RuntimeConstants.MAX_REPLAYABLE_RETRIES, stateDefinition.getName());
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
                    stateDefinition.getMaxReplayableRetries() != null ? (short) stateDefinition.getMaxReplayableRetries() :  RuntimeConstants.MAX_REPLAYABLE_RETRIES,
                    (short) 0,
                    stateDefinition.isReplayable());

            return state;
        } catch (Exception e) {
            throw new IllegalRepresentationException("Unable to create state domain object", e);
        }
    }

    private String getDependentEvents(List<String> stateDependentEventNames, Long eventExecutionVersion) {
        List<AuditEvent> dependentEvents = new LinkedList<>();
        for(String dependentEventName : stateDependentEventNames) {
            dependentEvents.add(new AuditEvent(dependentEventName, eventExecutionVersion));
        }
        return dependentEvents.toString();
    }

}