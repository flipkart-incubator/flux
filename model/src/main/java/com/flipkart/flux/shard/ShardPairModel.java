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

/* Class which holds the details of a Mysql Shard Master Slave Pair
 * @author amitkumar.o
 */
public class ShardPairModel {
    private ShardId shardId;
    private String masterIp;
    private String slaveIp;
    private String startKey;
    private String endKey;

    public ShardPairModel(ShardId shardId, String masterIp, String slaveIp, String startKey, String endKey) {
        this.shardId = shardId;
        this.masterIp = masterIp;
        this.slaveIp = slaveIp;
        this.startKey = startKey;
        this.endKey = endKey;
    }

    public ShardPairModel() {
    }

    public ShardId getShardId() {
        return shardId;
    }

    public void setShardId(ShardId shardId) {
        this.shardId = shardId;
    }

    public String getMasterIp() {
        return masterIp;
    }

    public void setMasterIp(String masterIp) {
        this.masterIp = masterIp;
    }

    public String getSlaveIp() {
        return slaveIp;
    }

    public void setSlaveIp(String slaveIp) {
        this.slaveIp = slaveIp;
    }

    public String getStartKey() {
        return startKey;
    }

    public void setStartKey(String startKey) {
        this.startKey = startKey;
    }

    public String getEndKey() {
        return endKey;
    }

    public void setEndKey(String endKey) {
        this.endKey = endKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShardPairModel)) return false;

        ShardPairModel that = (ShardPairModel) o;

        if (!getShardId().equals(that.getShardId())) return false;
        if (!getMasterIp().equals(that.getMasterIp())) return false;
        if (!getSlaveIp().equals(that.getSlaveIp())) return false;
        if (!getStartKey().equals(that.getStartKey())) return false;
        return getEndKey().equals(that.getEndKey());

    }

    @Override
    public int hashCode() {
        int result = getShardId().hashCode();
        result = 31 * result + getMasterIp().hashCode();
        result = 31 * result + getSlaveIp().hashCode();
        result = 31 * result + getStartKey().hashCode();
        result = 31 * result + getEndKey().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ShardHostModel{" +
                "shardId=" + shardId +
                ", masterIp='" + masterIp + '\'' +
                ", slaveIp='" + slaveIp + '\'' +
                ", startKey='" + startKey + '\'' +
                ", endKey='" + endKey + '\'' +
                '}';
    }
}
