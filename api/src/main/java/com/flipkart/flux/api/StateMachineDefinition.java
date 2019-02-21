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

    private Set<StateMetaDataDefinition> stateMetaDataDefinition;

    private Set<StateTransitionDefinition> stateTransitionDefinition;

    /* All Event Data that has been passed on as part of state machine execution */
    private Set<EventData> eventData;


    /* User supplied string for easy identification of a workflow instance */
    private String correlationId;

    /* Client Eb Id to which task is supposed to be forwarded for execution in Flux Runtime  */
    private String clientElbId;

    /* For Jackson */
    StateMachineDefinition() {
        this(null,null,null, Collections.emptySet(),Collections.emptySet(), Collections.emptySet(),
                null, null);
    }

    /** Constructor */
    public StateMachineDefinition(String description, String name, Long version,
                                  Set<StateTransitionDefinition> stateTransitionDefinition,
                                  Set<StateMetaDataDefinition> stateMetaDataDefinition,
                                  Set<EventData> eventData, String correlationId, String clientElbId) {
        this.description = description;
        this.name = name;
        this.stateMetaDataDefinition = stateMetaDataDefinition;
        this.stateTransitionDefinition = stateTransitionDefinition;
        this.version = version;
        this.eventData = eventData;
        this.correlationId = correlationId;
        this.clientElbId = clientElbId;
    }

    public void addStateMetaData(StateMetaDataDefinition stateMetaDataDefinition) {
        this.stateMetaDataDefinition.add(stateMetaDataDefinition);
    }

    public void addStateTransition(StateTransitionDefinition stateTransitionDefinition) {
        this.stateTransitionDefinition.add(stateTransitionDefinition);
    }

    /** Accessors/Mutators for member variables*/
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
    public String getClientElbId() {
        return clientElbId;
    }
    public void setClientElbId(String clientElbId) {
        this.clientElbId = clientElbId;
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
	public Set<StateMetaDataDefinition> getStateMetaDataDefinition() {
		return stateMetaDataDefinition;
	}
	public void setStateMetaDataDefinition(Set<StateMetaDataDefinition> stateMetaDataDefinition) {
	    this.stateMetaDataDefinition = stateMetaDataDefinition;
	}

    public Set<StateTransitionDefinition> getStateTransitionDefinition() {
        return stateTransitionDefinition;
    }
    public void setStateTransitionDefinition(Set<StateTransitionDefinition> stateTransitionDefinition) {
        this.stateTransitionDefinition = stateTransitionDefinition;
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
    public Map<EventMetaDataDefinition, EventData> getEventDataMap() {
        Map<EventMetaDataDefinition, EventData> eventDataMap = new HashMap<>();
        for (StateMetaDataDefinition aState : this.stateMetaDataDefinition) {
            final List<EventMetaDataDefinition> dependenciesForCurrentState = aState.getDependencies();
            for (EventMetaDataDefinition anEventMetaDefinition : dependenciesForCurrentState) {
                eventDataMap.putIfAbsent(anEventMetaDefinition, retrieveEventDataFor(anEventMetaDefinition));
            }
            final EventMetaDataDefinition outputEventMetaDefiniton = aState.getOutputEvent();
            if (outputEventMetaDefiniton  != null) {
                eventDataMap.put(outputEventMetaDefiniton, retrieveEventDataFor(outputEventMetaDefiniton));
            }
        }
        return eventDataMap;
    }

    private EventData retrieveEventDataFor(EventMetaDataDefinition anEventMetaDefiniton) {
        for (EventData someData : this.eventData) {
            if (someData.isFor(anEventMetaDefiniton)) {
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
        return eventData.equals(that.eventData);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + version.hashCode();
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + eventData.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "StateMachineDefinition{" +
            "description='" + description + '\'' +
            ", name='" + name + '\'' +
            ", version=" + version +
            ", eventData=" + eventData +
            '}';
    }

    public void addEventDatas(EventData[] events) {
        Collections.addAll(this.eventData, events);
    }
}
