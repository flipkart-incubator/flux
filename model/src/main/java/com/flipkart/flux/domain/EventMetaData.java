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

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author akif.khan
 */

@Entity
@Table(name = "EventMetaData")
@IdClass(EventMetaData.EventMetaDataPK.class)
public class EventMetaData implements Serializable {

    /**
     * Default serial version UID
     */
    private static final long serialVersionUID = 1L;

    @Id
    /** The name for this Event*/
    private String name;

    /**
     * The type of this Event
     */
    private String type;

    /**
     * Instance Id of state machine with which this event is associated
     */
    @Id
    private String stateMachineInstanceId;

    /**
     * Set of states dependent on this event
     */
    private String dependentStates;

    /**
     * Event creation time
     */
    private Timestamp createdAt;

    /**
     * Default constructor needed for hibernate entity
     */
    public EventMetaData(){

    }

    public EventMetaData(String name, String type, String stateMachineInstanceId, String dependentStates) {
        this.name = name;
        this.type = type;
        this.stateMachineInstanceId = stateMachineInstanceId;
        this.dependentStates = dependentStates;
    }

    /**
     * Accessor/Mutator methods
     */
    public void setName(String name) {
        this.name = name;
    }

    public String getStateMachineInstanceId() {
        return stateMachineInstanceId;
    }

    public void setStateMachineInstanceId(String stateMachineInstanceId) {
        this.stateMachineInstanceId = stateMachineInstanceId;
    }

    public String getDependentStates() {
        return dependentStates;
    }

    public void setDependentStates(String dependentStates) {
        this.dependentStates = dependentStates;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventMetaData)) return false;

        EventMetaData eventMetaData = (EventMetaData) o;

        if (name != null ? !name.equals(eventMetaData.name) : eventMetaData.name != null) return false;
        if (stateMachineInstanceId != null ? !stateMachineInstanceId.equals(eventMetaData.stateMachineInstanceId) : eventMetaData.stateMachineInstanceId != null)
            return false;
        if (dependentStates != eventMetaData.dependentStates) return false;
        if (type != null ? !type.equals(eventMetaData.type) : eventMetaData.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (stateMachineInstanceId != null ? stateMachineInstanceId.hashCode() : 0);
        result = 31 * result + (dependentStates != null ? dependentStates.hashCode() : 0);
        return result;
    }

    /**
     * <code>EventMetaDataPK</code> is the composite primary key of "EventMetaData" table in DB.
     */
    static class EventMetaDataPK implements Serializable {

        private String stateMachineInstanceId;
        private String name;

        /**
         * for Hibernate
         */
        public EventMetaDataPK() {
        }

        public EventMetaDataPK(String stateMachineInstanceId, String name) {
            this.stateMachineInstanceId = stateMachineInstanceId;
            this.name = name;
        }

        public String getStateMachineInstanceId() {
            return stateMachineInstanceId;
        }

        public void setStateMachineInstanceId(String stateMachineInstanceId) {
            this.stateMachineInstanceId = stateMachineInstanceId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof EventMetaDataPK)) return false;

            EventMetaDataPK eventMetaDataPK = (EventMetaDataPK) o;

            if (!getStateMachineInstanceId().equals(eventMetaDataPK.getStateMachineInstanceId())) return false;
            return getName().equals(eventMetaDataPK.getName());

        }

        @Override
        public int hashCode() {
            int result = getStateMachineInstanceId().hashCode();
            result = 31 * result + getName().hashCode();
            return result;
        }
    }
}
