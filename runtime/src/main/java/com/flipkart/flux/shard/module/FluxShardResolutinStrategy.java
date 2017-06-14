package com.flipkart.flux.shard.module;

import com.flipkart.flux.domain.AuditRecord;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.StateMachine;
import org.hibernate.shards.ShardId;
import org.hibernate.shards.strategy.resolution.ShardResolutionStrategy;
import org.hibernate.shards.strategy.selection.ShardResolutionStrategyData;

import java.util.List;

/**
 * Created by amitkumar.o on 06/06/17.
 */
public class FluxShardResolutinStrategy implements ShardResolutionStrategy {
    private List<ShardId> shardIds;

    public FluxShardResolutinStrategy(List<ShardId> shardIds) {
        this.shardIds = shardIds;
    }

    @Override
    public List<ShardId> selectShardIdsFromShardResolutionStrategyData(ShardResolutionStrategyData shardResolutionStrategyData) {
        Character hash;
        if (shardResolutionStrategyData.getId().toString().length() > 0) {
            if (shardResolutionStrategyData.getEntityName().equals(StateMachine.class.getName())) {
                hash = CryptHashGenerator.getUniformCryptHash(shardResolutionStrategyData.getId().toString());
            } else if (shardResolutionStrategyData.getEntityName().equals(State.class.getName())) {
                hash = CryptHashGenerator.getUniformCryptHash(shardResolutionStrategyData.getId().toString());
            } else if (shardResolutionStrategyData.getEntityName().equals(Event.class.getName())) {
                hash = CryptHashGenerator.getUniformCryptHash(shardResolutionStrategyData.getId().toString());
            } else if (shardResolutionStrategyData.getEntityName().equals(AuditRecord.class.getName())) {
                hash = CryptHashGenerator.getUniformCryptHash(shardResolutionStrategyData.getId().toString());
            }
        }
        // logic to select shard and return only one shard Id in the list
        return shardIds;
    }
}
