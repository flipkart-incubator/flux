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
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author akif.khan
 */
@Entity
@Table(name = "StateMetaData")
@IdClass(StateMetaData.StateMetaDataPK.class)
public class StateMetaData {

    /**
     * Unique identifier of the state
     */
    @Id
    private Long id;

    /**
     * Id of the state machine to which this state belongs
     */
    @Id
    private String stateMachineId;

    /* Defined by the User */
    /**
     * Version for this State
     */
    private Long version;

    /**
     * The name of this State
     */
    private String name;

    /**
     * Description for this State
     */
    private String description;

    /**
     * Name of Task class that is executed when the transition happens to this State, must be a public class
     */
    private String task;

    /**
     * The max retry count for a successful transition
     */
    private Long retryCount;
    /**
     * Timeout for state transition
     */
    private Long timeout;
    /**
     * List of event names this state is dependent on
     */
    @Type(type = "ListJsonType")
    private List<String> dependencies;

    private String outputEvent;
    
    /**
     * Time at which this State has been created
     */
    private Timestamp createdAt;
    
    /**
     * Constructors
     */
    protected StateMetaData() {
        super();
        dependencies = new LinkedList<>();
    }

    public StateMetaData(Long version, String name, String description, String task, List<String> dependencies,
                 Long retryCount, Long timeout, String outputEvent,
                 String stateMachineId, Long id) {
        this();
        this.version = version;
        this.name = name;
        this.description = description;
        this.task = task;
        this.dependencies = dependencies;
        this.retryCount = retryCount;
        this.timeout = timeout;
        this.outputEvent = outputEvent;
        this.stateMachineId = stateMachineId;
        this.id = id;
    }

    /**
     * Used to check whether the state has all its dependencies met based on the input set of event names
     *
     * @param receivedEvents - Input set containing event names of all events received so far
     * @return true if dependency is completely satisfied
     */
    public boolean isDependencySatisfied(Set<String> receivedEvents) {
        return receivedEvents.containsAll(this.dependencies);
    }

    /**
     * Accessor/Mutator methods
     */
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

    public String getStateMachineId() {
        return stateMachineId;
    }

    public void setStateMachineId(String stateMachineId) {
        this.stateMachineId = stateMachineId;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
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

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public String getOutputEvent() {
        return outputEvent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StateMetaData)) return false;

        StateMetaData stateMetaData = (StateMetaData) o;

        if (createdAt != null ? !createdAt.equals(stateMetaData.createdAt) : stateMetaData.createdAt != null) return false;
        if (description != null ? !description.equals(stateMetaData.description) : stateMetaData.description != null) return false;
        if (name != null ? !name.equals(stateMetaData.name) : stateMetaData.name != null) return false;
        if (outputEvent != null ? !outputEvent.equals(stateMetaData.outputEvent) : stateMetaData.outputEvent != null) return false;
        if (retryCount != null ? !retryCount.equals(stateMetaData.retryCount) : stateMetaData.retryCount != null) return false;
        if (stateMachineId != null ? !stateMachineId.equals(stateMetaData.stateMachineId) : stateMetaData.stateMachineId != null)
            return false;
        if (task != null ? !task.equals(stateMetaData.task) : stateMetaData.task != null) return false;
        if (timeout != null ? !timeout.equals(stateMetaData.timeout) : stateMetaData.timeout != null) return false;
        if (version != null ? !version.equals(stateMetaData.version) : stateMetaData.version != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = version != null ? version.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (stateMachineId != null ? stateMachineId.hashCode() : 0);
        result = 31 * result + (task != null ? task.hashCode() : 0);
        result = 31 * result + (outputEvent != null ? outputEvent.hashCode() : 0);
        result = 31 * result + (retryCount != null ? retryCount.hashCode() : 0);
        result = 31 * result + (timeout != null ? timeout.hashCode() : 0);
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
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
                ", task='" + task + '\'' +
                ", outputEvent='" + outputEvent + '\'' +
                ", retryCount=" + retryCount +
                ", timeout=" + timeout +
                ", dependencies=" + dependencies +
                ", createdAt=" + createdAt +
                '}';
    }
    
    static class StateMetaDataPK implements Serializable {

        private Long id;

        private String stateMachineId;

        /**
         * for Hibernate
         */
        public StateMetaDataPK() {
        }

        public StateMetaDataPK(Long id, String stateMachineId) {
            this.id = id;
            this.stateMachineId = stateMachineId;
        }

        public Long getId() {
            return id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof StateMetaDataPK)) return false;

            StateMetaDataPK stateMetaDataPK = (StateMetaDataPK) o;

            if (!getId().equals(stateMetaDataPK.getId())) return false;
            return getStateMachineId().equals(stateMetaDataPK.getStateMachineId());

        }

        @Override
        public int hashCode() {
            int result = getId().hashCode();
            result = 31 * result + getStateMachineId().hashCode();
            return result;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getStateMachineId() {
            return stateMachineId;
        }

        public void setStateMachineId(String stateMachineId) {
            this.stateMachineId = stateMachineId;
        }
    }
}
