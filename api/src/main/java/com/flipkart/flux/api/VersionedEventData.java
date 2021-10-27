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

package com.flipkart.flux.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * <code>VersionedEventData</code> represents the event which would be submitted to Flux execution from Flux runtime.
 * This is useful for data transfer purpose only.
 * @author akif.khan
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VersionedEventData implements Serializable {

    /** Name of the event */
    private String name;

    /** Type of the event */
    private String type;

    /** ExecutionVersion of the event */
    private Long executionVersion;

    /** Serialised Data for the event */
    private String data;

    /** Source who generated this event, might be state name or external */
    private String eventSource;

    /** Indicates whether this event is cancelled, based on this value runtime decides to cancel the entire path in DAG */
    private Boolean isCancelled;

    /** Used by jackson */
    VersionedEventData() {}

    /** constructor */
    public VersionedEventData(String name, String type, String data, String eventSource, Long executionVersion) {
        this.name = name;
        this.type = type;
        this.data = data;
        this.eventSource = eventSource;
        this.executionVersion = executionVersion;
    }

    public VersionedEventData(String name, String type, String data, String eventSource, Long executionVersion,
                              Boolean isCancelled) {
        this(name, type, data, eventSource, executionVersion);
        this.isCancelled = isCancelled;
    }

    /** Accessor/Mutator methods*/
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getData() {
        return data;
    }
    public void setData(String data) {
        this.data = data;
    }
    public String getEventSource() {
        return eventSource;
    }
    public void setEventSource(String eventSource) {
        this.eventSource = eventSource;
    }
    public Boolean getCancelled() {
        return isCancelled;
    }
    public void setCancelled(Boolean cancelled) {
        isCancelled = cancelled;
    }

    public Long getExecutionVersion() {
        return executionVersion;
    }

    public void setExecutionVersion(Long executionVersion) {
        this.executionVersion = executionVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VersionedEventData versionedEventData = (VersionedEventData) o;

        if (!name.equals(versionedEventData.name)) return false;
        if (type != null ? !type.equals(versionedEventData.type) : versionedEventData.type != null) return false;
        if (data != null ? !data.equals(versionedEventData.data) : versionedEventData.data != null) return false;
        if (eventSource != null ? !eventSource.equals(versionedEventData.eventSource) :
                versionedEventData.eventSource != null) return false;
        if (!executionVersion.equals(versionedEventData.executionVersion)) return false;
        return isCancelled != null ? isCancelled.equals(versionedEventData.isCancelled) :
                versionedEventData.isCancelled == null;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (data != null ? data.hashCode() : 0);
        result = 31 * result + (eventSource != null ? eventSource.hashCode() : 0);
        result = 31 * result + (executionVersion != null ? executionVersion.hashCode() : 0);
        result = 31 * result + (isCancelled != null ? isCancelled.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "VersionedEventData{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", data='" + data + '\'' +
                ", eventSource='" + eventSource + '\'' +
                ", executionVersion=" + executionVersion +
                ", isCancelled=" + isCancelled +
                '}';
    }

    /**
     * This event data object validates if it is carrying data for the given event definition
     * We should ideally change eventVersionedData objects to have eventDefinitions instead of redundant name & type.
     * When we do, only the impl of this method changes
     * @param eventDefinition the event definition we want to check for
     * @return true if this data is corresponding to the given definition, false if not
     */
    @JsonIgnore
    public boolean isFor(EventDefinition eventDefinition) {
        return this.name.equals(eventDefinition.getName()) && this.type.equals(eventDefinition.getType());
    }
}