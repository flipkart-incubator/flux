/*
 * Copyright 2012-2019, the original author or authors.
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

package com.flipkart.flux.persistence.dao.impl;

import com.flipkart.flux.domain.StateTraversalPath;
import com.flipkart.flux.persistence.*;
import com.flipkart.flux.persistence.dao.iface.StateTraversalPathDAO;
import com.google.inject.name.Named;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * <code>StateTraversalPathDAOImpl</code> is an implementation of {@link StateTraversalPathDAO}
 * which uses Hibernate to perform operations.
 *
 * @author akif.khan
 */
public class StateTraversalPathDAOImpl extends AbstractDAO<StateTraversalPath> implements StateTraversalPathDAO {

    @Inject
    public StateTraversalPathDAOImpl(@Named("fluxSessionFactoriesContext") SessionFactoryContext sessionFactoryContext) {
        super(sessionFactoryContext);
    }


    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public StateTraversalPath create(String stateMachineId, StateTraversalPath stateTraversalPath) {
        return super.save(stateTraversalPath);
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public Optional<StateTraversalPath> findById(String stateMachineId, Long stateId) {
        Criteria criteria = currentSession().createCriteria(StateTraversalPath.class)
                .add(Restrictions.eq("stateMachineId", stateMachineId))
                .add(Restrictions.eq("stateId", stateId));
        Object object = criteria.uniqueResult();
        StateTraversalPath castedObject = null;
        if(object != null)
            castedObject = (StateTraversalPath) object;

        return Optional.ofNullable(castedObject);
    }

    @SuppressWarnings("unchecked")
	@Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public List<StateTraversalPath> findByStateMachineId(String stateMachineId) {
        Criteria criteria = currentSession().createCriteria(StateTraversalPath.class)
                .add(Restrictions.eq("stateMachineId", stateMachineId));
        Object object = criteria.list();
        List<StateTraversalPath> castedObject;

        if(object != null)
            castedObject = (List<StateTraversalPath>) object;
        else
            castedObject = Collections.emptyList();

        return castedObject;
    }
}