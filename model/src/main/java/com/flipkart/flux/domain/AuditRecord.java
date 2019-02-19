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
import java.sql.Timestamp;

/**
 * <code>AuditRecord</code> represents a audit log of state machine execution.
 * @author shyam.akirala
 */

@Entity
@Table(name = "AuditRecords")
public class AuditRecord {

    /** Auto generated id */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Instance id of the state machine to which this audit belongs */
    private String stateMachineInstanceId;

    /** The State identifier to which this audit belongs */
    private Long stateId;

    /** Version to which current state execution belongs */
    private Long executionVersion;

    /** The State execution retry count */
    private Long retryAttempt;

    /** The State execution status */
    @Enumerated(EnumType.STRING)
    private Status stateStatus;

    /** The State rollback status */
    @Enumerated(EnumType.STRING)
    private Status stateRollbackStatus;

    /** Any errors occurred in the state execution*/
    private String errors;

    /** Audit log creation time */
    private Timestamp createdAt;

    /** Constructors */
    protected AuditRecord(){}
    public AuditRecord(String stateMachineInstanceId, Long stateId, Long retryAttempt, Status stateStatus, Status stateRollbackStatus,
                       String errors, Long executionVersion) {
        this.stateMachineInstanceId = stateMachineInstanceId;
        this.stateId = stateId;
        this.retryAttempt = retryAttempt;
        this.stateStatus = stateStatus;
        this.stateRollbackStatus = stateRollbackStatus;
        this.errors = errors;
        this.executionVersion = executionVersion;
    }

    /** Accessor/Mutator methods*/
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
    public void setExecutionVersion(Long executionVersion) {
        this.executionVersion = executionVersion;
    }
    public Long getExecutionVersion() {
        return executionVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuditRecord)) return false;

        AuditRecord that = (AuditRecord) o;

        if (retryAttempt != null ? !retryAttempt.equals(that.retryAttempt) : that.retryAttempt != null) return false;
        if (stateId != null ? !stateId.equals(that.stateId) : that.stateId != null) return false;
        if (stateMachineInstanceId != null ? !stateMachineInstanceId.equals(that.stateMachineInstanceId) : that.stateMachineInstanceId != null)
            return false;
        if (stateRollbackStatus != that.stateRollbackStatus) return false;
        if (stateStatus != that.stateStatus) return false;
        if (executionVersion != that.executionVersion) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = stateMachineInstanceId != null ? stateMachineInstanceId.hashCode() : 0;
        result = 31 * result + (stateId != null ? stateId.hashCode() : 0);
        result = 31 * result + (retryAttempt != null ? retryAttempt.hashCode() : 0);
        result = 31 * result + (stateStatus != null ? stateStatus.hashCode() : 0);
        result = 31 * result + (stateRollbackStatus != null ? stateRollbackStatus.hashCode() : 0);
        result = 31 * result + (executionVersion != null ? executionVersion.hashCode() : 0);
        return result;
    }

}