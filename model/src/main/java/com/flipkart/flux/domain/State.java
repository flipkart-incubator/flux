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
import java.util.List;

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
public class State<T> {

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
    /** Id of the State Machine to which this State belongs*/
    private Long stateMachineId;
    /** Hook that is executed on entry of this State, must be a public class*/
    @Type(type = "StoreFQNOnly")
    private Hook<T> onEntryHook;
    /** Task that is executed when the transition happens to this State, must be a public class*/
    @Type(type = "StoreFQNOnly")
    private Task<T> task;
    /** Hook that is executed on exit of this State, must be a public class*/
    @Type(type = "StoreFQNOnly")
    private Hook<T> onExitHook;
    /** The max retry count for a successful transition*/
    private Long retryCount;
    /** Timeout for state transition*/
    private Long timeout;

    /* Maintained by the execution engine */
    /** List of errors during state transition*/
    @Transient
    private List<FluxError> errors;

    /** The Status of state transition execution*/
    @Transient
    private Status status;

    /** The rollback status*/
    @Transient
    private Status rollbackStatus;

    /** The number of retries attempted*/
    @Transient
    private Long numRetries;

    /** Time at which this State has been created */
    private Timestamp createdAt;

    /** Time at which this State has been last updated */
    private Timestamp updatedAt;


    /** Constructors */
    protected State() {}
    public State(Long version, String name, String description, Hook<T> onEntryHook, Task<T> task, Hook<T> onExitHook,
                 Long retryCount, Long timeout) {
        super();
        this.version = version;
        this.name = name;
        this.description = description;
        this.onEntryHook = onEntryHook;
        this.task = task;
        this.onExitHook = onExitHook;
        this.retryCount = retryCount;
        this.timeout = timeout;
    }

    /**
     * The entry method to state transition. Executes the {@link Task} associated with this State and signals a transition to the next state on successful execution.
     * @param context the Task execution context
     */
    public void enter(Context<T> context) {
        // 1. Begin execution of the task
        // 2. Set next state
        // The return value of the task can either be returned from here, or if we go truly async then
        // the worker executing the task can "Post" it back to the WF engine.
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
    public Hook<T> getOnEntryHook() {
        return onEntryHook;
    }
    public void setOnEntryHook(Hook<T> onEntryHook) {
        this.onEntryHook = onEntryHook;
    }
    public Task<T> getTask() {
        return task;
    }
    public void setTask(Task<T> task) {
        this.task = task;
    }
    public Hook<T> getOnExitHook() {
        return onExitHook;
    }
    public void setOnExitHook(Hook<T> onExitHook) {
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
    public List<FluxError> getErrors() {
        return errors;
    }
    public void setErrors(List<FluxError> errors) {
        this.errors = errors;
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
    public Long getNumRetries() {
        return numRetries;
    }
    public void setNumRetries(Long numRetries) {
        this.numRetries = numRetries;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        State<T> other = (State<T>) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
