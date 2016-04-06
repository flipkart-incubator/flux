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

package com.flipkart.flux.api;

import java.util.Set;

/**
 * <Code>StateMachineDefinition</Code> defines a template for State machine instances on Flux. Defines the states that the state machine can transition through 
 * @author Yogesh
 * @author regunath.balasubramanian
 * @author shyam.akirala
 */
public class StateMachineDefinition<T> {
	
	/** Name for this state machine definition*/
    private String name;

    /** Version of this state machine definition*/
    private Long Version;

	/** Short description for this state machine definition*/
    private String description;
    
    /** Possible states that this state machine can transition to*/
    private Set<StateDefinition<T>> states;
    
    /** Constructor */
    public StateMachineDefinition(String description, String name, Set<StateDefinition<T>> states) {
        this.description = description;
        this.name = name;
        this.states = states;
    }

    /** Accessors/Mutators for member variables*/
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
    public Long getVersion() {
        return Version;
    }
    public void setVersion(Long version) {
        Version = version;
    }
    public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Set<StateDefinition<T>> getStates() {
		return states;
	}
	public void setStates(Set<StateDefinition<T>> states) {
		this.states = states;
	}
    
}
