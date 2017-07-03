package com.flipkart.flux.shard;

import java.util.List;

/**
 * Created by amitkumar.o on 05/06/17.
 */
public class ShardHostModel {
    private ShardId shardId;
    private String ip;
    private List<Character> shardKeys;

    public ShardHostModel(ShardId shardId, String ip, List<Character> shardKeys) {
        this.shardId = shardId;
        this.ip = ip;
        this.shardKeys = shardKeys;
    }

    public ShardHostModel() {
    }

    public ShardId getShardId() {
        return shardId;
    }

    public void setShardId(ShardId shardId) {
        this.shardId = shardId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public List<Character> getShardKeys() {
        return shardKeys;
    }

    public void setShardKeys(List<Character> shardKeys) {
        this.shardKeys = shardKeys;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShardHostModel)) return false;

        ShardHostModel that = (ShardHostModel) o;

        if (getShardId() != that.getShardId()) return false;
        if (!getIp().equals(that.getIp())) return false;
        return getShardKeys().equals(that.getShardKeys());

    }

    @Override
    public int hashCode() {
        int result = getShardId().hashCode();
        result = 31 * result + getIp().hashCode();
        result = 31 * result + getShardKeys().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ShardHostModel{" +
                "shardId=" + shardId +
                ", ip='" + ip + '\'' +
                ", shardKeys=" + shardKeys +
                '}';
    }
}
