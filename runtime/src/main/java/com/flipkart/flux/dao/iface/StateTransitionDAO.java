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

import com.flipkart.flux.domain.StateTransition;
import com.flipkart.flux.domain.Status;
import com.flipkart.flux.shard.ShardId;

import java.sql.Timestamp;
import java.util.List;

/**
 * <code>StateTransitionDAO</code> interface provides methods to perform CR operations on {@link StateTransition}
 *
 * @author akif.khan
 */
public interface StateTransitionDAO {

    /**
     * Updates a stateTransition in db
     */
    void updateStateTransition(String stateMachineInstanceId, StateTransition stateTransition);

    /**
     * Updates status of a stateTransition
     */
    void updateStatus(String stateMachineInstanceId, Long stateId, Status status);

    /**
     * Updates rollback status of a stateTransition
     */
    public void updateRollbackStatus(String stateMachineInstanceId, Long stateId, Status rollbackStatus);

    /**
     * Increments the attempted no.of retries of a state by 1
     */
    void incrementRetryCount(String stateMachineInstanceId, Long stateId);

    /**
     * Retrieves a stateTransition by it's unique identifier
     */
    StateTransition findById(String stateMachineInstanceId, Long id);

    /**
     * Scatter gather query for slaves
     * Retrieves all errored stateTransitions for a particular state machine name and the state machine creation time in
     * the given range fromTime and toTime.
     */
    List findErroredStates(ShardId shardId, String stateMachineName, Timestamp fromTime, Timestamp toTime);

    /**
     * Scatter gather query for slave shards
     * Retrieves all stateTransition having one of the given statuses for a particular state machine name and the state machine creation time in
     * the given range fromTime and toTime with optional taskName parameter.
     * If status list is empty/null, returns all tasks.
     */
    List findStateTransitionByStatus(ShardId shardId, String stateMachineName, Timestamp fromTime, Timestamp toTime, String taskName, List<Status> statuses);

    /**
     * Retrieves all stateTransition for a particular state-machine-id and like input dependent-event-name.
     */
    List findStateTransitionByDependentEvent(String stateMachineId, String eventName);
}
