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

import com.flipkart.flux.dao.iface.AuditDAO;
import com.flipkart.flux.domain.AuditRecord;
import com.flipkart.flux.util.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import java.util.List;

/**
 * @author shyam.akirala
 */
public class AuditDAOImpl extends AbstractDAO<AuditRecord> implements AuditDAO {

    @Override
    public List<AuditRecord> find(String stateMachineInstanceId) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();

        Criteria criteria = session.createCriteria(AuditRecord.class).add(Restrictions.eq("stateMachineInstanceId", stateMachineInstanceId));
        List<AuditRecord> records = criteria.list();

        tx.commit();
        return records;
    }

    @Override
    public AuditRecord find(String stateMachineInstanceId, Long stateId, int retryAttempt) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();

        Criteria criteria = session.createCriteria(AuditRecord.class)
                .add(Restrictions.eq("stateMachineInstanceId", stateMachineInstanceId))
                .add(Restrictions.eq("stateId", stateId))
                .add(Restrictions.eq("retryAttempt", retryAttempt));
        //assuming only one record matches the above criteria
        Object record = criteria.uniqueResult();
        tx.commit();

        AuditRecord auditRecord = null;
        if(record !=null) {
            auditRecord = (AuditRecord) record;
        }
        return auditRecord;
    }


    @Override
    public AuditRecord create(AuditRecord auditRecord) {
        return super.save(auditRecord);
    }

    @Override
    public void update(AuditRecord auditRecord) {
        super.update(auditRecord);
    }
}
