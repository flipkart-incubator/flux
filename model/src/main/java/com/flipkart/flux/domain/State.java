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

import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * <code>State</code> represents the current state of the StateMachine. This implementation also supports integration with user defined code that is executed when the 
 * state transition happens. User code can be integrated using {@link Hook} and {@link Task}. Hooks are added on entry or exit of this State while Task is executed when the 
 * transition is in progress. The outcome of Hook execution does not impact state transition whereas a failed Task execution will abort the transition.
 *
 * @author Yogesh
 * @author regunath.balasubramanian
 * @author shyam.akirala
 * @author kartik.bommepally
 */
@Entity
@Table(name = "States")
public class State {

    /** Unique identifier of the state*/
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* Defined by the User */
    /** Version for this State*/
    private Long version;
    /** The name of this State*/
    private String name;
    /** Description for this State*/
    private String description;
    /** Id of the state machine to which this state belongs*/
    private Long stateMachineId;
    /** Name of Hook class that is executed on entry of this State, must be a public class*/
    private String onEntryHook;
    /** Name of Task class that is executed when the transition happens to this State, must be a public class*/
    private String task;
    /** Name of Hook class that is executed on exit of this State, must be a public class*/
    private String onExitHook;
    /** The max retry count for a successful transition*/
    private Long retryCount;
    /** Timeout for state transition*/
    private Long timeout;
    /** List of event names this state is dependent on*/
    @Type(type = "ListJsonType")
    private List<String> dependencies;

    private String outputEvent;

    /* Maintained by the execution engine */
    /** The Status of state transition execution*/
    @Enumerated(EnumType.STRING)
    private Status status;

    /** The rollback status*/
    @Enumerated(EnumType.STRING)
    private Status rollbackStatus;

    /** The number of retries attempted*/
    private Long attemptedNoOfRetries;

    /** Time at which this State has been created */
    private Timestamp createdAt;

    /** Time at which this State has been last updated */
    @Column(updatable = false)
    private Timestamp updatedAt;


    /** Constructors */
    protected State() {
        super();
        dependencies = new LinkedList<>();
    }
    public State(Long version, String name, String description, String onEntryHook, String task, String onExitHook, List<String> dependencies,
                 Long retryCount, Long timeout, String outputEvent, Status status, Status rollbackStatus, Long attemptedNoOfRetries) {
        this();
        this.version = version;
        this.name = name;
        this.description = description;
        this.onEntryHook = onEntryHook;
        this.task = task;
        this.onExitHook = onExitHook;
        this.dependencies = dependencies;
        this.retryCount = retryCount;
        this.timeout = timeout;
        this.outputEvent = outputEvent;
        this.status = status;
        this.rollbackStatus = rollbackStatus;
        this.attemptedNoOfRetries = attemptedNoOfRetries;
    }

    /**
     * Used to check whether the state has all its dependencies met based on the input set of event names
     * @param receivedEvents - Input set containing event names of all events received so far
     * @return true if dependency is completely satisfied
     */
    public boolean isDependencySatisfied(Set<String> receivedEvents) {
       return receivedEvents.containsAll(this.dependencies);
    }

    /** Accessor/Mutator methods*/
    public Long getId() {
        return id;
    }
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
    public Long getStateMachineId() {
        return stateMachineId;
    }
    public void setStateMachineId(Long stateMachineId) {
        this.stateMachineId = stateMachineId;
    }
    public String getOnEntryHook() {
        return onEntryHook;
    }
    public void setOnEntryHook(String onEntryHook) {
        this.onEntryHook = onEntryHook;
    }
    public String getTask() {
        return task;
    }
    public void setTask(String task) {
        this.task = task;
    }
    public String getOnExitHook() {
        return onExitHook;
    }
    public void setOnExitHook(String onExitHook) {
        this.onExitHook = onExitHook;
    }
    public List<String> getDependencies() {
        return dependencies;
    }
    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
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
    public Status getStatus() {
        return status;
    }
    public void setStatus(Status status) {
        this.status = status;
    }
    public Status getRollbackStatus() {
        return rollbackStatus;
    }
    public void setRollbackStatus(Status rollbackStatus) {
        this.rollbackStatus = rollbackStatus;
    }
    public Long getAttemptedNoOfRetries() {
        return attemptedNoOfRetries;
    }
    public void setAttemptedNoOfRetries(Long attemptedNoOfRetries) {
        this.attemptedNoOfRetries = attemptedNoOfRetries;
    }

    public String getOutputEvent() {
        return outputEvent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof State)) return false;

        State state = (State) o;

        if (createdAt != null ? !createdAt.equals(state.createdAt) : state.createdAt != null) return false;
        if (description != null ? !description.equals(state.description) : state.description != null) return false;
        if (name != null ? !name.equals(state.name) : state.name != null) return false;
        if (attemptedNoOfRetries != null ? !attemptedNoOfRetries.equals(state.attemptedNoOfRetries) : state.attemptedNoOfRetries != null) return false;
        if (onEntryHook != null ? !onEntryHook.equals(state.onEntryHook) : state.onEntryHook != null) return false;
        if (onExitHook != null ? !onExitHook.equals(state.onExitHook) : state.onExitHook != null) return false;
        if (outputEvent != null ? !outputEvent.equals(state.outputEvent) : state.outputEvent != null) return false;
        if (retryCount != null ? !retryCount.equals(state.retryCount) : state.retryCount != null) return false;
        if (rollbackStatus != state.rollbackStatus) return false;
        if (stateMachineId != null ? !stateMachineId.equals(state.stateMachineId) : state.stateMachineId != null)
            return false;
        if (status != state.status) return false;
        if (task != null ? !task.equals(state.task) : state.task != null) return false;
        if (timeout != null ? !timeout.equals(state.timeout) : state.timeout != null) return false;
        if (updatedAt != null ? !updatedAt.equals(state.updatedAt) : state.updatedAt != null) return false;
        if (version != null ? !version.equals(state.version) : state.version != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = version != null ? version.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (stateMachineId != null ? stateMachineId.hashCode() : 0);
        result = 31 * result + (onEntryHook != null ? onEntryHook.hashCode() : 0);
        result = 31 * result + (task != null ? task.hashCode() : 0);
        result = 31 * result + (onExitHook != null ? onExitHook.hashCode() : 0);
        result = 31 * result + (outputEvent != null ? outputEvent.hashCode() : 0);
        result = 31 * result + (retryCount != null ? retryCount.hashCode() : 0);
        result = 31 * result + (timeout != null ? timeout.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (rollbackStatus != null ? rollbackStatus.hashCode() : 0);
        result = 31 * result + (attemptedNoOfRetries != null ? attemptedNoOfRetries.hashCode() : 0);
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        result = 31 * result + (updatedAt != null ? updatedAt.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "State{" +
                "id=" + id +
                ", version=" + version +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", stateMachineId=" + stateMachineId +
                ", onEntryHook='" + onEntryHook + '\'' +
                ", task='" + task + '\'' +
                ", onExitHook='" + onExitHook + '\'' +
                ", outputEvent='" + outputEvent + '\'' +
                ", retryCount=" + retryCount +
                ", timeout=" + timeout +
                ", dependencies=" + dependencies +
                ", status=" + status +
                ", rollbackStatus=" + rollbackStatus +
                ", attemptedNoOfRetries=" + attemptedNoOfRetries +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
