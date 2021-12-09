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
import com.flipkart.flux.persistence.DataSourceType;
import com.flipkart.flux.persistence.SelectDataSource;
import com.flipkart.flux.persistence.SessionFactoryContext;
import com.flipkart.flux.persistence.Storage;
import com.flipkart.flux.shard.ShardId;
import com.google.inject.name.Named;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

/**
 * <code>StatesDAOImpl</code> is an implementation of {@link StatesDAO} which uses Hibernate to perform operations.
 *
 * @author shyam.akirala
 */
// TODO : Add tests for all newly added queries
public class StatesDAOImpl extends AbstractDAO<State> implements StatesDAO {

    private static final String TABLE_NAME = "State";

    private static final String COLUMN_ATTEMPTED_NUM_OF_REPLAYABLE_RETRIES = "attemptedNumOfReplayableRetries";

    private static final String COLUMN_ID = "id";

    private static final String COLUMN_STATE_MACHINE_ID = "stateMachineId";

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
    public void updateStatus(String stateMachineInstanceId, List<State> states, Status status) {
        StringBuilder inClause = new StringBuilder();
        if (states!=null && !states.isEmpty()) {
            inClause.append(" and id in (");
            for (State state : states) {
                inClause.append(state.getId()).append(",");
            }
        }
        inClause.deleteCharAt(inClause.length() - 1).append(")");
        Query query = currentSession().createQuery("update State set status = :status where stateMachineId = :stateMachineId".concat(inClause.toString()));
        query.setString("status", status != null ? status.toString() : null);
        query.setString("stateMachineId", stateMachineInstanceId);
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

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    //TODO: check query
    public void incrementReplayableRetries(String stateMachineId, Long stateId, Short attemptedNumOfReplayableRetries) {
        Query query = currentSession().createQuery("update " + TABLE_NAME + " set " + COLUMN_ATTEMPTED_NUM_OF_REPLAYABLE_RETRIES + " = :attemptedNumOfReplayableRetries " +
                " where " + COLUMN_ID + " = :stateId and " + COLUMN_STATE_MACHINE_ID + " = :stateMachineId");
        query.setLong("stateId", stateId);
        query.setString("stateMachineId", stateMachineId);
        query.setShort("attemptedNumOfReplayableRetries", attemptedNumOfReplayableRetries);
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

    /**
     * @return list of all the states for the given state ids
     */
    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public List<State> findAllStatesForGivenStateIds(String stateMachineId, List<Long> stateIds) {
        String inClause = stateIds.toString().replace("[","(").replace("]",")");
        Query query = currentSession().createQuery(
                "select s from State s where stateMachineId = :stateMachineId and id in " + inClause);
        query.setString("stateMachineId",stateMachineId);
        return query.list();
    }


    @Transactional
    @SelectDataSource(type = DataSourceType.READ_ONLY, storage = Storage.SHARDED)
    public List findErroredStates(ShardId shardId, String stateMachineName, Timestamp fromTime, Timestamp toTime) {
        List<Status> statuses = new ArrayList<>();
        statuses.add(Status.errored);
        return findStatesByStatus(shardId, stateMachineName, fromTime, toTime, null, statuses);
    }

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
                sb.append("'" + status.toString() + "',");
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

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public List<State> findStatesByDependentEvent(String stateMachineId, String eventName) {
        Query query = currentSession().createQuery(
                "select state from State state where stateMachineId = :stateMachineId and" +
                        " dependencies like :eventName");
        query.setString("stateMachineId", stateMachineId);
        query.setString("eventName", "%" + eventName + "%");
        return query.list();
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public Long findStateIdByEventName(String stateMachineId, String eventName) {
        SQLQuery query = currentSession().createSQLQuery(
                "select id from States where `stateMachineId`= '" + stateMachineId + "' and dependencies like" +
                        " '%" + eventName + "%'");
        return ((BigInteger) query.uniqueResult()).longValue();
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public void updateExecutionVersion(String stateMachineId, Long stateId, Long executionVersion) {
        Query query = currentSession().createQuery("update State set executionVersion = :executionVersion" +
                " where id = :stateId and stateMachineId = :stateMachineId");
        query.setLong("executionVersion", executionVersion);
        query.setLong("stateId", stateId);
        query.setString("stateMachineId", stateMachineId);
        query.executeUpdate();
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public void updateExecutionVersion(String stateMachineId, List<State> states, Long executionVersion) {
        StringBuilder inClause = new StringBuilder();
        if (states!=null && !states.isEmpty()) {
            inClause.append(" and id in (,");
            for (State state : states) {
                inClause.append(state.getId()).append(",");
            }
            inClause.deleteCharAt(inClause.length() - 1).append(")");
        }
        Query query = currentSession().createQuery("update State set executionVersion= :executionVersion" +
                " where stateMachineId= :stateMachineId".concat(inClause.toString()));
        query.setLong("executionVersion", executionVersion);
        query.setString("stateMachineId", stateMachineId);
        query.executeUpdate();
    }

    // TODO : Add test for this
    @Override
    public void updateExecutionVersion_NonTransactional(String stateMachineId,
        List<Long> stateIds, Long executionVersion, Session session) {

        String inClause = stateIds.toString().replace("[","(").replace("]",")");
        Query query = session.createQuery("update State set executionVersion= :executionVersion" +
            " where stateMachineId= :stateMachineId and id in ".concat(inClause.toString()));
        query.setLong("executionVersion", executionVersion);
        query.setString("stateMachineId", stateMachineId);
        query.executeUpdate();
    }

    // TODO : Add test for this
    @Override
    public void updateStatus_NonTransactional(String stateMachineInstanceId, List<Long> stateIds, Status status,
                                              Session session) {
        String inClauseQuery = stateIds.toString().replace("[","(").replace("]",")");
        Query query = session.createQuery(
                "update State set status = :status where stateMachineId = :stateMachineId and id in "
                        .concat(inClauseQuery));
        query.setString("status", status != null ? status.toString() : null);
        query.setString("stateMachineId", stateMachineInstanceId);
        query.executeUpdate();
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public void updateReplayableRetries(String stateMachineId, Long stateId, Short replayableRetries) {
        Query query = currentSession().createQuery("update " + TABLE_NAME + " set " + COLUMN_ATTEMPTED_NUM_OF_REPLAYABLE_RETRIES + " = :attemptedNumOfReplayableRetries" +
                " where " + COLUMN_ID + " = :stateId and " + COLUMN_STATE_MACHINE_ID + " = :stateMachineId");
        query.setString("stateMachineId", stateMachineId);
        query.setLong("stateId", stateId);
        query.setShort("attemptedNumOfReplayableRetries", replayableRetries);
        query.executeUpdate();
    }
}