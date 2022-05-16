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

import com.flipkart.flux.shard.AbstractShardedEntity;
import com.flipkart.flux.shard.ShardId;

/**
 * A Criteria for constructing queries on the State Machines data store. The attributes of this class determine the data filters applied when retrieving the 
 * data.   
 * @author regu.b
 *
 */
public class FSMNameCriteria extends AbstractShardedEntity {

	public ShardId shardId; // needed to identify/specify the shard on which this the query formed by this criteria will execute on
	public String fsmName;
	public Long version;

	public FSMNameCriteria(ShardId shardId,String fsmName) {
		super(shardId);
		this.fsmName = fsmName;
	}

	public FSMNameCriteria(ShardId shardId,String fsmName, Long version) {
		super(shardId);
		this.fsmName = fsmName;
		this.version = version;
	}
	
}
