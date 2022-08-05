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

import javax.inject.Inject;
import javax.transaction.Transactional;

import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.domain.StateMachineStatus;
import com.flipkart.flux.persistence.dao.iface.StateMachinesDAOV1;
import com.flipkart.flux.persistence.dto.StateMachineUpdate;
import com.flipkart.flux.persistence.dto.StateMachineUpdate.StateMachineUpdateField;
import com.flipkart.flux.persistence.key.FSMId;

/**
 * An @EntityManager sub-type for managing persistence of @StateMachine instances 
 * @author regu.b
 *
 */
public class StateMachineEntityManager extends EntityManager<StateMachine> {

	/** Constructor */
	@Inject
	public StateMachineEntityManager(StateMachinesDAOV1 stateMachinesDAOV1) {
		super(stateMachinesDAOV1);
	}
	
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)	
	public void updateStatus(FSMId fsmId, StateMachineStatus status) {
		((StateMachinesDAOV1)getDAO()).updateData(StateMachineUpdateField.status, 
				new StateMachineUpdate.StatusUpdate(fsmId, status));		
	}
    
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)	
    public long getExecutionVersionForUpdate(FSMId fsmId) {
    	return ((StateMachinesDAOV1)getDAO()).getExecutionVersionForUpdate(fsmId);
    }
    
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)	
    public void updateExecutionVersion(FSMId fsmId, Long smExecutionVersion) {
    	((StateMachinesDAOV1)getDAO()).updateData(StateMachineUpdateField.executionVersion, 
    			new StateMachineUpdate.ExecutionVersionUpdate(fsmId, smExecutionVersion));
    }
}
