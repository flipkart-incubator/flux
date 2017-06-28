package com.flipkart.flux.persistence;

/**
 * Interface to get Shard Key from all Persistent Object Models
 */
public interface ShardKey {

    String getShardKey();
}
