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

import java.util.HashSet;
import java.util.Set;

/**
 * <Code>StateMachineDefinition</Code> defines a template for State machine instances on Flux. Defines the states that the state machine can transition through 
 * @author Yogesh
 * @author regunath.balasubramanian
 * @author shyam.akirala
 */
public class StateMachineDefinition {
	
	/** Name for this state machine definition*/
    private String name;

    /** Version of this state machine definition*/
    private Long version;

	/** Short description for this state machine definition*/
    private String description;

    /** Possible states that this state machine can transition to*/
    private Set<StateDefinition> states;

    /* For Jackson */
    StateMachineDefinition() {
    }

    /** Constructor */
    public StateMachineDefinition(String description, String name, long version, Set<StateDefinition> stateDefinitions) {
        this.description = description;
        this.name = name;
        this.states = stateDefinitions;
        this.version = version;
    }

    public void addState(StateDefinition stateDefinition) {
        this.states.add(stateDefinition);
    }

    /** Accessors/Mutators for member variables*/
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
    public Long getVersion() {
        return version;
    }
    public void setVersion(Long version) {
        this.version = version;
    }
    public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Set<StateDefinition> getStates() {
		return states;
	}
	public void setStates(Set<StateDefinition> states) {
		this.states = states;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StateMachineDefinition that = (StateMachineDefinition) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (version != null ? !version.equals(that.version) : that.version != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        return !(states != null ? !states.equals(that.states) : that.states != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (states != null ? states.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "StateMachineDefinition{" +
            "description='" + description + '\'' +
            ", name='" + name + '\'' +
            ", version=" + version +
            ", states=" + states +
            '}';
    }
}
