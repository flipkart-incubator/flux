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
 * Unique numeric Identifier for a Particular MySQl shard
 * @author amitkumar.o
 */
public class ShardId {

    private int shardId;

    public ShardId(int shardId) {
        this.shardId = shardId;
    }

    public int getShardId() {
        return shardId;
    }

    public void setShardId(int shardId) {
        this.shardId = shardId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShardId)) return false;
        ShardId shardId1 = (ShardId) o;
        return getShardId() == shardId1.getShardId();
    }

    @Override
    public int hashCode() {
        return getShardId();
    }

    @Override
    public String toString() {
        return "ShardId{" +
                "shardId=" + shardId +
                '}';
    }
}


