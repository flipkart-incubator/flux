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
 * An abstract implementatiobn for {@link ShardedEntity} 
 * @author regu.b
 *
 */
public abstract class AbstractShardedEntity implements ShardedEntity {
	private ShardId shardId;
	private String shardKey;
	public AbstractShardedEntity(ShardId shardId) {
		this.shardId = shardId;
	}
	public AbstractShardedEntity(String shardKey) {
		this.shardKey = shardKey;
	}
	@Override
	public ShardId getShardId() {
		return shardId;
	}
	@Override
	public String getShardKey() {
		return shardKey;
	}
	public void setShardId(ShardId shardId) {
		this.shardId = shardId;
	}
	public void setShardKey(String shardKey) {
		this.shardKey = shardKey;
	}	
	@Override
	public boolean equals(Object object) {		
		if (object == null || !AbstractShardedEntity.class.isAssignableFrom(object.getClass())) {
			return false;
		}
		AbstractShardedEntity anotherEntity = (AbstractShardedEntity)object;
		if (this.getShardId() != null && anotherEntity.getShardId()!= null) {
			return this.getShardId().equals(anotherEntity.getShardId());
		}
		if (this.getShardKey() != null && anotherEntity.getShardKey() != null) {
			return this.getShardKey().equals(anotherEntity.getShardKey());
		}
		return false;
	}
	@Override
	public String toString() {
		return "ShardedEntity{" +
                "shardId=" + shardId + 
                ",shardKey=" + shardKey +
                '}';
	}
}
