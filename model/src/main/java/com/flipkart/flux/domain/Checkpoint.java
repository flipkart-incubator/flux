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

import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Map;

/**
 * @author shyam.akirala
 */

@Entity
@Table(name="checkpoints")
public class Checkpoint {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="state_machine_name")
    private String stateMachineName;

    @Column(name="state_machine_instance_id")
    private String stateMachineInstanceId;

    @Column(name="state_id")
    private Long stateId;

    private Map<String, Object> data;

    public Checkpoint() {}

    public Checkpoint(String stateMachineName, String stateMachineInstanceId, Long stateId, Map<String, Object> data) {
        this.stateMachineName = stateMachineName;
        this.stateMachineInstanceId = stateMachineInstanceId;
        this.stateId = stateId;
        this.data = data;
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
    public Map<String, Object> getData() {
        return data;
    }
    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
