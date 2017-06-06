package com.flipkart.flux.shard.module;

import com.flipkart.flux.domain.AuditRecord;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.StateMachine;
import org.hibernate.shards.BaseHasShardIdList;
import org.hibernate.shards.ShardId;
import org.hibernate.shards.strategy.selection.ShardSelectionStrategy;

import java.security.MessageDigest;
import java.util.List;

/**
 * Created by amitkumar.o on 06/06/17.
 */
public class FluxShardSelectionStrategy extends BaseHasShardIdList implements ShardSelectionStrategy {

    public FluxShardSelectionStrategy(List<ShardId> shardIds) {
        super(shardIds);
    }

    @Override
    public ShardId selectShardIdForNewObject(Object obj) {
        Character hash  ;
        if (obj instanceof StateMachine) {
            hash = CryptHashGenerator.getUniformCryptHash(((StateMachine) obj).getId());
        } else if (obj instanceof State) {
            hash = CryptHashGenerator.getUniformCryptHash(((State) obj).getStateMachineId());
        } else if (obj instanceof AuditRecord) {
            hash = CryptHashGenerator.getUniformCryptHash(((AuditRecord) obj).getStateMachineInstanceId());
        } else if (obj instanceof Event) {
            hash = CryptHashGenerator.getUniformCryptHash(((Event) obj).getStateMachineInstanceId());
        }
        // logic to determine shard needs to be written here
        return shardIds.get(0);
    }

}
