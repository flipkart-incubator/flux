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
 * @author shyam.akirala
 */

@Entity
@Table(name="AUDIT")
public class AuditRecord {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="state_machine_name")
    private String stateMachineName;

    @Column(name="state_machine_instance_id")
    private String stateMachineInstanceId;

    @Column(name="state_id")
    private Long stateId;

    @Column(name="retry_attempt")
    private int retryAttempt;

    @Column(name="state_status")
    private String stateStatus;

    @Column(name="state_start_time")
    private Date stateStartTime;

    @Column(name="state_end_time")
    private Date stateEndTime;

    public AuditRecord(){}

    public AuditRecord(String stateMachineName, String stateMachineInstanceId, Long stateId, int retryAttempt, String stateStatus, Date stateStartTime, Date stateEndTime) {
        this.stateMachineName = stateMachineName;
        this.stateMachineInstanceId = stateMachineInstanceId;
        this.stateId = stateId;
        this.retryAttempt = retryAttempt;
        this.stateStatus = stateStatus;
        this.stateStartTime = stateStartTime;
        this.stateEndTime = stateEndTime;
    }

    public Long getId() {
        return id;
    }
    public String getStateMachineName() {
        return stateMachineName;
    }
    public void setStateMachineName(String stateMachineName) {
        this.stateMachineName = stateMachineName;
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
    public String getStateStatus() {
        return stateStatus;
    }
    public void setStateStatus(String stateStatus) {
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
}
