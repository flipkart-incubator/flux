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

import java.util.LinkedList;
import java.util.List;

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
    
    /** The list of EventTransitionDefinitionS that this state definition is dependent on for a transition into*/
    private List<EventTransitionDefinition> dependencies;

	private EventTransitionDefinition outputEvent;

	/* Used only by Jackson */
	StateDefinition() {
		super();
		this.dependencies = new LinkedList<>();
	}

	/** Constructor*/
	public StateDefinition(Long version, String name, String description, String onEntryHook, String task, String onExitHook,
						   Long retryCount, Long timeout, List<EventTransitionDefinition> dependencies, EventTransitionDefinition outputEvent) {
		this();
		this.version = version;
		this.name = name;
        this.description = description;
        this.onEntryHook = onEntryHook;
		this.task = task;
        this.onExitHook = onExitHook;
		this.retryCount = retryCount;
		this.timeout = timeout;
		this.dependencies = dependencies;
		this.outputEvent = outputEvent;
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
	public List<EventTransitionDefinition> getDependencies() {
		return dependencies;
	}
	public void setDependencies(List<EventTransitionDefinition> dependencies) {
		this.dependencies = dependencies;
	}

	public EventTransitionDefinition getOutputEvent() {
		return outputEvent;
	}

	public void setOutputEvent(EventTransitionDefinition outputEvent) {
		this.outputEvent = outputEvent;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		StateDefinition that = (StateDefinition) o;

		if (version != null ? !version.equals(that.version) : that.version != null) return false;
		if (name != null ? !name.equals(that.name) : that.name != null) return false;
		if (description != null ? !description.equals(that.description) : that.description != null) return false;
		if (onEntryHook != null ? !onEntryHook.equals(that.onEntryHook) : that.onEntryHook != null) return false;
		if (task != null ? !task.equals(that.task) : that.task != null) return false;
		if (onExitHook != null ? !onExitHook.equals(that.onExitHook) : that.onExitHook != null) return false;
		if (outputEvent != null ? !outputEvent.equals(that.outputEvent) : that.outputEvent != null) return false;
		if (retryCount != null ? !retryCount.equals(that.retryCount) : that.retryCount != null) return false;
		if (timeout != null ? !timeout.equals(that.timeout) : that.timeout != null) return false;
		return !(dependencies != null ? !dependencies.equals(that.dependencies) : that.dependencies != null);

	}

	@Override
	public int hashCode() {
		int result = version != null ? version.hashCode() : 0;
		result = 31 * result + (name != null ? name.hashCode() : 0);
		result = 31 * result + (description != null ? description.hashCode() : 0);
		result = 31 * result + (onEntryHook != null ? onEntryHook.hashCode() : 0);
		result = 31 * result + (task != null ? task.hashCode() : 0);
		result = 31 * result + (onExitHook != null ? onExitHook.hashCode() : 0);
		result = 31 * result + (outputEvent != null ? outputEvent.hashCode() : 0);
		result = 31 * result + (retryCount != null ? retryCount.hashCode() : 0);
		result = 31 * result + (timeout != null ? timeout.hashCode() : 0);
		result = 31 * result + (dependencies != null ? dependencies.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "StateDefinition{" +
			"dependencies=" + dependencies +
			", version=" + version +
			", name='" + name + '\'' +
			", description='" + description + '\'' +
			", onEntryHook='" + onEntryHook + '\'' +
			", task='" + task + '\'' +
			", onExitHook='" + onExitHook + '\'' +
			", outputEvent='" + outputEvent + '\'' +
			", retryCount=" + retryCount +
			", timeout=" + timeout +
			'}';
	}
}
