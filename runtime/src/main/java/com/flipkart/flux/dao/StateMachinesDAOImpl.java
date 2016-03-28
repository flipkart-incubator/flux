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
import com.flipkart.flux.util.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import java.util.List;

/**
 * @author shyam.akirala
 */
public class StateMachinesDAOImpl extends AbstractDAO<StateMachine> implements StateMachinesDAO {

    @Override
    public Long create(StateMachine stateMachine) {
        return super.persist(stateMachine).getId();
    }

    @Override
    public StateMachine findById(Long id) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();

        Criteria criteria = session.createCriteria(StateMachine.class).add(Restrictions.eq("id", id));
        Object object = criteria.uniqueResult();
        StateMachine stateMachine = null;
        if(object != null)
            stateMachine = (StateMachine) object;

        tx.commit();
        return stateMachine;
    }

    @Override
    public List<StateMachine> findByName(String stateMachineName) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();

        Criteria criteria = session.createCriteria(StateMachine.class)
                .add(Restrictions.eq("name", stateMachineName));
        List<StateMachine> stateMachines = criteria.list();

        tx.commit();
        return stateMachines;
    }

    @Override
    public List<StateMachine> findByNameAndVersion(String stateMachineName, Long version) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();

        Criteria criteria = session.createCriteria(StateMachine.class)
                .add(Restrictions.eq("name", stateMachineName))
                .add(Restrictions.eq("version", version));
        List<StateMachine> stateMachines = criteria.list();

        tx.commit();
        return stateMachines;
    }
}
