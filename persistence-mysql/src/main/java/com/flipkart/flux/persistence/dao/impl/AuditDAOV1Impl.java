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

import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.flipkart.flux.domain.AuditRecord;
import com.flipkart.flux.persistence.SessionFactoryContext;
import com.flipkart.flux.persistence.dao.iface.AuditDAOV1;
import com.flipkart.flux.persistence.key.EntityId;
import com.flipkart.flux.persistence.key.FSMId;
import com.google.inject.name.Named;

/**
 * A Hibernate implementation of Audit DAO
 * @author regu.b
 *
 */
public class AuditDAOV1Impl extends AbstractDAO<AuditRecord> implements AuditDAOV1{

    @Inject
    public AuditDAOV1Impl(@Named("fluxSessionFactoriesContext") SessionFactoryContext sessionFactoryContext) {
        super(sessionFactoryContext);
    }
	
	@Override
	public AuditRecord create(AuditRecord auditRecord) {
        if (auditRecord.getErrors() != null && auditRecord.getErrors().toCharArray().length > 999){
            // As in db we are storing the column as varchar(1000)
            auditRecord.setErrors(auditRecord.getErrors().substring(0, ERROR_MSG_LENGTH_IN_AUDIT));
        }
        return super.save(auditRecord);		
	}

	@Override
	public AuditRecord findEntity(Object key) {
		if (!(key instanceof EntityId)) {
			throw new PersistenceException("Find AuditRecord is not supported for key : " + key);
		}
		return super.findById(AuditRecord.class, ((EntityId)key).entityId);
	}

	@Override
	public void remove(AuditRecord entity) {
		throw new PersistenceException("Operation is not supported!");		
	}

	@Override
	public AuditRecord[] findEntities(Object key) {
		if (key instanceof FSMId) {
			return this.findAuditRecordsForFSMId((FSMId)key);
		} 
		throw new PersistenceException("Find AuditRecordS is not supported for key : " + key);		
	}

	private AuditRecord[] findAuditRecordsForFSMId(FSMId key) {
    	CriteriaBuilder cb = currentSession().getCriteriaBuilder();
    	CriteriaQuery<AuditRecord> cq = cb.createQuery(AuditRecord.class);
    	Root<AuditRecord> root = cq.from(AuditRecord.class);
    	cq.select(root).where(cb.equal(root.get("stateMachineInstanceId"), key.statemachineId));     	
    	return currentSession().createQuery(cq).getResultList().toArray(new AuditRecord[0]);
	}

}
