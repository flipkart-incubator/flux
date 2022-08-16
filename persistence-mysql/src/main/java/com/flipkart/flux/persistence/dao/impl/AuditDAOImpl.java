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

package com.flipkart.flux.persistence.dao.impl;

import com.flipkart.flux.domain.AuditRecord;
import com.flipkart.flux.persistence.*;
import com.flipkart.flux.persistence.dao.iface.AuditDAO;
import com.google.inject.name.Named;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

/**
 * <code>AuditDAOImpl</code> is an implementation of {@link AuditDAO} which uses Hibernate to perform operations.
 * @author shyam.akirala
 */
public class AuditDAOImpl extends AbstractDAO<AuditRecord> implements AuditDAO {
	
	private boolean enableAuditRecord;
	
    @Inject
    public AuditDAOImpl(@Named("fluxSessionFactoriesContext") SessionFactoryContext sessionFactoryContext, @Named("enableAuditRecord") boolean enableAuditRecord) {
        super(sessionFactoryContext);
        this.enableAuditRecord = enableAuditRecord;
    }

    @SuppressWarnings("unchecked")
	@Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public List<AuditRecord> findBySMInstanceId(String stateMachineInstanceId) {
        Criteria criteria = currentSession().createCriteria(AuditRecord.class).add(Restrictions.eq("stateMachineInstanceId", stateMachineInstanceId));
        List<AuditRecord> records = criteria.list();
        return records;
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public AuditRecord create(String stateMachineId, AuditRecord auditRecord) {
    	if(!enableAuditRecord) {
    		return null;
    	}
    	
        if (auditRecord.getErrors() != null && auditRecord.getErrors().toCharArray().length > 999){
            // As in db we are storing the column as varchar(1000)
            auditRecord.setErrors(auditRecord.getErrors().substring(0, ERROR_MSG_LENGTH_IN_AUDIT));
        }
        return super.save(auditRecord);
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public AuditRecord findById(String stateMachineId, Long id) {
        return super.findById(AuditRecord.class, id);
    }

    @Override
    public AuditRecord create_NonTransactional(AuditRecord auditRecord, Session session) {
    	if(!enableAuditRecord) {
    		return null;
    	}
    	
        if (auditRecord.getErrors() != null && auditRecord.getErrors().toCharArray().length > 999){
            // As in db we are storing the column as varchar(1000)
            auditRecord.setErrors(auditRecord.getErrors().substring(0, ERROR_MSG_LENGTH_IN_AUDIT));
        }
        session.save(auditRecord);
        return auditRecord;
    }
    
    public void enableAuditRecord(boolean value) {
  	  this.enableAuditRecord=value;
    }
}