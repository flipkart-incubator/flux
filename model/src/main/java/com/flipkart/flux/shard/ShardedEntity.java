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
package com.flipkart.flux.shard;

/**
 * A marker interface to indicate that any derived type can be persisted in a sharded (i.e physically split/distributed manner) set up.
 * The destination shard is identified by either a @ShardId or a shard identifier String.  
 * 
 * @author regu.b
 *
 */

public interface ShardedEntity {
	
	/**
	 * null or the ShardId for the entity
	 * @return null or a valid ShardId
	 */
	public ShardId getShardId();
	/**
	 * null or the shard identifying key for the entity
	 * @return null or a valid shard key
	 */
	public String getShardKey();
	
}
