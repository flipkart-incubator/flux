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

import java.util.Objects;

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

    /** Name of the event*/
    private String name;

    /** Type of the event*/
    private String type;

	/* To be used only by jackson */
	EventDefinition() {
	}

	/** Constructor*/
	public EventDefinition(String name, String type) {
		super();
		this.name = name;
        this.type = type;
	}

    /** Accessors/Mutators for member variables*/
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventDefinition))
            return false;

        EventDefinition that = (EventDefinition) o;

        if (!Objects.equals(name, that.name))
            return false;

        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EventDefinition{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
