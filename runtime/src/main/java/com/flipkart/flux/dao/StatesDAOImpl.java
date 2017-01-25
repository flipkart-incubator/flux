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
import com.google.inject.name.Named;
import org.hibernate.Query;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.List;

/**
 * <code>StatesDAOImpl</code> is an implementation of {@link StatesDAO} which uses Hibernate to perform operations.
 * @author shyam.akirala
 */
public class StatesDAOImpl extends AbstractDAO<State> implements StatesDAO {

    @Inject
    public StatesDAOImpl(@Named("fluxSessionFactoryContext") SessionFactoryContext sessionFactoryContext) {
        super(sessionFactoryContext);
    }

    @Override
    @Transactional
    public State create(State state) {
        return super.save(state);
    }

    @Override
    @Transactional
    public void updateState(State state) {
        super.update(state);
    }

    @Override
    @Transactional
    public void updateStatus(Long stateId, Long stateMachineId, Status status) {
        Query query = currentSession().createQuery("update State set status = :status where id = :stateId and stateMachineId = :stateMachineId");
        query.setString("status", status != null ? status.toString() : null);
        query.setLong("stateId", stateId);
        query.setLong("stateMachineId", stateMachineId);
        query.executeUpdate();
    }

    @Override
    @Transactional
    public void updateRollbackStatus(Long stateId, Long stateMachineId, Status rollbackStatus) {
        Query query = currentSession().createQuery("update State set rollbackStatus = :rollbackStatus where id = :stateId and stateMachineId = :stateMachineId");
        query.setString("rollbackStatus", rollbackStatus != null ? rollbackStatus.toString() : null);
        query.setLong("stateId", stateId);
        query.setLong("stateMachineId", stateMachineId);
        query.executeUpdate();
    }

    @Override
    @Transactional
    public void incrementRetryCount(Long stateId, Long stateMachineId) {
        Query query = currentSession().createQuery("update State set attemptedNoOfRetries = attemptedNoOfRetries + 1 where id = :stateId and stateMachineId = :stateMachineId");
        query.setLong("stateId", stateId);
        query.setLong("stateMachineId", stateMachineId);
        query.executeUpdate();
    }

    @Override
    @Transactional
    public State findById(Long id) {
        return super.findById(State.class, id);
    }

    @Override
    @Transactional
    @SelectDataSource(DataSourceType.READ_ONLY)
    public List findErroredStates(String stateMachineName, Long fromStateMachineId, Long toStateMachineId) {
        Query query = currentSession().createQuery("select state.stateMachineId, state.id, state.status from StateMachine sm join sm.states state " +
                "where sm.id between :fromStateMachineId and :toStateMachineId and sm.name = :stateMachineName and state.status in ('errored', 'sidelined', 'cancelled')");

        query.setLong("fromStateMachineId", fromStateMachineId);
        query.setLong("toStateMachineId", toStateMachineId);
        query.setString("stateMachineName", stateMachineName);

        return query.list();
    }

    @Override
    @Transactional
    @SelectDataSource(DataSourceType.READ_ONLY)
    public List findErroredStates(String stateMachineName, Timestamp fromTime, Timestamp toTime, String stateName) {
        Query query;
        if(stateName == null) {
            query = currentSession().createQuery("select state.stateMachineId, state.id, state.status from StateMachine sm join sm.states state " +
                    "where sm.id between (select min(id) from StateMachine where createdAt >= :fromTime) and (select max(id) from StateMachine where createdAt <= :toTime) " +
                    "and sm.name = :stateMachineName and state.status in ('errored', 'sidelined', 'cancelled')");
        } else {
            query = currentSession().createQuery("select state.stateMachineId, state.id, state.status from StateMachine sm join sm.states state " +
                    "where sm.id between (select min(id) from StateMachine where createdAt >= :fromTime) and (select max(id) from StateMachine where createdAt <= :toTime) " +
                    "and sm.name = :stateMachineName and state.name = :stateName and state.status in ('errored', 'sidelined', 'cancelled')");
            query.setString("stateName", stateName);
        }

        query.setString("stateMachineName", stateMachineName);
        query.setTimestamp("fromTime", fromTime);
        query.setTimestamp("toTime", toTime);

        return query.list();
    }
}
