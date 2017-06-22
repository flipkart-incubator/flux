package com.flipkart.flux.shard;

/**
 * Created by amitkumar.o on 19/06/17.
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


