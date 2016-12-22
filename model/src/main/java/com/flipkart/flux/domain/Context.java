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

package com.flipkart.flux.domain;

import java.util.*;

/**
 * <code>Context</code> carries execution context for use during a State's execution.
 * This is created and maintained by the execution engine.
 * The Context is not passed around to workers. Workers interact with the context via APIs (see  event posting API, for instance).
 *
 * @author Yogesh
 * @author regunath.balasubramanian
 * @author shyam.akirala
 * @author kartik.bommepally
 */
public abstract class Context {

    /** The start time when this Context was created*/
    protected Long startTime;
    /** Identifier for the Context*/
    protected String contextId;

    /** will be used in dependency graph map as key for initial states which are dependant on no events*/
    private static final String START = "start";
    /**
     * A reverse dependency graph created across States based on Events.
     * Holds information on possible list of States waiting on an Event represented by its FQN.
     */
    protected Map<String, Set<State>> eventToStateDependencyGraph;

    /**
     * Attaches context to state machine and builds dependency graph for the state machine.
     * @param stateMachine
     */
    public Context(StateMachine stateMachine) {
        stateMachine.setContext(this);
        buildDependencyMap(stateMachine.getStates());
    }

    /**
     * Stores the specified data against the key for this Context. Implementations may bound the type and size of data stored into this Context.
     * @param key the data identifier key
     * @param data the opaque data stored against the specified key
     */
    public abstract void storeData(String key, Object data);

    /**
     * Retrieves the data stored against the specified key 
     * @param key the identifier key for data stored in this Context
     * @return data stored in this Context, keyed by the specified identifier 
     */
    public abstract Object retrieve(String key);

    /**
     * Returns set of states which are dependant on an event.
     * @param eventName
     * @return
     */
    public Set<State> getDependantStates(String eventName) {
        final Set<State> dependentStates = eventToStateDependencyGraph.get(eventName);
        if (dependentStates == null) {
            return Collections.emptySet();
        }
        return dependentStates;
    }

    /**
     * Returns set of states which can be started when state machine starts for the first time.
     * @return initial states
     * @param triggeredEventNames Names of events that have already been received during the state machine definition
     */
    public Set<State> getInitialStates(Set<String> triggeredEventNames) {
        final Set<State> initialStates = new HashSet<>();
        final Set<State> startStates = eventToStateDependencyGraph.get(START);
        if (startStates != null ) {
            initialStates.addAll(startStates);
        }
        for (String aTriggeredEventName : triggeredEventNames) {
            final Set<State> statesDependentOnThisEvent = eventToStateDependencyGraph.get(aTriggeredEventName);
            if (statesDependentOnThisEvent != null) {
                statesDependentOnThisEvent.stream().filter(state1 -> state1.isDependencySatisfied(triggeredEventNames)).forEach(initialStates::add);
            }
        }
        return initialStates;
    }

    /**
     * This builds dependency graph between event and states and keeps for later use. Currently dependency graph is created on every event arrival.
     */
    public void buildDependencyMap(Set<State> states) {
        eventToStateDependencyGraph = new HashMap<>();
        for(State state : states) {
            if (!state.getDependencies().isEmpty()) {
                for (String eventName : state.getDependencies()) {
                    if (!eventToStateDependencyGraph.containsKey(eventName))
                        eventToStateDependencyGraph.put(eventName, new HashSet<State>());
                    eventToStateDependencyGraph.get(eventName).add(state);
                }
            } else {
                if (!eventToStateDependencyGraph.containsKey(START))
                    eventToStateDependencyGraph.put(START, new HashSet<State>());
                eventToStateDependencyGraph.get(START).add(state);
            }
        }
    }

    /** Accessor/Mutator methods*/
    public Long getStartTime() {
        return startTime;
    }
    public String getContextId() {
        return contextId;
    }
}
