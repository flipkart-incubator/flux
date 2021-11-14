/*
 * Copyright 2012-2019, the original author or authors.
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
import java.util.Objects;

/**
 * <code>StateTraversalPath</code> is used to store list of states and events in the traversal paths of replayable
 * states in a state machine.
 *
 * @author akif.khan
 */
@Entity
@Table(name = "StateTraversalPath")
@IdClass(StateTraversalPath.StateTraversalPathPK.class)
public class StateTraversalPath {

    /**
     * Unique identifier of the state
     */
    @Id
    private Long stateId;

    /**
     * Id of the state machine to which this state belongs
     */
    @Id
    private String stateMachineId;

    /**
     * List of event names in the traversal path of this stateId
     */
    @Type(type = "ListJsonType")
    private List<String> nextDependentEvents;

    /**
     * List of stateIds in the traversal path of this stateId
     */
    @Type(type = "ListJsonType")
    private List<Long> nextDependentStates;


    /**
     * Time at which this Traversal Path Mapping  has been created
     */
    private Timestamp createdAt;

    /**
     * Constructors
     */
    protected StateTraversalPath() {
        super();
        nextDependentEvents = new LinkedList<>();
        nextDependentStates = new LinkedList<>();
    }

    public StateTraversalPath(String stateMachineId, Long stateId, List<String> nextDependentEvents,
                              List<Long> nextDependentStates) {
        this();
        this.stateMachineId = stateMachineId;
        this.stateId = stateId;
        this.nextDependentEvents = nextDependentEvents;
        this.nextDependentStates = nextDependentStates;

    }

    /**
     * Accessor/Mutator methods
     */
    public Long getStateId() {
        return stateId;
    }

    public void setStateId(Long stateId) {
        this.stateId = stateId;
    }

    public String getStateMachineId() {
        return stateMachineId;
    }

    public void setStateMachineId(String stateMachineId) {
        this.stateMachineId = stateMachineId;
    }

    public List<String> getNextDependentEvents() {
        return nextDependentEvents;
    }

    public void setNextDependentEvents(List<String> nextDependentEvents) {
        this.nextDependentEvents = nextDependentEvents;
    }

    public List<Long> getNextDependentStates() {
        return nextDependentStates;
    }

    public void setNextDependentStates(List<Long> nextDependentStates) {
        this.nextDependentStates = nextDependentStates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StateTraversalPath)) return false;

        StateTraversalPath stateTraversalPath = (StateTraversalPath) o;

        if (!Objects.equals(createdAt, stateTraversalPath.createdAt))
            return false;
        if (!Objects.equals(stateMachineId, stateTraversalPath.stateMachineId))
            return false;
        return Objects.equals(stateId, stateTraversalPath.stateId);
    }

    @Override
    public int hashCode() {
        int result = stateMachineId != null ? stateMachineId.hashCode() : 0;
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        result = 31 * result + (stateId != null ? stateId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "State{" +
                "stateId=" + stateId +
                ", stateMachineId=" + stateMachineId +
                ", nextDependentEvents=" + nextDependentEvents +
                ", nextDependentStates=" + nextDependentStates +
                ", createdAt=" + createdAt +
                '}';
    }

    /**
     * <code>StateTraversalPathPK</code> is the composite primary key of "StateTraversalPath" table in DB.
     */
    static class StateTraversalPathPK implements Serializable {

        private Long stateId;

        private String stateMachineId;

        /**
         * for Hibernate
         */
        public StateTraversalPathPK() {
        }

        public StateTraversalPathPK(Long stateId, String stateMachineId) {
            this.stateId = stateId;
            this.stateMachineId = stateMachineId;
        }

        public Long getStateId() {
            return stateId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof StateTraversalPathPK)) return false;

            StateTraversalPathPK stateTraversalPathPK = (StateTraversalPathPK) o;

            if (!getStateId().equals(stateTraversalPathPK.getStateId())) return false;
            return getStateMachineId().equals(stateTraversalPathPK.getStateMachineId());

        }

        @Override
        public int hashCode() {
            int result = getStateId().hashCode();
            result = 31 * result + getStateMachineId().hashCode();
            return result;
        }

        public void setStateId(Long stateId) {
            this.stateId = stateId;
        }

        public String getStateMachineId() {
            return stateMachineId;
        }

        public void setStateMachineId(String stateMachineId) {
            this.stateMachineId = stateMachineId;
        }
    }
}