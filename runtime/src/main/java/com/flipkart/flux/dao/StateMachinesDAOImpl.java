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
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.domain.StateMachineStatus;
import com.flipkart.flux.persistence.*;
import com.flipkart.flux.shard.ShardId;
import com.google.inject.name.Named;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <code>StateMachinesDAOImpl</code> is an implementation of {@link StateMachinesDAO} which uses Hibernate to perform operations.
 *
 * @author shyam.akirala
 */
public class StateMachinesDAOImpl extends AbstractDAO<StateMachine> implements StateMachinesDAO {

    @Inject
    public StateMachinesDAOImpl(@Named("fluxSessionFactoriesContext") SessionFactoryContext sessionFactoryContext) {
        super(sessionFactoryContext);
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = storage.SHARDED)
    public StateMachine create(String stateMachineInstanceId, StateMachine stateMachine) {
        return super.save(stateMachine);
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = storage.SHARDED)
    public StateMachine findById(String stateMachineInstanceId) {
        return super.findById(StateMachine.class, stateMachineInstanceId);
    }

    // scatter gather query
    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_ONLY, storage = storage.SHARDED)
    public Set<StateMachine> findByName(ShardId shardId, String stateMachineName) {
        Criteria criteria = currentSession().createCriteria(StateMachine.class)
                .add(Restrictions.eq("name", stateMachineName));
        List<StateMachine> stateMachines = criteria.list();
        return new HashSet<>(stateMachines);
    }

    //Scatter gather query
    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_ONLY, storage = storage.SHARDED)
    public Set<StateMachine> findByNameAndVersion(ShardId shardId, String stateMachineName, Long version) {
        Criteria criteria = currentSession().createCriteria(StateMachine.class)
                .add(Restrictions.eq("name", stateMachineName))
                .add(Restrictions.eq("version", version));
        List<StateMachine> stateMachines = criteria.list();
        return new HashSet<>(stateMachines);
    }


    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = storage.SHARDED)
    public void updateStatus(String stateMachineId, StateMachineStatus status) {
        Query query = currentSession().createQuery("update StateMachine set status = :status where id = :stateMachineId");
        query.setString("status", status != null ? status.toString() : null);
        query.setString("stateMachineId", stateMachineId);
        query.executeUpdate();
    }

}
