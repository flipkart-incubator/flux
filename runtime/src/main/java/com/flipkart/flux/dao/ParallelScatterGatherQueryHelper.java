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
import com.flipkart.flux.shard.ShardId;
import com.flipkart.flux.shard.ShardPairModel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Helper class to facilitate scatter queries and gather results paralleled.
 *
 * @author amitkumar.o
 */
@Singleton
public class ParallelScatterGatherQueryHelper {
    private final StatesDAO statesDAO;
    private final StateMachinesDAO stateMachinesDAO;
    private final Map<ShardId, ShardPairModel> fluxShardIdToShardPairModelMap;
    private final ExecutorService executorService;

    private static final Logger logger = LoggerFactory.getLogger(StatesDAOImpl.class);

    @Inject
    public ParallelScatterGatherQueryHelper(StatesDAO statesDAO, StateMachinesDAO stateMachinesDAO,
                                            @Named("fluxShardIdToShardPairMap") Map<ShardId, ShardPairModel> fluxShardKeyToShardIdMap) {
        this.statesDAO = statesDAO;
        this.stateMachinesDAO = stateMachinesDAO;
        this.fluxShardIdToShardPairModelMap = fluxShardKeyToShardIdMap;
        executorService = Executors.newFixedThreadPool(10);
    }


    public List findErroredStates(String stateMachineName, Timestamp fromTime, Timestamp toTime) {
        List result = Collections.synchronizedList(new ArrayList<>());
        scatterGatherQueryHelper((shardId) ->
                statesDAO.findErroredStates(shardId, stateMachineName, fromTime, toTime), result, "errored states");
        return result;
    }


    public List findStatesByStatus(String stateMachineName, Timestamp fromTime, Timestamp toTime, String taskName, List<Status> statuses) {
        List result = Collections.synchronizedList(new ArrayList<>());
        scatterGatherQueryHelper((shardId) ->
                statesDAO.findStatesByStatus(shardId, stateMachineName, fromTime, toTime, taskName, statuses), result, "states by status");
        return result;
    }

    public Set<StateMachine> findStateMachinesByName(String stateMachineName) {
        Set result = Collections.synchronizedSet(new HashSet<StateMachine>());
        scatterGatherQueryHelper((shardId) ->
                stateMachinesDAO.findByName(shardId, stateMachineName), result, "stateMachines by name");
        return result;
    }

    public Set<StateMachine> findStateMachinesByNameAndVersion(String stateMachineName, Long Version) {
        Set result = Collections.synchronizedSet(new HashSet<StateMachine>());
        scatterGatherQueryHelper((shardId) ->
                stateMachinesDAO.findByNameAndVersion(shardId, stateMachineName, Version), result, "stateMachines by name and version");
        return result;
    }

    private void scatterGatherQueryHelper(Function<ShardId, Collection> reader, Collection result, String fetchOperation) {
        Future[] tasksCompleted = new Future[fluxShardIdToShardPairModelMap.size()];
        AtomicInteger tasks = new AtomicInteger(0);
        fluxShardIdToShardPairModelMap.entrySet().forEach(entry -> {
            tasksCompleted[tasks.get()] = executorService.submit(() -> {
                try {
                    result.addAll(reader.apply(entry.getKey()));
                } catch (Exception ex) {
                    logger.error("Error in fetching {} from Slave with key {} , id {} {}",
                            fetchOperation, entry.getKey(), entry.getValue(), ex.getStackTrace());
                }
            });
            tasks.getAndIncrement();
        });
        try {
            boolean allDone = false;
            while (!allDone) {
                allDone = true;
                for (int i = 0; i < fluxShardIdToShardPairModelMap.size(); i++)
                    if (!tasksCompleted[i].isDone() && !tasksCompleted[i].isCancelled()) {
                        allDone = false;
                        break;
                    }
            }
        } catch (Exception e) {
            logger.error("Exception occured while getting {} from Slaves : {}", fetchOperation, e.getStackTrace());
        }
    }


}
