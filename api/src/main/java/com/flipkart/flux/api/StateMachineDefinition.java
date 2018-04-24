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

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;

/**
 * <Code>StateMachineDefinition</Code> defines a template for State machine instances on Flux. Defines the states that the state machine can transition through 
 * @author Yogesh
 * @author regunath.balasubramanian
 * @author shyam.akirala
 * @author amitkumar.o
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

    /* All Event Data that has been passed on as part of state machine execution */
    private Set<EventData> eventData;

    /* User supplied string for easy identification of a workflow instance */
    private String correlationId;

    /* For Jackson */
    StateMachineDefinition() {
        this(null,null,null, Collections.emptySet(),Collections.emptySet(), null);
    }

    /** Constructor */
    public StateMachineDefinition(String description, String name, Long version, Set<StateDefinition> stateDefinitions, Set<EventData> eventData, String correlationId) {
        this.description = description;
        this.name = name;
        this.states = stateDefinitions;
        this.version = version;
        this.eventData = eventData;
        this.correlationId = correlationId;
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

    public Set<EventData> getEventData() {
        return eventData;
    }

    public void setEventData(Set<EventData> eventData) {
        this.eventData = eventData;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    @JsonIgnore
    public Map<EventDefinition,EventData> getEventDataMap() {
        Map<EventDefinition,EventData> eventDataMap = new HashMap<>();
        for (StateDefinition aState : this.states) {
            final List<EventDefinition> dependenciesForCurrentState = aState.getDependencies();
            for (EventDefinition anEventDefinition : dependenciesForCurrentState) {
                eventDataMap.putIfAbsent(anEventDefinition, retrieveEventDataFor(anEventDefinition));
            }
            final EventDefinition outputEventDefinition = aState.getOutputEvent();
            if (outputEventDefinition  != null) {
                eventDataMap.put(outputEventDefinition, retrieveEventDataFor(outputEventDefinition));
            }
        }
        return eventDataMap;
    }

    private EventData retrieveEventDataFor(EventDefinition anEventDefinition) {
        for (EventData someData : this.eventData) {
            if (someData.isFor(anEventDefinition)) {
                return someData;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StateMachineDefinition that = (StateMachineDefinition) o;

        if (!name.equals(that.name)) return false;
        if (!version.equals(that.version)) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (!states.equals(that.states)) return false;
        return eventData.equals(that.eventData);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + version.hashCode();
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + states.hashCode();
        result = 31 * result + eventData.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "StateMachineDefinition{" +
            "description='" + description + '\'' +
            ", name='" + name + '\'' +
            ", version=" + version +
            ", states=" + states +
            ", eventData=" + eventData +
            '}';
    }

    public void addEventDatas(EventData[] events) {
        Collections.addAll(this.eventData, events);
    }
}
