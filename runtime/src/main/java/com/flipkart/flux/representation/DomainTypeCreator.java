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

import com.flipkart.flux.api.EventDefinition;
import com.flipkart.flux.api.StateDefinition;
import com.flipkart.flux.api.StateMachineDefinition;
import com.flipkart.flux.dao.iface.EventsDAO;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.dao.iface.StatesDAO;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.StateMachine;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

/**
 * <code>DomainTypeCreator</code> class converts user provided entity definition to domain type object and stores in DB.
 * @author shyam.akirala
 */
public class DomainTypeCreator<T> {

    private StateMachinesDAO stateMachinesDAO;
    private EventsDAO eventsDAO;
    private StatesDAO statesDAO;

    @Inject
    public DomainTypeCreator(StateMachinesDAO stateMachinesDAO, EventsDAO eventsDAO, StatesDAO statesDAO) {
        this.stateMachinesDAO = stateMachinesDAO;
        this.eventsDAO = eventsDAO;
        this.statesDAO = statesDAO;
    }

    /**
     * Converts state machine definition to state machine domain object and saves in DB.
     * @param stateMachineDefinition
     * @return saved state machine object
     */
    public StateMachine createStateMachine(StateMachineDefinition<T> stateMachineDefinition) {
        Set<StateDefinition<T>> stateDefinitions = stateMachineDefinition.getStates();
        Set<State<T>> states = new HashSet<>();

        for(StateDefinition<T> stateDefinition : stateDefinitions) {
            State state = convertStateDefinitionToStateAndPersist(stateDefinition);
            states.add(state);
        }

        StateMachine<T> stateMachine = new StateMachine<T>(stateMachineDefinition.getVersion(),
                stateMachineDefinition.getName(),
                stateMachineDefinition.getDescription(),
                states);

        return stateMachinesDAO.create(stateMachine);
    }

    /**
     * Converts state definition to state domain object and persists in db.
     * @param stateDefinition
     * @return state
     */
    private State<T> convertStateDefinitionToStateAndPersist(StateDefinition<T> stateDefinition) {
        State<T> state = new State<>(stateDefinition.getVersion(),
                stateDefinition.getName(),
                stateDefinition.getDescription(),
                stateDefinition.getOnEntryHook(),
                stateDefinition.getTask(),
                stateDefinition.getOnExitHook(),
                stateDefinition.getRetryCount(),
                stateDefinition.getTimeout());

        return statesDAO.create(state);
    }

    /**
     * TO DO:
     * @param eventDefinition
     * @return
     */
    public Event<T> createEvent(EventDefinition eventDefinition) {
        return null;
    }

}
