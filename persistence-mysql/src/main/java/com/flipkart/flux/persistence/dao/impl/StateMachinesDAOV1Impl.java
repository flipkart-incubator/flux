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

import java.util.LinkedList;

import javax.inject.Inject;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.persistence.SessionFactoryContext;
import com.flipkart.flux.persistence.criteria.FSMNameCriteria;
import com.flipkart.flux.persistence.dao.iface.StateMachinesDAOV1;
import com.flipkart.flux.persistence.dto.Field;
import com.flipkart.flux.persistence.dto.StateMachineUpdate;
import com.flipkart.flux.persistence.dto.StateMachineUpdate.StatusUpdate;
import com.flipkart.flux.persistence.dto.StateUpdate.StateUpdateField;
import com.flipkart.flux.persistence.key.FSMId;
import com.google.inject.name.Named;

/**
 * A Hibernate implementation of StateMachine DAO
 * @author regu.b
 *
 */

public class StateMachinesDAOV1Impl extends AbstractDAO<StateMachine> implements StateMachinesDAOV1{

    @Inject
    public StateMachinesDAOV1Impl(@Named("fluxSessionFactoriesContext") SessionFactoryContext sessionFactoryContext) {
        super(sessionFactoryContext);
    }

	@Override
	public StateMachine create(StateMachine entity) {
		return super.save(entity);
	}

	@Override
	public void update(StateMachine object) {
		super.update(object);		
	}

	@Override
	public void remove(StateMachine entity) {
		throw new PersistenceException("Operation is not supported!");		
	}

	@Override
	public StateMachine findEntity(Object key) {
		if (key instanceof FSMId) {
			return super.findById(StateMachine.class,((FSMId)key).statemachineId);	
		} 
		throw new PersistenceException("Find StateMachine is not supported for : " + key);
	}

	@Override
	public StateMachine[] findEntities(Object key) {
		if (key instanceof FSMNameCriteria) {
			return this.findByFSMName((FSMNameCriteria)key);
		} 
		throw new PersistenceException("Find StateMachineS is not supported for key : " + key);	
	}

	@Override
	public void updateData(Field field, Object updates) {
		StateUpdateField stateupdateField = (StateUpdateField)field;
		switch(stateupdateField) {
		case status:
			if (updates instanceof StateMachineUpdate.StatusUpdate) {
				this.updateStatus((StateMachineUpdate.StatusUpdate)updates);
				return;
			}
			break;
		case executionVersion:
			if (updates instanceof StateMachineUpdate.ExecutionVersionUpdate) {
				this.updateExecutionVersion((StateMachineUpdate.ExecutionVersionUpdate)updates);
				return;
			}
			break;
		default:
			throw new PersistenceException("Unable to update StateMachine using unsupported Field type : " + field);
		}
		throw new PersistenceException("Unable to update StateMachine using unsupported update object type : " + updates);
		
	}

	@Override
	public long getExecutionVersionForUpdate(FSMId fsmId) {
    	CriteriaBuilder cb = currentSession().getCriteriaBuilder();
    	CriteriaQuery<StateMachine> cq = cb.createQuery(StateMachine.class);
    	Root<StateMachine> root = cq.from(StateMachine.class);
    	Predicate restrictions[] = new Predicate[] {
    		cb.equal(root.get("id"),fsmId.statemachineId),
    	};    	
    	cq.select(root).where(restrictions);     	    	
    	return currentSession().createQuery(cq).setLockMode(LockModeType.PESSIMISTIC_WRITE).getSingleResult().getExecutionVersion();
	}
	
	private StateMachine[] findByFSMName(FSMNameCriteria nameCriteria) {
    	CriteriaBuilder cb = currentSession().getCriteriaBuilder();
    	CriteriaQuery<StateMachine> cq = cb.createQuery(StateMachine.class);
    	Root<StateMachine> root = cq.from(StateMachine.class);
    	LinkedList<Predicate> predicateList = new LinkedList<Predicate>();
    	predicateList.add(cb.equal(root.get("name"), nameCriteria.fsmName));
    	if (nameCriteria.version != null) {
        	predicateList.add(cb.equal(root.get("version"), nameCriteria.version));
    	}
    	cq.select(root).where(predicateList.toArray(new Predicate[0]));
    	return currentSession().createQuery(cq).getResultList().toArray(new StateMachine[0]);
	}
	
	private void updateStatus(StatusUpdate statusUpdate) {
    	CriteriaBuilder cb = currentSession().getCriteriaBuilder();
    	CriteriaUpdate<StateMachine> cu = cb.createCriteriaUpdate(StateMachine.class);
    	Root<StateMachine> root = cu.from(StateMachine.class);
    	cu.set("status", statusUpdate.status);
    	cu.where(cb.equal(root.get("id"), statusUpdate.fsmId.statemachineId));
    	currentSession().createQuery(cu).executeUpdate();
	}
	
    private void updateExecutionVersion(StateMachineUpdate.ExecutionVersionUpdate versionUpdate) {
    	CriteriaBuilder cb = currentSession().getCriteriaBuilder();
    	CriteriaUpdate<StateMachine> cu = cb.createCriteriaUpdate(StateMachine.class);
    	Root<StateMachine> root = cu.from(StateMachine.class);
    	cu.set("executionVersion", versionUpdate.version);
    	cu.where(cb.equal(root.get("id"), versionUpdate.fsmId.statemachineId));
    	currentSession().createQuery(cu).executeUpdate();
    }
	
	
}
