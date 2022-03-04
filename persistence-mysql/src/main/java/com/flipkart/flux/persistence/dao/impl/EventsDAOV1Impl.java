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
import javax.persistence.PersistenceException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.flipkart.flux.domain.Event;
import com.flipkart.flux.persistence.SessionFactoryContext;
import com.flipkart.flux.persistence.criteria.EventStatusCriteria;
import com.flipkart.flux.persistence.dao.iface.EventsDAOV1;
import com.flipkart.flux.persistence.key.EntityId;
import com.google.inject.name.Named;

/**
 * A Hibernate implementation of Events DAO
 * @author regu.b
 *
 */
public class EventsDAOV1Impl extends AbstractDAO<Event> implements EventsDAOV1 {

    @Inject
    public EventsDAOV1Impl(@Named("fluxSessionFactoriesContext") SessionFactoryContext sessionFactoryContext) {
        super(sessionFactoryContext);
    }

	@Override
	public Event create(Event entity) {
		return super.save(entity);
	}

	@Override
	public void update(Event object) {
		super.update(object);		
	}

	@Override
	public void remove(Event entity) {
		throw new PersistenceException("Operation is not supported!");		
	}

	@Override
	public Event findEntity(Object key) {
		if (key instanceof EntityId) {
			return super.findById(Event.class,((EntityId)key).entityId);	
		}
		throw new PersistenceException("Find Event is not supported for : " + key);	
	}

	@Override
	public Event[] findEntities(Object key) {
		if (key instanceof EventStatusCriteria) {
			return this.findEventsByStatusCriteria((EventStatusCriteria)key);
		} 
		throw new PersistenceException("Find EventS is not supported for key : " + key);	
	}
	
	private Event[] findEventsByStatusCriteria(EventStatusCriteria statusCritera) {
    	CriteriaBuilder cb = currentSession().getCriteriaBuilder();
    	CriteriaQuery<Event> cq = cb.createQuery(Event.class);
    	Root<Event> root = cq.from(Event.class);
    	LinkedList<Predicate> predicateList = new LinkedList<Predicate>();
    	predicateList.add(cb.equal(root.get("stateMachineInstanceId"), statusCritera.fsmId.statemachineId));
    	if (statusCritera.ignoreStatus != null) {
        	predicateList.add(cb.notEqual(root.get("status"), statusCritera.ignoreStatus));    		
    	}
    	cq.select(root).where(predicateList.toArray(new Predicate[0]));    	
    	return currentSession().createQuery(cq).getResultList().toArray(new Event[0]);
	}
	
}
