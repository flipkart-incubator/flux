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
import com.flipkart.flux.domain.StateMachineStatus;
import com.flipkart.flux.shard.ShardId;
import org.hibernate.Session;

import java.util.Set;

/**
 * <code>StateMachinesDAO</code> interface provides methods to perform CR operations on {@link StateMachine}
 *
 * @author shyam.akirala
 */
public interface StateMachinesDAO {

    /**
     * Creates state machine and returns saved object
     */
    StateMachine create(String StateMachineInstanceId, StateMachine stateMachine);

    /**
     * Retrieves state machine by it's unique identifier
     */
    StateMachine findById(String stateMachineId);

    /**
     * Retrieves set of state machines by State machine's Name
     */
    Set<StateMachine> findByName(ShardId shardId, String stateMachineName);

    /**
     * Retrieves set of state machines by Name and version
     */
    Set<StateMachine> findByNameAndVersion(ShardId shardId, String stateMachineName, Long Version);

    /**
     * Updates status of a state machine
     */
    void updateStatus(String stateMachineId, StateMachineStatus status);

    /**
     * findById from StateMachine with "FOR UPDATE" flag to avoid dirty/stale reads by multiple transactions
     * on same StateMachine instance's executionVersion. "FOR UPDATE" will make any other transaction trying to access
     * same State machine instance's executionVersion wait until current transaction commit/roll-back
     */
    Long findExecutionVersionBySMIdForUpdate_NonTransactional(String stateMachineInstanceId, Session session);

    /**
     * Updates value of executionVersion of a stateMachine, this will be picked up as an executionVersion for
     * post replay event.
     */
    void updateExecutionVersion_NonTransactional(String stateMachineInstanceId, Long smExecutionVersion, Session session);
}