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
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.flipkart.flux.domain.StateTraversalPath;
import com.flipkart.flux.persistence.SessionFactoryContext;
import com.flipkart.flux.persistence.dao.iface.StateTraversalPathDAOV1;
import com.flipkart.flux.persistence.key.FSMId;
import com.flipkart.flux.persistence.key.FSMIdEntityId;
import com.google.inject.name.Named;

/**
 * A Hibernate implementation of StateTraversalPath DAO
 * @author regu.b
 *
 */
public class StateTraversalPathDAOV1Impl extends AbstractDAO<StateTraversalPath> implements StateTraversalPathDAOV1 {

    @Inject
    public StateTraversalPathDAOV1Impl(@Named("fluxSessionFactoriesContext") SessionFactoryContext sessionFactoryContext) {
        super(sessionFactoryContext);
    }

	@Override
	public StateTraversalPath create(StateTraversalPath entity) {
		return super.save(entity);
	}

	@Override
	public void update(StateTraversalPath object) {
		super.update(object);		
	}

	@Override
	public void remove(StateTraversalPath entity) {
		throw new PersistenceException("Operation is not supported!");		
	}

	@Override
	public StateTraversalPath findEntity(Object key) {
		if (key instanceof FSMIdEntityId) {
			return this.findByCompositeId((FSMIdEntityId)key);	
		}
		throw new PersistenceException("Find StateTraversalPath is not supported for : " + key);	
	}

	@Override
	public StateTraversalPath[] findEntities(Object key) {
		if (key instanceof FSMId) {
			return this.findStateTraversalPathsByFSMId((FSMId)key);
		} 
		throw new PersistenceException("Find StateTraversalPathS is not supported for key : " + key);	
	}
	
	private StateTraversalPath findByCompositeId(FSMIdEntityId fsmIdEntityId) {
    	CriteriaBuilder cb = currentSession().getCriteriaBuilder();
    	CriteriaQuery<StateTraversalPath> cq = cb.createQuery(StateTraversalPath.class);
    	Root<StateTraversalPath> root = cq.from(StateTraversalPath.class);
    	Predicate restrictions[] = new Predicate[] {
			cb.equal(root.get("stateId"), fsmIdEntityId.entityId.entityId),
			cb.equal(root.get("stateMachineId"), fsmIdEntityId.fsmId.statemachineId),
    	};    	
    	cq.select(root).where(restrictions);     	
    	return currentSession().createQuery(cq).getSingleResult();
    }
	
	private StateTraversalPath[] findStateTraversalPathsByFSMId(FSMId fsmId) {
    	CriteriaBuilder cb = currentSession().getCriteriaBuilder();
    	CriteriaQuery<StateTraversalPath> cq = cb.createQuery(StateTraversalPath.class);
    	Root<StateTraversalPath> root = cq.from(StateTraversalPath.class);
    	cq.select(root).where(cb.equal(root.get("stateMachineId"), fsmId.statemachineId));     	
    	return currentSession().createQuery(cq).getResultList().toArray(new StateTraversalPath[0]);
	}
	
}
