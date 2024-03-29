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

import java.lang.reflect.ParameterizedType;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.flipkart.flux.persistence.SessionFactoryContext;
import com.flipkart.flux.persistence.dao.iface.DAO;

/**
 * <code>AbstractDAO</code> class provides methods to perform CRUD operations on an object using Hibernate.
 *
 * @author shyam.akirala
 */
public abstract class AbstractDAO<T> {

    private SessionFactoryContext sessionFactoryContext;

    public AbstractDAO(SessionFactoryContext sessionFactoryContext) {
        this.sessionFactoryContext = sessionFactoryContext;
    }

    /**
     * Provides the session which is bound to current thread.
     *
     * @return Session
     */
    public Session currentSession() {
        return sessionFactoryContext.getThreadLocalSession();
    }

    /**
     * Retrieves object by it's unique identifier.
     *
     * @param cls - Class type of the object
     * @param id
     * @return (T) Object
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public T findById(Class cls, Long id) {
        Criteria criteria = currentSession().createCriteria(cls).add(Restrictions.eq("id", id));
        Object object = criteria.uniqueResult();
        T castedObject = null;
        if (object != null) {
            castedObject = (T) object;
        }
        return castedObject;
    }

    /**
     * Retrieves object by it's unique identifier.
     *
     * @param cls - Class type of the object
     * @param id
     * @return (T) Object
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public T findById(Class cls, String id) {
        Criteria criteria = currentSession().createCriteria(cls).add(Restrictions.eq("id", id));
        Object object = criteria.uniqueResult();
        T castedObject = null;
        if (object != null) {
            castedObject = (T) object;
        }
        return castedObject;
    }

    /**
     * Retrieves object by it's unique identifier.
     *
     * @param cls  - Class type of the object
     * @param id   - stateId
     * @param smId - stateMachineId
     * @return (T) Object
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public T findByCompositeIdFromStateTable(Class cls, String smId, Long id) {
        Criteria criteria = currentSession().createCriteria(cls)
                .add(Restrictions.eq("stateMachineId", smId))
                .add(Restrictions.eq("id", id));
        Object object = criteria.uniqueResult();
        T castedObject = null;
        if (object != null) {
            castedObject = (T) object;
        }
        return castedObject;
    }

    /**
     * Saves the object in DB and returns the saved object.
     *
     * @param object
     * @return saved object
     */
    public T save(T object) {
        currentSession().save(object);
        return object;
    }

    /**
     * Updates the object
     *
     * @param object
     */
    public void update(T object) {
        currentSession().update(object);
    }

    /**
     * Saves object in DB, and returns the saved object.
     * If the object already exists, updates it.
     *
     * @param object
     * @return saved object
     */
    public T saveOrUpdate(T object) {
        currentSession().saveOrUpdate(object);
        return object;
    }

    /**
     * Deletes the object from DB.
     *
     * @param object
     */
    public void delete(T object) {
        currentSession().delete(object);
    }
    
    /**
     * Implementation for {@link DAO}{@link #getPersistedEntityType()}
     * @return
     */
	@SuppressWarnings("rawtypes")
	public Class getPersistedEntityType() {
		return ((Class)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
	}    
    
}
