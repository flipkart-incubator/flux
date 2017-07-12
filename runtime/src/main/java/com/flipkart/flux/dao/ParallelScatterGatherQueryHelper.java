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
    private final Map<String, ShardId> fluxShardKeyToShardIdMap;
    private final ExecutorService executorService;

    private static final Logger logger = LoggerFactory.getLogger(StatesDAOImpl.class);

    @Inject
    public ParallelScatterGatherQueryHelper(StatesDAO statesDAO, StateMachinesDAO stateMachinesDAO,
                                            @Named("fluxShardKeyToShardIdMap") Map<String, ShardId> fluxShardKeyToShardIdMap) {
        this.statesDAO = statesDAO;
        this.stateMachinesDAO = stateMachinesDAO;
        this.fluxShardKeyToShardIdMap = fluxShardKeyToShardIdMap;
        executorService = Executors.newFixedThreadPool(10);
    }


    public List findErroredStates(String stateMachineName, String fromStateMachineId, String toStateMachineId) {
        List result = Collections.synchronizedList(new ArrayList<>());
        scatterGatherQueryHelper((shardKey) ->
                statesDAO.findErroredStates(shardKey, stateMachineName, fromStateMachineId, toStateMachineId), result, "errored states");
        return result;
    }


    public List findStatesByStatus(String stateMachineName, Timestamp fromTime, Timestamp toTime, String taskName, List<Status> statuses) {
        List result = Collections.synchronizedList(new ArrayList<>());
        scatterGatherQueryHelper((shardKey) ->
                statesDAO.findStatesByStatus(shardKey, stateMachineName, fromTime, toTime, taskName, statuses), result, "states by status");
        return result;
    }

    public Set<StateMachine> findByName(String stateMachineName) {
        Set result = Collections.synchronizedSet(new HashSet<StateMachine>());
        scatterGatherQueryHelper((shardKey) ->
                stateMachinesDAO.findByName(shardKey, stateMachineName), result, "states by status");
        return result;
    }

    public Set<StateMachine> findByNameAndVersion(String stateMachineName, Long Version) {
        Set result = Collections.synchronizedSet(new HashSet<StateMachine>());
        scatterGatherQueryHelper((shardKey) ->
                stateMachinesDAO.findByNameAndVersion(shardKey, stateMachineName, Version), result, "states by status");
        return result;
    }

    private void scatterGatherQueryHelper(Function<String, Collection> reader, Collection result, String errorMessage) {
        Future[] tasksCompleted = new Future[fluxShardKeyToShardIdMap.size()];
        AtomicInteger tasks = new AtomicInteger(0);
        fluxShardKeyToShardIdMap.entrySet().forEach(entry -> {
            tasksCompleted[tasks.get()] = executorService.submit(() -> {
                try {
                    result.addAll(reader.apply(entry.getKey()));
                } catch (Exception ex) {
                    logger.error("Error in fetching {} from Slave with key {} , id {} {}",
                            errorMessage, entry.getKey(), entry.getValue(), ex.getStackTrace());
                }
            });
            tasks.getAndIncrement();
        });
        try {
            boolean allDone = false;
            while (!allDone) {
                allDone = true;
                for (int i = 0; i < fluxShardKeyToShardIdMap.size(); i++)
                    if (!tasksCompleted[i].isDone() && !tasksCompleted[i].isCancelled()) {
                        allDone = false;
                        break;
                    }
            }
        } catch (Exception e) {
            logger.error("Exception occured while gathering {} from Slaves : {}", errorMessage, e.getStackTrace());
        }
    }


}
