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

package com.flipkart.flux.api;

import java.util.Set;

/**
 * <Code>StateDefinition</Code> models a State, an action associated with the state and the list of dependencies for the state transition to occur.
 * For flux, this is equivalent to a "State" in the state machine runtime.
 * 
 *  @author Yogesh
 *  @author regunath.balasubramanian
 *  @author shyam.akirala
 *  @author kartik.bommepally
 */
public class StateDefinition {
	
	/** The version of this state definition*/
    private Long version;
    
    /** Name of this state definition*/
    private String name;

    /** Description of this state definition*/
    private String description;

    /** Name of Hook class that will be executed on entry of this state*/
    private String onEntryHook;

    /** Name of Task class that will be executed when the state machine transitions to the state*/
    private String task;

    /** Name of Hook class that will be executed on exit of this state*/
    private String onExitHook;

    /** Retry count for task execution*/    
    private Long retryCount;
    
    /** The timeout in millis for each attempt to execute the Task*/
    private Long timeout;
    
    /** The list of EventDefinitionS that this state definition is dependent on for a transition into*/
    private Set<EventDefinition> dependencies;

    /** Constructor*/
    public StateDefinition() {}
	public StateDefinition(Long version, String name, String description, String onEntryHook, String task, String onExitHook,
                           Long retryCount, Long timeout, Set<EventDefinition> dependencies) {
		super();
		this.version = version;
		this.name = name;
        this.description = description;
        this.onEntryHook = onEntryHook;
		this.task = task;
        this.onExitHook = onExitHook;
		this.retryCount = retryCount;
		this.timeout = timeout;
		this.dependencies = dependencies;
	}

    /** Accessors/Mutators for member variables*/
	public Long getVersion() {
		return version;
	}
	public void setVersion(Long version) {
		this.version = version;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
	public String getTask() {
		return task;
	}
	public void setTask(String task) {
		this.task = task;
	}
    public String getOnEntryHook() {
        return onEntryHook;
    }
    public void setOnEntryHook(String onEntryHook) {
        this.onEntryHook = onEntryHook;
    }
    public String getOnExitHook() {
        return onExitHook;
    }
    public void setOnExitHook(String onExitHook) {
        this.onExitHook = onExitHook;
    }
	public Long getRetryCount() {
		return retryCount;
	}
	public void setRetryCount(Long retryCount) {
		this.retryCount = retryCount;
	}
	public Long getTimeout() {
		return timeout;
	}
	public void setTimeout(Long timeout) {
		this.timeout = timeout;
	}
	public Set<EventDefinition> getDependencies() {
		return dependencies;
	}
	public void setDependencies(Set<EventDefinition> dependencies) {
		this.dependencies = dependencies;
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        @SuppressWarnings("unchecked")
        StateDefinition other = (StateDefinition) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (version == null) {
            if (other.version != null)
                return false;
        } else if (!version.equals(other.version))
            return false;
        return true;
    }
}
