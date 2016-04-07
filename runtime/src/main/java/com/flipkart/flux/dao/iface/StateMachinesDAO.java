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

package com.flipkart.flux.dao.iface;

import com.flipkart.flux.domain.StateMachine;

import java.util.List;

/**
 * Provides methods to perform CR operations on {@link com.flipkart.flux.domain.StateMachine}
 * @author shyam.akirala
 */
public interface StateMachinesDAO {

    /** Creates state machine and returns saved object*/
    StateMachine create(StateMachine stateMachine);

    /** Retrieves state machine by it's unique identifier*/
    StateMachine findById(String id);

    /** Retrieves list of state machines by State machine's Name*/
    List<StateMachine> findByName(String stateMachineName);

    /** Retrieves list of state machines by Name and version*/
    List<StateMachine> findByNameAndVersion(String stateMachineName, Long Version);

}
