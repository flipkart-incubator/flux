package com.flipkart.flux.shard.module;

import org.hibernate.shards.strategy.access.ParallelShardAccessStrategy;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by amitkumar.o on 06/06/17.
 */
public class FluxShardAccessStrategy extends ParallelShardAccessStrategy{
   // need to implement how to give executor
    public FluxShardAccessStrategy(ThreadPoolExecutor executor) {
        super(executor);
    }
}
