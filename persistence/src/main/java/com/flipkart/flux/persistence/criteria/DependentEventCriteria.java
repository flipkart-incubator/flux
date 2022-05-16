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
package com.flipkart.flux.persistence.criteria;

import com.flipkart.flux.persistence.key.FSMId;
import com.flipkart.flux.shard.AbstractShardedEntity;

/**
 * A Criteria for constructing queries on the States data store. The attributes of this class determine the data filters applied when retrieving the 
 * data.   
 * @author regu.b
 *
 */public class DependentEventCriteria extends AbstractShardedEntity {
	
	public FSMId fsmId;
	public String eventName;
	public DependentEventCriteria(FSMId fsmId, String eventName) {
		super(fsmId.statemachineId);
		this.fsmId = fsmId;
		this.eventName = eventName;
	}	

}
