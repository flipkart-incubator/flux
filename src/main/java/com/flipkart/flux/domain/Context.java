/*
 * Copyright 2012-2015, the original author or authors.
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
 * @understands : Carries execution context that can be used during a state's execution
 * This is created and maintained by the execution engine.
 * This is not passed around to workers. Workers interact with the context via APIs (see  event posting API, for instance).
 *
 */
public class Context {
    Long startTime;
    String contextId;
    Map<String,Object> data; //Convenience given to workers to store any transient information at a central store
    private Map<String,Event> events;
    private Map<State,List<String>> stateToEventDependencyGraph;

    public String getContextId() {
        return contextId;
    }

    public Long getStartTime() {
        return startTime;
    }
    public Object retrieve(String key) {
        return data.get(key);
    }
    public void postEvent(Event event) {
        // Record event
    }
    public List<State> getExecutableStates() {
        // Go through the dependency graph to figure the states that can now be executed
        return null;
    }
}
