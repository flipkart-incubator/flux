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

import com.flipkart.flux.dao.iface.CheckpointsDAO;
import com.flipkart.flux.domain.Checkpoint;
import com.flipkart.flux.util.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import java.util.List;

/**
 * @author shyam.akirala
 */
public class CheckpointsDAOImpl extends AbstractDAO<Checkpoint> implements CheckpointsDAO {

    @Override
    public Long create(Checkpoint checkpoint) {
        return super.save(checkpoint).getId();
    }

    @Override
    public Checkpoint find(Long id) {
        return super.findById(Checkpoint.class, id);
    }

    @Override
    public Checkpoint find(String stateMachineInstanceId, Long stateId) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();

        Criteria criteria = session.createCriteria(Checkpoint.class)
                .add(Restrictions.eq("stateMachineInstanceId", stateMachineInstanceId))
                .add(Restrictions.eq("stateId", stateId));
        List<Checkpoint> checkpointList = criteria.list();
        tx.commit();

        //The above criteria should return only one record ideally
        Checkpoint checkpoint = null;
        if(checkpointList != null && checkpointList.size() > 0) {
            checkpoint = checkpointList.get(0);
        }
        return checkpoint;
    }
}
