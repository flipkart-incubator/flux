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
 */
public class Context {

	/** The start time when this Context was created*/
    private Long startTime;
    /** Identifier for the Context*/
    private String contextId;
    /** Data bag for information stored in this Context*/
    private Map<String,Object> data; //Convenience given to workers to store transient information at a central store, implementations will define size limits of this data
    /** A dependency graph created across States - holds information on possible next state transitions for a State*/
    private Map<Event,List<State>> stateToEventDependencyGraph;

    /** Constructor */
    public Context(Long startTime, String contextId) {
		super();
		this.startTime = startTime;
		this.contextId = contextId;
	}
    public List<State> getExecutableStates(State currentState, Event event) {
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
	public Object retrieve(String key) {
        return data.get(key);
    }
    public void storeData(String key, Object data) {
        this.data.put(key, data);
    }
}
