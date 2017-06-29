package com.flipkart.flux.dao;

import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.dao.iface.StatesDAO;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.domain.Status;
import com.flipkart.flux.shard.ShardHostModel;
import com.flipkart.flux.shard.ShardId;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

/**
 * Helper class to facilitate scatter gather queries from Slave Shards
 *
 * @author amitkumar.o
 */
public class ParallelScatterGatherQueryHelper {
    private Map<ShardId, ShardHostModel> fluxROShardIdToShardMap;
    private StatesDAO statesDAO;
    private StateMachinesDAO stateMachinesDAO;

    private static final Logger logger = LoggerFactory.getLogger(StatesDAOImpl.class);

    @Inject
    public ParallelScatterGatherQueryHelper(@Named("fluxROShardIdToShardMapping") Map<ShardId, ShardHostModel> fluxROShardIdToShardMap,
                                            StatesDAO statesDAO, StateMachinesDAO stateMachinesDAO) {
        this.fluxROShardIdToShardMap = fluxROShardIdToShardMap;
        this.statesDAO = statesDAO;
        this.stateMachinesDAO = stateMachinesDAO;
    }


    public List findErroredStates(String stateMachineName, String fromStateMachineId, String toStateMachineId) {
        List result = Collections.synchronizedList(new ArrayList<>());
        helper((shardKey) ->
                statesDAO.findErroredStates(shardKey, stateMachineName, fromStateMachineId, toStateMachineId), result , "errored states");
        return  result;
    }


    public List findStatesByStatus(String stateMachineName, Timestamp fromTime, Timestamp toTime, String taskName, List<Status> statuses) {
        List result = Collections.synchronizedList(new ArrayList<>());
        helper((shardKey) ->
                statesDAO.findStatesByStatus(shardKey, stateMachineName, fromTime, toTime, taskName, statuses),result , "states by status");
        return result;
    }

    public Set<StateMachine> findByName(String stateMachineName){
        Set result = Collections.synchronizedSet(new HashSet<StateMachine>());
        helper((shardKey) ->
                stateMachinesDAO.findByName(shardKey, stateMachineName), result , "states by status");
        return  result;
    }

    public Set<StateMachine> findByNameAndVersion(String stateMachineName, Long Version){
        Set result = Collections.synchronizedSet(new HashSet<StateMachine>());
        helper((shardKey) ->
                stateMachinesDAO.findByNameAndVersion(shardKey, stateMachineName, Version), result , "states by status");
        return  result;
    }

    private void helper(Function<ShardId, Collection> reader, Collection result, String errorMessage) {
        // noOfThreads spawned = no. of Slave Shards
        final CountDownLatch latch = new CountDownLatch(fluxROShardIdToShardMap.size());
        fluxROShardIdToShardMap.entrySet().forEach(entry -> {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        result.addAll(reader.apply(entry.getKey()));
                    } catch (Exception ex) {
                        logger.error("Error in fetching {} from Slave with id {} , ip {} {}",
                                errorMessage ,entry.getKey(), entry.getValue().getIp(), ex.getStackTrace());
                    } finally {
                        latch.countDown();
                    }
                }
                }).run();
        });
        try {
            // wait till all the results are returned from all shards
            latch.await();
        } catch (InterruptedException e) {
            logger.error("Exception occured while gathering {} from Slaves : {}", errorMessage ,e.getStackTrace());
        }
    }


}
