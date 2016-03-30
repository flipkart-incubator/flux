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
import java.util.Date;

/**
 * <code>AuditRecord</code> represents a audit log of state machine execution.
 * @author shyam.akirala
 */

@Entity
public class AuditRecord {

    /** Auto generated id */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Instance id of the state machine to which this audit belongs */
    private String stateMachineInstanceId;

    /** The State identifier to which this audit belongs */
    private Long stateId;

    /** The State execution retry count */
    private int retryAttempt;

    /** The State execution status */
    @Enumerated(EnumType.STRING)
    private Status stateStatus;

    /** Time when the State has started */
    private Date stateStartTime;

    /** Time when the State has ended */
    private Date stateEndTime;

    /** Audit log creation time */
    private Date createdAt;

    /** Constructors */
    protected AuditRecord(){}
    public AuditRecord(String stateMachineInstanceId, Long stateId, int retryAttempt, Status stateStatus, Date stateStartTime, Date stateEndTime) {
        this.stateMachineInstanceId = stateMachineInstanceId;
        this.stateId = stateId;
        this.retryAttempt = retryAttempt;
        this.stateStatus = stateStatus;
        this.stateStartTime = stateStartTime;
        this.stateEndTime = stateEndTime;
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
    public Date getStateStartTime() {
        return stateStartTime;
    }
    public void setStateStartTime(Date stateStartTime) {
        this.stateStartTime = stateStartTime;
    }
    public Date getStateEndTime() {
        return stateEndTime;
    }
    public void setStateEndTime(Date stateEndTime) {
        this.stateEndTime = stateEndTime;
    }
    public Date getCreatedAt() {
        return createdAt;
    }
}