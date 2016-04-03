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
    private String stateId;

    /** The State execution retry count */
    private int retryAttempt;

    /** The State execution status */
    @Enumerated(EnumType.STRING)
    private Status stateStatus;

    /** The State rollback status */
    @Enumerated(EnumType.STRING)
    private Status stateRollbackStatus;

    /** Time when the State has started */
    private Timestamp stateStartTime;

    /** Time when the State has ended */
    private Timestamp stateEndTime;

    /** Time when the State rollback has started */
    private Timestamp rollbackStartTime;

    /** Audit log creation time */
    private Timestamp createdAt;

    /** Constructors */
    protected AuditRecord(){}
    public AuditRecord(String stateMachineInstanceId, String stateId, int retryAttempt, Status stateStatus, Status stateRollbackStatus,
                       Timestamp stateStartTime, Timestamp stateEndTime, Timestamp rollbackStartTime) {
        this.stateMachineInstanceId = stateMachineInstanceId;
        this.stateId = stateId;
        this.retryAttempt = retryAttempt;
        this.stateStatus = stateStatus;
        this.stateRollbackStatus = stateRollbackStatus;
        this.stateStartTime = stateStartTime;
        this.stateEndTime = stateEndTime;
        this.rollbackStartTime = rollbackStartTime;
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
    public String getStateId() {
        return stateId;
    }
    public void setStateId(String stateId) {
        this.stateId = stateId;
    }
    public int getRetryAttempt() {
        return retryAttempt;
    }
    public void setRetryAttempt(int retryAttempt) {
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
    public Timestamp getStateStartTime() {
        return stateStartTime;
    }
    public void setStateStartTime(Timestamp stateStartTime) {
        this.stateStartTime = stateStartTime;
    }
    public Timestamp getStateEndTime() {
        return stateEndTime;
    }
    public void setStateEndTime(Timestamp stateEndTime) {
        this.stateEndTime = stateEndTime;
    }
    public Timestamp getRollbackStartTime() {
        return rollbackStartTime;
    }
    public void setRollbackStartTime(Timestamp rollbackStartTime) {
        this.rollbackStartTime = rollbackStartTime;
    }
    public Timestamp getCreatedAt() {
        return createdAt;
    }
}