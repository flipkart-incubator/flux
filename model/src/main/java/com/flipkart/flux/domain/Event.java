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
public class Event<T> implements Serializable {

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

    /** Data associated with this Event, must have public getters and setters and be serializable */
    @Type(type = "BlobType")
    private T eventData;

    /** The source who generated this Event */
    private String eventSource;

    /** Event creation time */
    private Timestamp createdAt;

    /** Time at which this event is last updated */
    private Timestamp updatedAt;

    /** Enum of Event statuses*/
    public enum EventStatus {
        pending,triggered;
    }

    /** Constructors */
    protected Event() {}
    public Event(String name, String type, EventStatus status, Long stateMachineInstanceId, T eventData, String eventSource) {
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
    public T getEventData() {
        return eventData;
    }
    public void setEventData(T eventData) {
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

        if (!eventSource.equals(event.eventSource)) return false;
        if (id != null ? !id.equals(event.id) : event.id != null) return false;
        if (!name.equals(event.name)) return false;
        if (!stateMachineInstanceId.equals(event.stateMachineInstanceId)) return false;
        if (!type.equals(event.type)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + name.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + stateMachineInstanceId.hashCode();
        result = 31 * result + eventSource.hashCode();
        return result;
    }
}
