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

import com.flipkart.flux.dao.iface.StatesDAO;
import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.Status;
import com.flipkart.flux.persistence.*;
import com.flipkart.flux.shard.ShardId;
import com.google.inject.name.Named;
import org.hibernate.Query;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.*;

/**
 * <code>StatesDAOImpl</code> is an implementation of {@link StatesDAO} which uses Hibernate to perform operations.
 *
 * @author shyam.akirala
 */
public class StatesDAOImpl extends AbstractDAO<State> implements StatesDAO {

    @Inject
    public StatesDAOImpl(@Named("fluxSessionFactoriesContext") SessionFactoryContext sessionFactoryContext) {
        super(sessionFactoryContext);
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public void updateState(String stateMachineInstanceId, State state) {
        super.update(state);
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public void updateStatus(String stateMachineId, Long stateId, Status status) {
        Query query = currentSession().createQuery("update State set status = :status where id = :stateId and stateMachineId = :stateMachineId");
        query.setString("status", status != null ? status.toString() : null);
        query.setLong("stateId", stateId);
        query.setString("stateMachineId", stateMachineId);
        query.executeUpdate();
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public void updateRollbackStatus(String stateMachineId, Long stateId, Status rollbackStatus) {
        Query query = currentSession().createQuery("update State set rollbackStatus = :rollbackStatus where id = :stateId and stateMachineId = :stateMachineId");
        query.setString("rollbackStatus", rollbackStatus != null ? rollbackStatus.toString() : null);
        query.setLong("stateId", stateId);
        query.setString("stateMachineId", stateMachineId);
        query.executeUpdate();
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public void incrementRetryCount(String stateMachineId, Long stateId) {
        Query query = currentSession().createQuery("update State set attemptedNoOfRetries = attemptedNoOfRetries + 1 where id = :stateId and stateMachineId = :stateMachineId");
        query.setLong("stateId", stateId);
        query.setString("stateMachineId", stateMachineId);
        query.executeUpdate();
    }

    /**
     * Query should go to Default Shard , As it is a redriver Task
     *
     * @param id
     * @return
     */
    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public State findById(String stateMachineId, Long id) {
        return super.findByCompositeIdFromStateTable(State.class, stateMachineId, id);
    }

    @SuppressWarnings("rawtypes")
	@Transactional
    @SelectDataSource(type = DataSourceType.READ_ONLY, storage = Storage.SHARDED)
    public List findErroredStates(ShardId shardId, String stateMachineName, Timestamp fromTime, Timestamp toTime) {
        List<Status> statuses = new ArrayList<>();
        statuses.add(Status.errored);
        return findStatesByStatus(shardId, stateMachineName, fromTime, toTime, null, statuses);
    }

    @SuppressWarnings("rawtypes")
	@Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_ONLY, storage = Storage.SHARDED)
    public List findStatesByStatus(ShardId shardId, String stateMachineName, Timestamp fromTime, Timestamp toTime, String stateName, List<Status> statuses) {
        Query query;
        String queryString = "select state.stateMachineId, state.id, state.status from State state join StateMachine sm " +
                "on sm.id = state.stateMachineId and sm.createdAt between :fromTime and :toTime and sm.name = :stateMachineName";

        if (statuses != null && !statuses.isEmpty()) {
            StringBuilder sb = new StringBuilder(" and state.status in (");
            for (Status status : statuses) {
                sb.append("'").append(status.toString()).append("',");
            }
            sb.deleteCharAt(sb.length() - 1).append(")");
            String statusClause = sb.toString();
            queryString = queryString.concat(statusClause);
        }

        if (stateName == null) {
            query = currentSession().createQuery(queryString);
        } else {
            query = currentSession().createQuery(queryString + " and state.name = :stateName");
            query.setString("stateName", stateName);
        }

        query.setString("stateMachineName", stateMachineName);
        query.setTimestamp("fromTime", fromTime);
        query.setTimestamp("toTime", toTime);
        return query.list();
    }

    @SuppressWarnings("rawtypes")
	@Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public List findStatesByDependentEvent(String stateMachineId, String eventName) {
        Query query;
        String queryString = "select id, stateMachineId, status from State where stateMachineId = :stateMachineId" +
                " and dependencies like :eventName";
        query = currentSession().createQuery(queryString);
        query.setString("stateMachineId", stateMachineId);
        query.setString("eventName", "%" + eventName + "%");
        return query.list();
    }
}
