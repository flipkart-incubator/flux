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

import java.util.List;
import java.util.Map;

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
public abstract class Context<T> {

    /** The start time when this Context was created*/
    protected Long startTime;
    /** Identifier for the Context*/
    protected String contextId;
    /**
     * A reverse dependency graph created across States based on Events.
     * Holds information on possible list of States waiting on an Event represented by its FQN.
     */
    protected Map<String, List<State<T>>> stateToEventDependencyGraph;

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

    public List<State<T>> getExecutableStates(State<T> currentState, Event<T> event) {
        // Go through the dependency graph to figure the states that can now be executed
        return null;
    }
    public boolean isExecutionCancelled() {
        //check for cancelledException in data, and return whether state machine execution is cancelled or not
        return false;
    }

    /** Accessor/Mutator methods*/
    public Long getStartTime() {
        return startTime;
    }
    public String getContextId() {
        return contextId;
    }
}
