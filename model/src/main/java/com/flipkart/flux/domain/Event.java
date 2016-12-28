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
 * <code>Event</code> is the result of a {@link Task} execution.
 * It is to be posted back to the Flux execution engine once a worker has executed the task
 *
 * @author Yogesh
 * @author regunath.balasubramanian
 * @author shyam.akirala
 */

@Entity
@Table(name = "Events")
public class Event implements Serializable {

    /** Default serial version UID*/
    private static final long serialVersionUID = 1L;

    /** Auto generated Id*/
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The name for this Event*/
    private String name;

    /** The type of this Event*/
    private String type;

    /** Status for this Event*/
    @Enumerated(EnumType.STRING)
    private EventStatus status;

    /** Instance Id of state machine with which this event is associated */
    private Long stateMachineInstanceId;

    /** Data associated with this Event, stored as serialised json */
    private String eventData;

    /** The source who generated this Event */
    private String eventSource;

    /** Event creation time */
    private Timestamp createdAt;

    /** Time at which this event is last updated */
    @Column(updatable = false)
    private Timestamp updatedAt;

    /** Enum of Event statuses*/
    public enum EventStatus {
        pending,triggered;
    }

    /** Constructors */
    protected Event() {}
    public Event(String name, String type, EventStatus status, Long stateMachineInstanceId, String eventData, String eventSource) {
        this.name = name;
        this.type = type;
        this.status = status;
        this.stateMachineInstanceId = stateMachineInstanceId;
        this.eventData = eventData;
        this.eventSource = eventSource;
    }

    /** Accessor/Mutator methods*/
    public Long getId() {
        return id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public EventStatus getStatus() {
        return status;
    }
    public void setStatus(EventStatus status) {
        this.status = status;
    }
    public Long getStateMachineInstanceId() {
        return stateMachineInstanceId;
    }
    public void setStateMachineInstanceId(Long stateMachineInstanceId) {
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
    public String getType() {
        return type;
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

        Event event = (Event) o;

        if (eventData != null ? !eventData.equals(event.eventData) : event.eventData != null) return false;
        if (eventSource != null ? !eventSource.equals(event.eventSource) : event.eventSource != null) return false;
        if (name != null ? !name.equals(event.name) : event.name != null) return false;
        if (stateMachineInstanceId != null ? !stateMachineInstanceId.equals(event.stateMachineInstanceId) : event.stateMachineInstanceId != null)
            return false;
        if (status != event.status) return false;
        if (type != null ? !type.equals(event.type) : event.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (stateMachineInstanceId != null ? stateMachineInstanceId.hashCode() : 0);
        result = 31 * result + (eventData != null ? eventData.hashCode() : 0);
        result = 31 * result + (eventSource != null ? eventSource.hashCode() : 0);
        return result;
    }
}
