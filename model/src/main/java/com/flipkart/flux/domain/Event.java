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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * <code>Event</code> is the result of a {@link Task} execution.
 * It is to be posted back to the Flux execution engine once a worker has executed the task
 * 
 * @author Yogesh
 * @author regunath.balasubramanian
 * @author shyam.akirala
 */

@Entity
@Table(name="events")
public class Event<T> implements Serializable {

    /** Default serial version UID*/
    private static final long serialVersionUID = 1L;

    /** Auto generated Id*/
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

	/** The name for this Event*/
    private String name;
    
    /** The type of this Event*/
    private String type;
    
    /** Status for this Event*/
    private EventStatus status;

    /** Instance Id of state machine with which this event is associated */
    private String stateMachineInstanceId;
    
    /** Data associated with this Event, must be serializable*/
    private T eventData;

    /** The source who generated this Event */
    private String eventSource;

    /** Constructor*/
    public Event(String name, String type) {
		super();
		this.name = name;
		this.type = type;
	}

	/** Enum of Event statuses*/
    public enum EventStatus {
        pending,triggered;
    }

    /** Accessor/Mutator methods*/
	public EventStatus getStatus() {
		return status;
	}
	public void setStatus(EventStatus status) {
		this.status = status;
	}
    public String getStateMachineInstanceId() {
        return stateMachineInstanceId;
    }
    public void setStateMachineInstanceId(String stateMachineInstanceId) {
        this.stateMachineInstanceId = stateMachineInstanceId;
    }
    public T getEventData() {
		return eventData;
	}
	public void setEventData(T eventData) {
		this.eventData = eventData;
	}
    public String getEventSource() {
        return eventSource;
    }
    public void setEventSource(String eventSource) {
        this.eventSource = eventSource;
    }
    public String getName() {
		return name;
	}
	public String getType() {
		return type;
	}
    
}
