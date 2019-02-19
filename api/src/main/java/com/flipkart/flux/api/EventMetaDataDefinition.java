

package com.flipkart.flux.api;

import java.io.Serializable;
import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class EventMetaDataDefinition implements Serializable {


    private String name;
    private Long smId;
    private String type;
    private String dependentStates;
    private Timestamp createdAt;

    EventMetaDataDefinition() {
    }

    /** Constructor*/
    public EventMetaDataDefinition(String name, Long smId, String type, String dependentStates, Timestamp createdAt ) {
        super();
        this.name = name;
        this.smId=smId;
        this.type = type;
        this.dependentStates=dependentStates;
        this.createdAt=createdAt;
    }

    /** Accessors/Mutators for member variables*/
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Long getSmId() {
        return smId;
    }
    public void setSmId(String type) {
        this.smId = smId;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getDependentStates() {
        return dependentStates;
    }
    public  void setDependentStates(String dependentStates) {
        this.dependentStates = dependentStates;
    }
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventMetaDataDefinition)) return false;

        EventMetaDataDefinition that = (EventMetaDataDefinition) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (smId != null ? !smId.equals(that.smId) : that.smId != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (dependentStates != null ? !dependentStates.equals(that.dependentStates) : that.dependentStates != null) return false;
        if (createdAt != null ? !createdAt.equals(that.createdAt) : that.createdAt != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (smId != null ? smId.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (dependentStates != null ? dependentStates.hashCode() : 0);
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EventMetaDataDefinition{" +
                "name='" + name + '\'' +
                ", smId='" + smId + '\'' +
                ", type='" + type + '\'' +
                ", dependentStates='" + dependentStates + '\'' +
                ", createAt='" + createdAt + '\'' +
                '}';
    }

    /**
     * This event data object validates if it is carrying data for the given event definition
     * We should ideally change eventData objects to have eventDefinitions instead of redundant name & type.
     * When we do, only the impl of this method changes
     * @param eventMetaDataDefinition the event definition we want to check for
     * @return true if this data is corresponding to the given definition, false if not
     */


    @JsonIgnore
    public boolean isFor(EventMetaDataDefinition eventMetaDataDefinition) {
        return this.name.equals(eventMetaDataDefinition.getName()) && this.type.equals(eventMetaDataDefinition.getType());
    }
}


