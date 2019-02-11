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
@Table(name = "EventTransition")
@IdClass(EventTransition.EventTransitionPK.class)
public class EventTransition implements Serializable {

    /**
     * Default serial version UID
     */
    private static final long serialVersionUID = 1L;

    @Id
    /** The name for this Event*/
    private String name;

    /**
     * Validity for this Event Transition
     */
    @Enumerated(EnumType.STRING)
    private Validity validity;

    /**
     * Validity for this Event Transition
     */
    @Enumerated(EnumType.STRING)
    private EventStatus status;

    /**
     * Instance Id of state machine with which this event is associated
     */
    @Id
    private String stateMachineInstanceId;

    /**
     * Execution Version of the event
     */
    private Integer executionVersion;

    /**
     * Data associated with this Event, stored as serialised json
     */
    private String eventData;

    /**
     * The source who generated this Event
     */
    private String eventSource;

    /**
     * Event creation time
     */
    private Timestamp createdAt;

    /**
     * Time at which this event is last updated
     */
    @Column(updatable = false)
    private Timestamp updatedAt;

    /**
     * Enum of Event statuses
     */
    public enum EventStatus {
        pending, triggered, cancelled;
    }

    /**
     * Default constructor needed for hibernate entity
     */
    public EventTransition(){

    }

    public EventTransition(String name, Validity validity, EventStatus status, String stateMachineInstanceId,
                           Integer executionVersion, String eventData, String eventSource) {
        this.name = name;
        this.validity = validity;
        this.status = status;
        this.stateMachineInstanceId = stateMachineInstanceId;
        this.executionVersion = executionVersion;
        this.eventData = eventData;
        this.eventSource = eventSource;
    }

    /**
     * Accessor/Mutator methods
     */

    public void setName(String name) {
        this.name = name;
    }
    
    public Validity getValidity() {
        return validity;
    }

    public void setValidity(Validity validity) {
        this.validity = validity;
    }

    public Integer getExecutionVersion() {
        return executionVersion;
    }

    public void setExecutionVersion(Integer executionVersion) {
        this.executionVersion = executionVersion;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public String getStateMachineInstanceId() {
        return stateMachineInstanceId;
    }

    public void setStateMachineInstanceId(String stateMachineInstanceId) {
        this.stateMachineInstanceId = stateMachineInstanceId;
    }

    public String getEventData() {
        return eventData;
    }

    public void setEventData(String eventData) {
        this.eventData = eventData;
    }

    public String getEventSource() {
        return eventSource;
    }

    public void setEventSource(String eventSource) {
        this.eventSource = eventSource;
    }

    public String getName() {
        return name;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event)) return false;

        EventTransition eventTransition = (EventTransition) o;

        if (eventData != null ? !eventData.equals(eventTransition.eventData) : eventTransition.eventData != null) return false;
        if (eventSource != null ? !eventSource.equals(eventTransition.eventSource) : eventTransition.eventSource != null) return false;
        if (name != null ? !name.equals(eventTransition.name) : eventTransition.name != null) return false;
        if (stateMachineInstanceId != null ? !stateMachineInstanceId.equals(eventTransition.stateMachineInstanceId) : eventTransition.stateMachineInstanceId != null)
            return false;
        if (status != eventTransition.status) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (stateMachineInstanceId != null ? stateMachineInstanceId.hashCode() : 0);
        result = 31 * result + (eventData != null ? eventData.hashCode() : 0);
        result = 31 * result + (eventSource != null ? eventSource.hashCode() : 0);
        return result;
    }

    /**
     * <code>EventTransitionPK</code> is the composite primary key of "EventTransition" table in DB.
     */
    static class EventTransitionPK implements Serializable {

        private String stateMachineInstanceId;
        private String name;
        private Integer executionVersion;

        /**
         * for Hibernate
         */
        public EventTransitionPK() {
        }

        public EventTransitionPK(String stateMachineInstanceId, String name, Integer executionVersion) {
            this.stateMachineInstanceId = stateMachineInstanceId;
            this.name = name;
            this.executionVersion = executionVersion;
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

        public Integer getExecutionVersion() {
            return executionVersion;
        }

        public void setExecutionVersion(Integer executionVersion) {
            this.executionVersion = executionVersion;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof EventTransitionPK)) return false;

            EventTransitionPK eventTransitionPK = (EventTransitionPK) o;

            if (!getStateMachineInstanceId().equals(eventTransitionPK.getStateMachineInstanceId())) return false;
            return getName().equals(eventTransitionPK.getName());

        }

        @Override
        public int hashCode() {
            int result = getStateMachineInstanceId().hashCode();
            result = 31 * result + getName().hashCode();
            result = 31 * result + getExecutionVersion().hashCode();
            return result;
        }
    }
}
