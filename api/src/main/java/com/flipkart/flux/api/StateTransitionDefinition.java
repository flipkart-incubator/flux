package com.flipkart.flux.api;


import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

import com.flipkart.flux.domain.*;
import javax.persistence.*;
//import org.hibernate.annotations.Type;


public class StateTransitionDefinition {

    private String smId;
    private Long id;
    private Integer executionVersion;
    private List<EventTransitionDefinition> dependencies;

   //@Enumerated(EnumType.STRING)
    private Status status;

    private String validity;
    private Long attemptedRetries;
    private EventTransitionDefinition outputEvent;
    private Timestamp createdAt;
    private Timestamp updatedAt;


    StateTransitionDefinition(){
        super();
        this.dependencies = new LinkedList<>();
    }
    public StateTransitionDefinition(Integer executionVersion, List<EventTransitionDefinition> dependencies, String validity,
                                     Long attemptedRetries, EventTransitionDefinition outputEvent) {
        this();

        this.executionVersion=executionVersion;
        this.dependencies = dependencies;
        this.status =status;
        this.validity=validity;
        this.attemptedRetries=attemptedRetries;
        this.outputEvent = outputEvent;

    }


    public String getSmId() {
        return smId;
    }
    public void setSmId(String smId) {
        this.smId = smId;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) { this.id = id; }

    public Integer getExecutionVersion(){return executionVersion;}
    public void setExecutionVersion(Integer executionVersion){this.executionVersion=executionVersion;}

    public List<EventTransitionDefinition> getDependencies(){return dependencies;}
    public void setDependencies(List<EventTransitionDefinition> dependencies){this.dependencies=dependencies;}

    public Status getStatus(){return status;}
    public void setStatus(Status status){this.status=status;}

    public String getValidity(){return validity;}
    public void setValidity(String validity){this.validity=validity;}

    public Long getAttemptedRetries(){return attemptedRetries;}
    public void setAttemptedRetries(Long attemptedRetries){this.attemptedRetries=attemptedRetries;}


    public EventTransitionDefinition getOutputEvent() {
        return outputEvent;
    }
    public void setOutputEvent(EventTransitionDefinition outputEvent) {
        this.outputEvent = outputEvent;
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



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StateTransitionDefinition that = (StateTransitionDefinition) o;

        if (smId != null ? !smId.equals(that.smId) : that.smId != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (executionVersion != null ? !executionVersion.equals(that.executionVersion) : that.executionVersion != null) return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        if (validity != null ? !validity.equals(that.validity) : that.validity != null) return false;
        if (attemptedRetries != null ? !attemptedRetries.equals(that.attemptedRetries) : that.attemptedRetries != null) return false;
        if (outputEvent != null ? !outputEvent.equals(that.outputEvent) : that.outputEvent != null) return false;
        if (createdAt!= null ? !createdAt.equals(that.createdAt) : that.createdAt != null) return false;
        if (updatedAt!= null ? !updatedAt.equals(that.updatedAt) : that.updatedAt != null) return false;

        return !(dependencies != null ? !dependencies.equals(that.dependencies) : that.dependencies != null);

    }


    @Override
    public int hashCode() {
        int result = smId != null ? smId.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (executionVersion != null ? executionVersion.hashCode() : 0);
        result = 31 * result + (dependencies != null ? dependencies.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (validity != null ? validity.hashCode() : 0);
        result = 31 * result + (attemptedRetries != null ? attemptedRetries.hashCode() : 0);
        result = 31 * result + (outputEvent != null ? outputEvent.hashCode() : 0);
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        result = 31 * result + (updatedAt != null ? updatedAt.hashCode() : 0);


        return result;
    }

    @Override
    public String toString() {
        return "StateTransitionDefinition{" +
                "smId=" + smId +
                ", id=" + id +
                ", executionVersion=" + executionVersion +
                ", dependencies='" + dependencies + '\'' +
                ", status='" + status + '\'' +
                ", validity='" + validity + '\'' +
                ", attemptedRetries='" + attemptedRetries + '\'' +
                ", outputEvent='" + outputEvent + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +

                '}';
    }
}

