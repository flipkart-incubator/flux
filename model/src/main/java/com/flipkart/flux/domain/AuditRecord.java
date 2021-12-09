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

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * <code>AuditRecord</code> represents a audit log of state machine execution.
 *
 * @author shyam.akirala
 */

@Entity
@Table(name = "AuditRecords")
public class AuditRecord {

    /**
     * Auto generated id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Instance id of the state machine to which this audit belongs
     */
    private String stateMachineInstanceId;

    /**
     * The State identifier to which this audit belongs
     */
    private Long stateId;

    /**
     * The State execution retry count
     */
    private Long retryAttempt;

    /**
     * The State execution status
     */
    @Enumerated(EnumType.STRING)
    private Status stateStatus;

    /**
     * The State rollback status
     */
    @Enumerated(EnumType.STRING)
    private Status stateRollbackStatus;

    /**
     * Any errors occurred in the state execution
     */
    private String errors;

    private Long taskExecutionVersion;

    private String eventDependencies;

    /**
     * Audit log creation time
     */
    private Timestamp createdAt;

    /**
     * Constructors
     */
    protected AuditRecord() {
    }

    public AuditRecord(String stateMachineInstanceId, Long stateId, Long retryAttempt, Status stateStatus,
                       Status stateRollbackStatus, String errors) {
        this(stateMachineInstanceId, stateId, retryAttempt, stateStatus, stateRollbackStatus, errors,
                0L, "");
    }

    public AuditRecord(String stateMachineInstanceId, Long stateId, Long retryAttempt, Status stateStatus,
                       Status stateRollbackStatus, String errors, Long taskExecutionVersion, String eventDependencies) {
        this.stateMachineInstanceId = stateMachineInstanceId;
        this.stateId = stateId;
        this.retryAttempt = retryAttempt;
        this.stateStatus = stateStatus;
        this.stateRollbackStatus = stateRollbackStatus;
        this.errors = errors;
        this.taskExecutionVersion = taskExecutionVersion;
        this.eventDependencies = eventDependencies;
    }

    /**
     * Accessor/Mutator methods
     */
    public Long getId() {
        return id;
    }

    public String getStateMachineInstanceId() {
        return stateMachineInstanceId;
    }

    public void setStateMachineInstanceId(String stateMachineInstanceId) {
        this.stateMachineInstanceId = stateMachineInstanceId;
    }

    public Long getStateId() {
        return stateId;
    }

    public void setStateId(Long stateId) {
        this.stateId = stateId;
    }

    public Long getRetryAttempt() {
        return retryAttempt;
    }

    public void setRetryAttempt(Long retryAttempt) {
        this.retryAttempt = retryAttempt;
    }

    public Status getStateStatus() {
        return stateStatus;
    }

    public void setStateStatus(Status stateStatus) {
        this.stateStatus = stateStatus;
    }

    public Status getStateRollbackStatus() {
        return stateRollbackStatus;
    }

    public void setStateRollbackStatus(Status stateRollbackStatus) {
        this.stateRollbackStatus = stateRollbackStatus;
    }

    public String getErrors() {
        return errors;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Long getTaskExecutionVersion() {
        return taskExecutionVersion;
    }

    public void setTaskExecutionVersion(Long taskExecutionVersion) {
        this.taskExecutionVersion = taskExecutionVersion;
    }

    public String getEventDependencies() {
        return eventDependencies;
    }

    public void setEventDependencies(String eventDependencies) {
        this.eventDependencies = eventDependencies;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuditRecord)) return false;

        AuditRecord that = (AuditRecord) o;

        if (retryAttempt != null ? !retryAttempt.equals(that.retryAttempt) : that.retryAttempt != null) return false;
        if (stateId != null ? !stateId.equals(that.stateId) : that.stateId != null) return false;
        if (stateMachineInstanceId != null ? !stateMachineInstanceId.equals(
                that.stateMachineInstanceId) : that.stateMachineInstanceId != null)
            return false;
        if (stateRollbackStatus != that.stateRollbackStatus) return false;
        if (stateStatus != that.stateStatus) return false;
        if (taskExecutionVersion != that.taskExecutionVersion) return false;
        if (eventDependencies != that.eventDependencies) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = stateMachineInstanceId != null ? stateMachineInstanceId.hashCode() : 0;
        result = 31 * result + (stateId != null ? stateId.hashCode() : 0);
        result = 31 * result + (retryAttempt != null ? retryAttempt.hashCode() : 0);
        result = 31 * result + (stateStatus != null ? stateStatus.hashCode() : 0);
        result = 31 * result + (stateRollbackStatus != null ? stateRollbackStatus.hashCode() : 0);
        result = 31 * result + (taskExecutionVersion != null ? taskExecutionVersion.hashCode() : 0);
        result = 31 * result + (eventDependencies != null ? eventDependencies.hashCode() : 0);
        return result;
    }

}