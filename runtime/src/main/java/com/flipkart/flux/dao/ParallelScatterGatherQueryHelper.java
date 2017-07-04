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

package com.flipkart.flux.dao;

import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.dao.iface.StatesDAO;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.domain.Status;
import com.flipkart.flux.shard.ShardHostModel;
import com.flipkart.flux.shard.ShardId;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

/**
 * Helper class to facilitate scatter queries and gather results paralleled.
 *
 * @author amitkumar.o
 */
@Singleton
public class ParallelScatterGatherQueryHelper {
    private Map<ShardId, ShardHostModel> fluxROShardIdToShardMap;
    private StatesDAO statesDAO;
    private StateMachinesDAO stateMachinesDAO;

    private static final Logger logger = LoggerFactory.getLogger(StatesDAOImpl.class);

    @Inject
    public ParallelScatterGatherQueryHelper(@Named("fluxROShardIdToShardMapping") Map fluxROShardIdToShardMap, StatesDAO statesDAO, StateMachinesDAO stateMachinesDAO) {
        this.fluxROShardIdToShardMap = fluxROShardIdToShardMap;
        this.statesDAO = statesDAO;
        this.stateMachinesDAO = stateMachinesDAO;
    }


    public List findErroredStates(String stateMachineName, String fromStateMachineId, String toStateMachineId) {
        List result = Collections.synchronizedList(new ArrayList<>());
        scatterGatherQueryHelper((shardKey) ->
                statesDAO.findErroredStates(shardKey, stateMachineName, fromStateMachineId, toStateMachineId), result , "errored states");
        return  result;
    }


    public List findStatesByStatus(String stateMachineName, Timestamp fromTime, Timestamp toTime, String taskName, List<Status> statuses) {
        List result = Collections.synchronizedList(new ArrayList<>());
        scatterGatherQueryHelper((shardKey) ->
                statesDAO.findStatesByStatus(shardKey, stateMachineName, fromTime, toTime, taskName, statuses),result , "states by status");
        return result;
    }

    public Set<StateMachine> findByName(String stateMachineName){
        Set result = Collections.synchronizedSet(new HashSet<StateMachine>());
        scatterGatherQueryHelper((shardKey) ->
                stateMachinesDAO.findByName(shardKey, stateMachineName), result , "states by status");
        return  result;
    }

    public Set<StateMachine> findByNameAndVersion(String stateMachineName, Long Version){
        Set result = Collections.synchronizedSet(new HashSet<StateMachine>());
        scatterGatherQueryHelper((shardKey) ->
                stateMachinesDAO.findByNameAndVersion(shardKey, stateMachineName, Version), result , "states by status");
        return  result;
    }

    private void scatterGatherQueryHelper(Function<ShardId, Collection> reader, Collection result, String errorMessage) {
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
