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

package com.flipkart.flux.persistence;

import java.sql.Timestamp;

import javax.inject.Inject;
import javax.transaction.Transactional;

import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.Status;
import com.flipkart.flux.persistence.criteria.DependentEventCriteria;
import com.flipkart.flux.persistence.criteria.FSMStatusCriteria;
import com.flipkart.flux.persistence.dao.iface.StatesDAOV1;
import com.flipkart.flux.persistence.dto.StateUpdate;
import com.flipkart.flux.persistence.key.EntityId;
import com.flipkart.flux.persistence.key.FSMId;
import com.flipkart.flux.persistence.key.FSMIdEntityId;
import com.flipkart.flux.shard.ShardId;

/**
 * An @EntityManager sub-type for managing persistence of @State instances 
 * @author regu.b
 *
 */
public class StateEntityManager extends EntityManager<State> {
	
	/** Constructor */
	@Inject
	public StateEntityManager(StatesDAOV1 statesDAO) {		
		super(statesDAO);
	}
	
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)	
	public void updateStatus(FSMId fsmId, Long[] stateIds, Status status) {
		((StatesDAOV1)getDAO()).updateData(StateUpdate.StateUpdateField.status, new StateUpdate.StatusUpdate(fsmId, stateIds, status));
	}
	
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
	public void updateRollbackStatus(FSMId fsmId, Long stateId, Status rollbackStatus) {
    	((StatesDAOV1)getDAO()).updateData(StateUpdate.StateUpdateField.rollbackStatus, 
				new StateUpdate.RollbackStatusUpdate(new FSMIdEntityId(fsmId, new EntityId(stateId)), 
						rollbackStatus));
	}
	
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
	public void incrementRetryCount(FSMId fsmId, Long stateId) {
    	((StatesDAOV1)getDAO()).updateData(StateUpdate.StateUpdateField.attemptedNoOfRetries,new StateUpdate.NoOfRetriesIncrement(
				new FSMIdEntityId(fsmId, new EntityId(stateId))));
	}
	
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
	public void updateExecutionVersion(FSMId fsmId, Long stateId, Long executionVersion) {
    	((StatesDAOV1)getDAO()).updateData(StateUpdate.StateUpdateField.executionVersion, new StateUpdate.ExecutionVersionUpdate(
				new FSMIdEntityId(fsmId, new EntityId(stateId)), executionVersion));
	}
	
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
	public void updateReplayableRetries(FSMId fsmId, Long stateId, Short replayableRetries) {		
    	((StatesDAOV1)getDAO()).updateData(StateUpdate.StateUpdateField.attemptedNumOfReplayableRetries, new StateUpdate.ReplayableRetriesUpdate(
				new FSMIdEntityId(fsmId, new EntityId(stateId)), replayableRetries));
	}
        
	public State[] findErroredStates(ShardId shardId, String stateMachineName, Timestamp fromTime, Timestamp toTime) {
		return this.findByFSMStatusCriteria(new FSMStatusCriteria(shardId, stateMachineName, fromTime, toTime, new Status[] {Status.errored}));
	}
	
	public State[] findStatesByStatus(ShardId shardId, String stateMachineName, Timestamp fromTime, Timestamp toTime, String stateName, Status[] statuses) {
		return this.findByFSMStatusCriteria(new FSMStatusCriteria(shardId, stateMachineName, fromTime, toTime, stateName, statuses));
	}
    
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_ONLY, storage = Storage.SHARDED)
    public State[] findByFSMStatusCriteria(FSMStatusCriteria fsmStatusCriteria) { // has to be a public method for AOP interception to work
    	return ((StatesDAOV1)getDAO()).findEntities(fsmStatusCriteria);
    }
	
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_ONLY, storage = Storage.SHARDED)
	public State[] findStatesByDependentEvent(FSMId fsmId, String eventName) {
		return ((StatesDAOV1)getDAO()).findEntities(new DependentEventCriteria(fsmId, eventName));
	}
		
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
	public short getReplayableRetriesForUpdate(FSMIdEntityId fsmIfdEntityId) {
		return ((StatesDAOV1)getDAO()).getReplayableRetriesForUpdate(fsmIfdEntityId);
	}
			
}
