package com.flipkart.flux.api;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;

@JsonIgnoreProperties(ignoreUnknown = true)

public class EventTransitionDefinition implements Serializable {

    private String name;
    private String correlationId;
    private Integer executionVersion;
    private String eventData;
    private String eventSource;
    private Boolean validity;
    private String status;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Boolean isCancelled;


    EventTransitionDefinition(){

    }
    public EventTransitionDefinition(String name, String correlationId, Integer executionVersion, String eventData, String eventSource, Boolean validity, String status) {
        this();
        this.name=name;
        this.correlationId=correlationId;
        this.executionVersion=executionVersion;
        this.eventData=eventData;
        this.eventSource=eventSource;
        this.validity=validity;
        this.status=status;

        this.isCancelled=false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getcorrelationId() {
        return correlationId;
    }

    public void setcorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Integer getExecutionVersion() {
        return executionVersion;
    }

    public void setExecutionVersion(Integer executionVersion) {
        this.executionVersion = executionVersion;
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

    public Boolean getValidity() {
        return validity;
    }

    public void setValidity(Boolean validity) {
        this.validity = validity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getCancelled() {
        return isCancelled;
    }

    public void setCancelled(Boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventTransitionDefinition)) return false;

        EventTransitionDefinition that = (EventTransitionDefinition) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (correlationId != null ? !correlationId.equals(that.correlationId) : that.correlationId != null) return false;
        if (executionVersion != null ? !executionVersion.equals(that.executionVersion) : that.executionVersion != null) return false;
        if (eventData != null ? !eventData.equals(that.eventData) : that.eventData != null) return false;
        if (eventSource != null ? !eventSource.equals(that.eventSource) : that.eventSource != null) return false;
        if (validity != null ? !validity .equals(that.validity ) : that.validity  != null) return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        if (createdAt!= null ? !createdAt.equals(that.createdAt) : that.createdAt != null) return false;
        if (updatedAt!= null ? !updatedAt.equals(that.updatedAt) : that.updatedAt != null) return false;

        return true;

    }


    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (correlationId != null ? correlationId.hashCode() : 0);
        result = 31 * result + (executionVersion != null ? executionVersion.hashCode() : 0);
        result = 31 * result + (eventData != null ? eventData.hashCode() : 0);
        result = 31 * result + (eventSource != null ? eventSource.hashCode() : 0);
        result = 31 * result + (validity != null ? validity.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        result = 31 * result + (updatedAt!= null ? updatedAt.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EventTransitionDefinition{" +
                "name=" + name +
                ", correlationId=" + correlationId +
                ", executionVersion=" + executionVersion +
                ", eventData='" + eventData + '\'' +
                ", eventSource='" + eventSource + '\'' +
                ", validity=" + validity +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +


                '}';
    }

    /**
     * This event data object validates if it is carrying data for the given event definition
     * We should ideally change eventData objects to have eventDefinitions instead of redundant name & type.
     * When we do, only the impl of this method changes
     * @param eventDefinition the event definition we want to check for
     * @return true if this data is corresponding to the given definition, false if not
     */


    /*@JsonIgnore
    public boolean isFor(EventDefinition eventDefinition) {
        return this.name.equals(eventDefinition.getName()) && this.type.equals(eventDefinition.getType());
    }*/

}

