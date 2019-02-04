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
@Table(name = "StateTransition")
@IdClass(StateTransition.StateTransitionPK.class)
public class StateTransition {

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

    /**
     * executionVersion for this State
     */
    private Long executionVersion;

    /**
     * The name of this State
     */
    private String name;

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
     * Time at which this State has been last updated
     */
    private Timestamp updatedAt;

    /**
     * Maintained by the execution engine
     * The Status of state transition execution
     */
    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Validity validity;

    /**
     * The rollback status
     */
    @Enumerated(EnumType.STRING)
    private Status rollbackStatus;

    /**
     * The number of retries attempted
     */
    private Long attemptedNoOfRetries;

    /**
     * Constructors
     */
    protected StateTransition() {
        super();
        dependencies = new LinkedList<>();
    }

    public StateTransition(Long executionVersion, String name, List<String> dependencies,
                         String outputEvent, Status status, Status rollbackStatus,
                           Long attemptedNoOfRetries, Validity validity, String stateMachineId, Long id) {
        this();
        this.executionVersion = executionVersion;
        this.name = name;
        this.dependencies = dependencies;
        this.outputEvent = outputEvent;
        this.status = status;
        this.rollbackStatus = rollbackStatus;
        this.attemptedNoOfRetries = attemptedNoOfRetries;
        this.validity = validity;
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

    public Long getExecutionVersion() {
        return executionVersion;
    }

    public void setExecutionVersion(Long executionVersion) {
        this.executionVersion = executionVersion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStateMachineId() {
        return stateMachineId;
    }

    public void setStateMachineId(String stateMachineId) {
        this.stateMachineId = stateMachineId;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
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
        if (!(o instanceof StateTransition)) return false;

        StateTransition stateTransition = (StateTransition) o;

        if (createdAt != null ? !createdAt.equals(stateTransition.createdAt) : stateTransition.createdAt != null) return false;
        if (name != null ? !name.equals(stateTransition.name) : stateTransition.name != null) return false;
        if (outputEvent != null ? !outputEvent.equals(stateTransition.outputEvent) : stateTransition.outputEvent != null) return false;
        if (stateMachineId != null ? !stateMachineId.equals(stateTransition.stateMachineId) : stateTransition.stateMachineId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = executionVersion != null ? executionVersion.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (stateMachineId != null ? stateMachineId.hashCode() : 0);
        result = 31 * result + (outputEvent != null ? outputEvent.hashCode() : 0);
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "State{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", stateMachineId=" + stateMachineId +
                ", outputEvent='" + outputEvent + '\'' +
                ", dependencies=" + dependencies +
                ", createdAt=" + createdAt +
                '}';
    }

    static class StateTransitionPK implements Serializable {

        private Long id;

        private String stateMachineId;

        /**
         * for Hibernate
         */
        public StateTransitionPK() {
        }

        public StateTransitionPK(Long id, String stateMachineId) {
            this.id = id;
            this.stateMachineId = stateMachineId;
        }

        public Long getId() {
            return id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof StateTransitionPK)) return false;

            StateTransitionPK stateTransitionPK = (StateTransitionPK) o;

            if (!getId().equals(stateTransitionPK.getId())) return false;
            return getStateMachineId().equals(stateTransitionPK.getStateMachineId());

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
