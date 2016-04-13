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
 * <code>EventDefinition</code> defines an event to the system
 * An event is a named object of a certain type (say a java.lang.String with name foo)
 * 
 * @author Yogesh
 * @author regunath.balasubramanian
 * @author shyam.akirala
 * @author kartik.bommepally
 */
public class EventDefinition {

    /** The version of this event definition*/
    private Long version;

    /** The Event FQN*/
    private String eventFqn; // java.lang.String_foo

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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((eventFqn == null) ? 0 : eventFqn.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EventDefinition other = (EventDefinition) obj;
        if (eventFqn == null) {
            if (other.eventFqn != null)
                return false;
        } else if (!eventFqn.equals(other.eventFqn))
            return false;
        if (version == null) {
            if (other.version != null)
                return false;
        } else if (!version.equals(other.version))
            return false;
        return true;
    }
}
