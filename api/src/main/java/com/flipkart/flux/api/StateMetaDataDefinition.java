package com.flipkart.flux.api;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

public class StateMetaDataDefinition {

    private String smId;
    private Long id;
    private Long version;
    private String description;
    private String task;
    private List<EventMetaDataDefinition> dependencies;
    private String onEntryHook;
    private String onExitHook;
    private Long retryCount;
    private Long timeout;
    private EventMetaDataDefinition outputEvent;
    private Timestamp createdAt;


    StateMetaDataDefinition(){
        super();
        this.dependencies = new LinkedList<>();
    }
    public StateMetaDataDefinition(String smId, Long id, Long version, String description, String task, List<EventMetaDataDefinition> dependencies, String onEntryHook, String onExitHook,
                                   Long retryCount, Long timeout, EventMetaDataDefinition outputEvent, Timestamp createdAt) {
        this();
        this.smId=smId;
        this.id=id;
        this.version = version;
        this.description = description;
        this.task=task;
        this.dependencies = dependencies;
        this.onEntryHook = onEntryHook;
        this.onExitHook = onExitHook;
        this.retryCount = retryCount;
        this.timeout = timeout;
        this.outputEvent = outputEvent;
        this.createdAt = createdAt;
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
    public Long getVersion() {
        return version;
    }
    public void setVersion(Long version) {
        this.version = version;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getTask() {
        return task;
    }
    public void setTask(String task) {
        this.task = task;
    }
    public List<EventMetaDataDefinition> getDependencies() {
        return dependencies;
    }
    public void setDependencies(List<EventMetaDataDefinition> dependencies) {
        this.dependencies = dependencies;
    }
    public String getOnEntryHook() {
        return onEntryHook;
    }
    public void setOnEntryHook(String onEntryHook) {
        this.onEntryHook = onEntryHook;
    }
    public String getOnExitHook() {
        return onExitHook;
    }
    public void setOnExitHook(String onExitHook) {
        this.onExitHook = onExitHook;
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
    public EventMetaDataDefinition getOutputEvent() {
        return outputEvent;
    }
    public void setOutputEvent(EventMetaDataDefinition outputEvent) {
        this.outputEvent = outputEvent;
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
        if (o == null || getClass() != o.getClass()) return false;

        StateMetaDataDefinition that = (StateMetaDataDefinition) o;

        if (smId != null ? !smId.equals(that.smId) : that.smId != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (version != null ? !version.equals(that.version) : that.version != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (task != null ? !task.equals(that.task) : that.task != null) return false;
        if (onEntryHook != null ? !onEntryHook.equals(that.onEntryHook) : that.onEntryHook != null) return false;
        if (onExitHook != null ? !onExitHook.equals(that.onExitHook) : that.onExitHook != null) return false;
        if (retryCount != null ? !retryCount.equals(that.retryCount) : that.retryCount != null) return false;
        if (timeout != null ? !timeout.equals(that.timeout) : that.timeout != null) return false;
        if (outputEvent != null ? !outputEvent.equals(that.outputEvent) : that.outputEvent != null) return false;
        if (createdAt!= null ? !createdAt.equals(that.createdAt) : that.createdAt != null) return false;
        return !(dependencies != null ? !dependencies.equals(that.dependencies) : that.dependencies != null);

    }


    @Override
    public int hashCode() {
        int result = smId != null ? smId.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (task != null ? task.hashCode() : 0);
        result = 31 * result + (dependencies != null ? dependencies.hashCode() : 0);
        result = 31 * result + (onEntryHook != null ? onEntryHook.hashCode() : 0);
        result = 31 * result + (onExitHook != null ? onExitHook.hashCode() : 0);
        result = 31 * result + (retryCount != null ? retryCount.hashCode() : 0);
        result = 31 * result + (timeout != null ? timeout.hashCode() : 0);
        result = 31 * result + (outputEvent != null ? outputEvent.hashCode() : 0);
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);


        return result;
    }

    @Override
    public String toString() {
        return "StateMetaDataDefinition{" +
                "smId=" + smId +
                ", id=" + id +
                ", version=" + version +
                ", description='" + description + '\'' +
                ", task='" + task + '\'' +
                ", dependencies=" + dependencies +
                ", onEntryHook='" + onEntryHook + '\'' +
                ", onExitHook='" + onExitHook + '\'' +
                ", retryCount=" + retryCount +
                ", timeout=" + timeout +
                ", outputEvent='" + outputEvent + '\'' +
                ", createdAt=" + createdAt +

                '}';
    }
}

