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

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.Map;

/**
 * <code>Checkpoint</code> represents saved state of a State Machines execution.
 * Used when the user wants to replay the execution of a state machine from a particular state.
 * @author shyam.akirala
 */

@Entity
public class Checkpoint {

    /** Auto generated id */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Name of the state machine to which this checkpoint belongs */
    private String stateMachineName;

    /** Instance id of the state machine to which this checkpoint belongs */
    private String stateMachineInstanceId;

    /** The State identifier to which this checkpoint belongs */
    private Long stateId;

    /** Date associated with the state machines execution*/
    @Type(type = "BlobType")
    private Map<String, Object> data;

    /** Checkpoint creation time */
    @CreationTimestamp
    private Date createdAt;

    /** Time at which this checkpoint is last updated */
    @UpdateTimestamp
    private Date updatedAt;

    /** Constructors */
    public Checkpoint() {}
    public Checkpoint(String stateMachineName, String stateMachineInstanceId, Long stateId, Map<String, Object> data) {
        this.stateMachineName = stateMachineName;
        this.stateMachineInstanceId = stateMachineInstanceId;
        this.stateId = stateId;
        this.data = data;
    }

    /** Accessor/Mutator methods*/
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
    public Map<String, Object> getData() {
        return data;
    }
    public void setData(Map<String, Object> data) {
        this.data = data;
    }
    public Date getCreatedAt() {
        return createdAt;
    }
    public Date getUpdatedAt() {
        return updatedAt;
    }
}
