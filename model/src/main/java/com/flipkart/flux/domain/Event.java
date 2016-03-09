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

import java.io.Serializable;

/**
 * <code>Event</code> is the result of a {@link Task} execution.
 * It is to be posted back to the Flux execution engine once a worker has executed the task
 * 
 * @author Yogesh
 * @author regunath.balasubramanian
 * @author shyam.akirala
 */
public class Event implements Serializable {
	
	/** The name for this Event*/
    private String name;
    /** The type of this Event*/
    private String type;
    /** Staus for this Event*/
    private EventStatus status;
    /** Data associated with this Event*/
    private Object eventData;

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
	public Object getEventData() {
		return eventData;
	}
	public void setEventData(Object eventData) {
		this.eventData = eventData;
	}
	public String getName() {
		return name;
	}
	public String getType() {
		return type;
	}
    
}
