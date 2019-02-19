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

import com.flipkart.flux.dao.iface.StateMetaDataDAO;
import com.flipkart.flux.domain.StateMetaData;
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
 * <code>StateMetaDataDAOImpl</code> is an implementation of {@link StateMetaDataDAO} which uses Hibernate to perform operations.
 *
 * @author akif.khan
 */
public class StateMetaDataDAOImpl extends AbstractDAO<StateMetaData> implements StateMetaDataDAO {


    @Inject
    public StateMetaDataDAOImpl(@Named("fluxSessionFactoriesContext") SessionFactoryContext sessionFactoryContext) {
        super(sessionFactoryContext);
    }

    /**
     * @param stateMachineId
     * @param id
     * @return StateMetaData
     */
    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public StateMetaData findById(String stateMachineId, Long id) {
        return super.findByCompositeIdFromStateTable(StateMetaData.class, stateMachineId ,id);
    }
}
