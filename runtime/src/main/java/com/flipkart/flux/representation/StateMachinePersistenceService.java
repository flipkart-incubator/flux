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

import com.flipkart.flux.api.StateDefinition;
import com.flipkart.flux.api.StateMachineDefinition;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.dao.iface.StatesDAO;
import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.StateMachine;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

/**
 * <code>StateMachinePersistenceService</code> class converts user provided state machine entity definition to domain type object and stores in DB.
 * @author shyam.akirala
 */
public class StateMachinePersistenceService<T> {

    private StateMachinesDAO stateMachinesDAO;
    private StatesDAO statesDAO;

    @Inject
    public StateMachinePersistenceService(StateMachinesDAO stateMachinesDAO, StatesDAO statesDAO) {
        this.stateMachinesDAO = stateMachinesDAO;
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
            State state = convertStateDefinitionToState(stateDefinition);
            states.add(state);
        }

        StateMachine<T> stateMachine = new StateMachine<T>(stateMachineDefinition.getVersion(),
                stateMachineDefinition.getName(),
                stateMachineDefinition.getDescription(),
                states);

        return stateMachinesDAO.create(stateMachine);
    }

    /**
     * Converts state definition to state domain object.
     * @param stateDefinition
     * @return state
     */
    private State<T> convertStateDefinitionToState(StateDefinition<T> stateDefinition) {
        State<T> state = new State<>(stateDefinition.getVersion(),
                stateDefinition.getName(),
                stateDefinition.getDescription(),
                stateDefinition.getOnEntryHook(),
                stateDefinition.getTask(),
                stateDefinition.getOnExitHook(),
                stateDefinition.getRetryCount(),
                stateDefinition.getTimeout());
        return state;
    }

}
