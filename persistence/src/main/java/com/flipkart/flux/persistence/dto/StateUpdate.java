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
package com.flipkart.flux.persistence.dto;

import com.flipkart.flux.domain.Status;
import com.flipkart.flux.persistence.key.FSMId;
import com.flipkart.flux.persistence.key.FSMIdEntityId;

/**
 * Data container for performing updates on the @State entity in underlying data store.
 * Also defines valid fields/attributes for the update
 * @author regu.b
 *
 */
public class StateUpdate {

	/**
	 * The recognized/valid fields for performing the update 
	 */
	public static enum Field {
		status, rollbackStatus, attemptedNoOfRetries, attemptedNumOfReplayableRetries,executionVersion
	}
	
	/** Update class for  status */
	public static class StatusUpdate {		
		public FSMId fsmId;
		public Long[] stateIds;
		public Status status;
		public StatusUpdate(FSMId fsmId, Long[] stateIds, Status status) {
			this.fsmId = fsmId;
			this.stateIds = stateIds;
			this.status = status;
		}
	}

	/** Update class for rollback status */
	public static class RollbackStatusUpdate {		
		public FSMIdEntityId fsmIdEntityId;
		public Status status;
		public RollbackStatusUpdate(FSMIdEntityId fsmIdEntityId, Status status) {
			this.fsmIdEntityId = fsmIdEntityId;
			this.status = status;
		}
	}
	
	/** Update class for no of retries increment*/
	public static class NoOfRetriesIncrement {
		public FSMIdEntityId fsmIdEntityId;
		public NoOfRetriesIncrement(FSMIdEntityId fsmIdEntityId) {
			this.fsmIdEntityId = fsmIdEntityId;
		}		
	}

	/** Update class for replayable retries */
	public static class ReplayableRetriesUpdate {
		public FSMIdEntityId fsmIdEntityId;
		public Short retries;
		public ReplayableRetriesUpdate(FSMIdEntityId fsmIdEntityId, Short retries) {
			this.fsmIdEntityId = fsmIdEntityId;
			this.retries = retries;
		}		
	}

	/** Update class for execution version */
	public static class ExecutionVersionUpdate {
		public FSMIdEntityId fsmIdEntityId;
		public Long version;
		public ExecutionVersionUpdate(FSMIdEntityId fsmIdEntityId, Long version) {
			this.fsmIdEntityId = fsmIdEntityId;
			this.version = version;
		}		
	}
	
}
