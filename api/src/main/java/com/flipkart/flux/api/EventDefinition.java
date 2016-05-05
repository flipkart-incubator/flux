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

/**
 * <code>EventDefinition</code> define an event to the system
 * An event is a named object of a certain type (say a java.lang.String with name foo)
 * 
 * @author Yogesh
 * @author regunath.balasubramanian
 * @author shyam.akirala
 */
public class EventDefinition {
	
	/** The Event FQN*/
    private String eventFqn; // java.lang.String_foo

	/* To be used only by jackson */
	EventDefinition() {
	}

	/** Constructor*/
	public EventDefinition(String eventFqn) {
		super();
		this.eventFqn = eventFqn;
	}

    /** Accessors/Mutators for member variables*/
	public String getEventFqn() {
		return eventFqn;
	}
	public void setEventFqn(String eventFqn) {
		this.eventFqn = eventFqn;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		EventDefinition that = (EventDefinition) o;

		return !(eventFqn != null ? !eventFqn.equals(that.eventFqn) : that.eventFqn != null);

	}

	@Override
	public int hashCode() {
		return eventFqn != null ? eventFqn.hashCode() : 0;
	}

	@Override
	public String toString() {
		return "EventDefinition{" +
			"eventFqn='" + eventFqn + '\'' +
			'}';
	}
}
