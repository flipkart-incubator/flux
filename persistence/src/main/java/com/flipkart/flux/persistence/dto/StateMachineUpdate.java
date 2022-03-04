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

import com.flipkart.flux.domain.StateMachineStatus;
import com.flipkart.flux.persistence.key.FSMId;

/**
 * Data container for performing updates on the @StateMachine entity in underlying data store.
 * Also defines valid fields/attributes for the update
 * @author regu.b
 *
 */
public class StateMachineUpdate {

	/**
	 * The recognized/valid fields for performing the update 
	 */
	public static enum Field {
		status, executionVersion
	}
	
	/** Update class for status */
	public static class StatusUpdate {		
		public FSMId fsmId;
		public StateMachineStatus status;
		public StatusUpdate(FSMId fsmId, StateMachineStatus status) {
			this.fsmId = fsmId;
			this.status = status;
		}
	}	
	
	/** Update class for execution version */
	public static class ExecutionVersionUpdate {
		public FSMId fsmId;
		public Long version;
		public ExecutionVersionUpdate(FSMId fsmId, Long version) {
			this.fsmId = fsmId;
			this.version = version;
		}		
	}
	
}
