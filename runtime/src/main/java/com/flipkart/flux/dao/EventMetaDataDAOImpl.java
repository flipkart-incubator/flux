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

import com.flipkart.flux.api.EventData;
import com.flipkart.flux.dao.iface.EventMetaDataDAO;
import com.flipkart.flux.domain.EventMetaData;
import com.flipkart.flux.persistence.*;
import com.google.inject.name.Named;
import org.hibernate.*;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.*;

/**
 * <code>EventMetaDataDAOImpl</code> is an implementation of {@link EventMetaDataDAO} which uses Hibernate to perform operations.
 *
 * @author akif.khan
 */
public class EventMetaDataDAOImpl extends AbstractDAO<EventMetaData> implements EventMetaDataDAO {

    @Inject
    public EventMetaDataDAOImpl(@Named("fluxSessionFactoriesContext") SessionFactoryContext sessionFactoryContext) {
        super(sessionFactoryContext);
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public EventMetaData create(String stateMachineInstanceId, EventMetaData eventMetaData) {
        return super.save(eventMetaData);
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public List<EventMetaData> findBySMInstanceId(String stateMachineInstanceId) {
        return currentSession().createCriteria(EventMetaData.class).add(Restrictions.eq("stateMachineId", stateMachineInstanceId)).list();
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public EventMetaData findBySMIdAndName(String stateMachineInstanceId, String eventName) {
        Criteria criteria = currentSession().createCriteria(EventMetaData.class).add(Restrictions.eq("stateMachineId", stateMachineInstanceId))
                .add(Restrictions.eq("name", eventName));
        return (EventMetaData) criteria.uniqueResult();
    }
}
