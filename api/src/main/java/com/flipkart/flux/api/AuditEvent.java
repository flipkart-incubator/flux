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
 * <code>AuditEvent</code> represents the event execution version to be used to propogate <eventName, executionVersion>
 *  between Execution Runtime <-> Flux Runtime for audit purposes
 * This is useful for data transfer purpose only.
 *
 * @author akif.khan
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditEvent implements Serializable {

    /**
     * Name of the event
     */
    private String name;

    /**
     * Indicates execution version for this event
     */
    private Long executionVersion;

    /**
     * Used by jackson
     */
    AuditEvent() {
    }

    /**
     * constructor
     */
    public AuditEvent(String name, Long executionVersion) {
        this.name = name;
        this.executionVersion = executionVersion;
    }

    /**
     * Accessor/Mutator methods
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

        AuditEvent auditEvent = (AuditEvent) o;

        if (!name.equals(auditEvent.name)) return false;
        if (executionVersion != null ? !executionVersion.equals(
                auditEvent.executionVersion) : auditEvent.executionVersion != null)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (executionVersion != null ? executionVersion.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        // Using ':' instead of comma so this can be parsed as Json object
        return "{" +
                "\"" + name + '\"' +
                ": " + executionVersion +
                '}';
    }
}
