package com.flipkart.flux.shard.module;

import org.hibernate.shards.strategy.ShardStrategyImpl;
import org.hibernate.shards.strategy.access.ShardAccessStrategy;
import org.hibernate.shards.strategy.resolution.ShardResolutionStrategy;
import org.hibernate.shards.strategy.selection.ShardSelectionStrategy;

/**
 * Created by amitkumar.o on 06/06/17.
 */
public class FluxShardStrategy extends ShardStrategyImpl {

    public FluxShardStrategy(ShardSelectionStrategy FluxShardSelectionStrategy, ShardResolutionStrategy
            FluxShardResolutionStrategy, ShardAccessStrategy FluxShardAccessStrategy) {
        super(FluxShardSelectionStrategy, FluxShardResolutionStrategy, FluxShardAccessStrategy);
    }
}
