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

import com.flipkart.flux.dao.iface.StateTransitionDAO;
import com.flipkart.flux.domain.StateTransition;
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
 * <code>StateTransitionDAOImpl</code> is an implementation of {@link StateTransitionDAO} which uses Hibernate to perform operations.
 *
 * @author akif.khan
 */
public class StateTransitionDAOImpl extends AbstractDAO<StateTransition> implements StateTransitionDAO {


    @Inject
    public StateTransitionDAOImpl(@Named("fluxSessionFactoriesContext") SessionFactoryContext sessionFactoryContext) {
        super(sessionFactoryContext);
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public void updateStateTransition(String stateMachineInstanceId, StateTransition stateTransition) {
        super.update(stateTransition);
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public void updateStatus(String stateMachineId, Long stateId, Status status) {
        Query query = currentSession().createQuery("update StateTransition set status = :status where id = :stateId and stateMachineId = :stateMachineId");
        query.setString("status", status != null ? status.toString() : null);
        query.setLong("stateId", stateId);
        query.setString("stateMachineId", stateMachineId);
        query.executeUpdate();
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public void updateRollbackStatus(String stateMachineId, Long stateId, Status rollbackStatus) {
        Query query = currentSession().createQuery("update StateTransition set rollbackStatus = :rollbackStatus where id = :stateId and stateMachineId = :stateMachineId");
        query.setString("rollbackStatus", rollbackStatus != null ? rollbackStatus.toString() : null);
        query.setLong("stateId", stateId);
        query.setString("stateMachineId", stateMachineId);
        query.executeUpdate();
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public void incrementRetryCount(String stateMachineId, Long stateId) {
        Query query = currentSession().createQuery("update StateTransition set attemptedNoOfRetries = attemptedNoOfRetries + 1 where id = :stateId and stateMachineId = :stateMachineId");
        query.setLong("stateId", stateId);
        query.setString("stateMachineId", stateMachineId);
        query.executeUpdate();
    }

    /**
     * @param id
     * @return
     */
    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public StateTransition findById(String stateMachineId, Long id) {
        return super.findByCompositeIdFromStateTable(StateTransition.class, stateMachineId ,id);
    }


    @Transactional
    @SelectDataSource(type = DataSourceType.READ_ONLY, storage = Storage.SHARDED)
    public List findErroredStates(ShardId shardId, String stateMachineName, Timestamp fromTime, Timestamp toTime) {
        List<Status> statuses = new ArrayList<>();
        statuses.add(Status.errored);
        return findStateTransitionByStatus(shardId, stateMachineName, fromTime, toTime, null, statuses);
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_ONLY, storage = Storage.SHARDED)
    public List findStateTransitionByStatus(ShardId shardId, String stateMachineName, Timestamp fromTime, Timestamp toTime, String stateName, List<Status> statuses) {
        Query query;
        String queryString = "select stateTransition.stateMachineId, stateTransition.id, stateTransition.status from StateTransition stateTransition join StateMachine sm " +
                "on sm.id = stateTransition.stateMachineId and sm.createdAt between :fromTime and :toTime and sm.name = :stateMachineName";

        if (statuses != null && !statuses.isEmpty()) {
            StringBuilder sb = new StringBuilder(" and stateTransition.status in (");
            for (Status status : statuses) {
                sb.append("'" + status.toString() + "',");
            }
            sb.deleteCharAt(sb.length() - 1).append(")");
            String statusClause = sb.toString();
            queryString = queryString.concat(statusClause);
        }

        if (stateName == null) {
            query = currentSession().createQuery(queryString);
        } else {
            query = currentSession().createQuery(queryString + " and stateTransition.name = :stateName");
            query.setString("stateName", stateName);
        }

        query.setString("stateMachineName", stateMachineName);
        query.setTimestamp("fromTime", fromTime);
        query.setTimestamp("toTime", toTime);
        return query.list();
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public List findStateTransitionByDependentEvent(String stateMachineId, String eventName) {
        Query query;
        String queryString = "select id, stateMachineId, status from StateTransition where stateMachineId = :stateMachineId" +
                " and dependencies like :eventName";
        query = currentSession().createQuery(queryString);
        query.setString("stateMachineId", stateMachineId);
        query.setString("eventName", "%"+eventName+"%");
        return query.list();
    }
}
