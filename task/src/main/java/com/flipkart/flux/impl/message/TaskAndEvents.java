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
 *
 */

package com.flipkart.flux.impl.message;

import com.flipkart.flux.api.EventData;
import com.flipkart.flux.api.core.Task;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.impl.task.AkkaTask;

import java.io.Serializable;
import java.util.Arrays;

/**
 * <code>TaskAndEvents</code> is a message that composes a {@link Task} identifier and the {@link Event}S that it processes.
 * Used in invoking the {@link AkkaTask} Actor in akka.
 *
 * @author yogesh.nachnani
 *
 */
public class TaskAndEvents implements Serializable {
	/** Default serialversion UID*/
	private static final long serialVersionUID = 1L;
	/* The task name*/
	private String taskName;
    /* The string that uniquely identifies a client task to be executed */
    private String taskIdentifier;
    /* The Task instance identifier in persistence store*/
    private Long taskId;
    /* The set of events that have presently unblocked the task and whose data will be utilised during task execution */
    private EventData[] events;
    /* The state machine id for which this execution message is raised */
    private Long stateMachineId;
    /* The state machine name for which this execution message is raised */
    private String stateMachineName;
    /* Serialised output event definition */
    private String outputEvent;
    /* The max retry count*/
    private long retryCount;
    /* The current retry count*/
    private long currentRetryCount;
    /* Indicates whether this task is getting executed for the first time, useful in incrementing attempted no. retries of task*/
    private boolean isFirstTimeExecution;

    /** constructors*/
    public TaskAndEvents() {}
    public TaskAndEvents(String taskName, String taskIdentifier, Long taskId, EventData[] events, Long stateMachineId, String stateMachineName, String outputEvent, long retryCount) {
    	this.taskName = taskName;
        this.taskIdentifier = taskIdentifier;
        this.taskId = taskId;
        this.events = events;
        this.stateMachineId = stateMachineId;
        this.stateMachineName = stateMachineName;
        this.outputEvent = outputEvent;
        this.retryCount = retryCount;
    }

    public TaskAndEvents(String taskName, String taskIdentifier, Long taskId, EventData[] events, Long stateMachineId, String stateMachineName, String outputEvent, long retryCount, long currentRetryCount) {
        this(taskName, taskIdentifier, taskId, events, stateMachineId, stateMachineName, outputEvent, retryCount);
        this.currentRetryCount = currentRetryCount;
    }

    public String getTaskName() {
		return taskName;
	}
	public String getTaskIdentifier() {
        return taskIdentifier;
    }
    public Long getTaskId() {
		return taskId;
	}
	public EventData[] getEvents() {
        return this.events;
    }
    public Long getStateMachineId() {
        return stateMachineId;
    }
    public String getStateMachineName() {
        return stateMachineName;
    }
    public String getOutputEvent() {
        return outputEvent;
    }
    public long getRetryCount() {
		return retryCount;
	}
	public long getCurrentRetryCount() {
		return currentRetryCount;
	}
	public void setCurrentRetryCount(long currentRetryCount) {
		this.currentRetryCount = currentRetryCount;
	}
    public boolean getIsFirstTimeExecution() {
        return isFirstTimeExecution;
    }
    public void setFirstTimeExecution(boolean firstTimeExecution) {
        isFirstTimeExecution = firstTimeExecution;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaskAndEvents that = (TaskAndEvents) o;

        if (!taskIdentifier.equals(that.taskIdentifier)) return false;
        if (!taskId.equals(that.taskId)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(events, that.events)) return false;
        if (!stateMachineId.equals(that.stateMachineId)) return false;
        return !(outputEvent != null ? !outputEvent.equals(that.outputEvent) : that.outputEvent != null);

    }

    @Override
    public int hashCode() {
        int result = taskIdentifier.hashCode();
        result = 31 * result + (events != null ? Arrays.hashCode(events) : 0);
        result = 31 * result + taskId.hashCode();
        result = 31 * result + stateMachineId.hashCode();
        result = 31 * result + (outputEvent != null ? outputEvent.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TaskAndEvents{" +
            "events=" + Arrays.toString(events) +
            ", taskIdentifier='" + taskIdentifier + '\'' +
            ", taskId=" + taskId +
            ", stateMachineId=" + stateMachineId +
            ", stateMachineName=" + stateMachineName +
            ", outputEvent='" + outputEvent + '\'' +
            '}';
    }
}
