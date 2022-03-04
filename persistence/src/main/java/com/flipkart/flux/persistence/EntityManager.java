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
package com.flipkart.flux.persistence;

import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;

import com.flipkart.flux.persistence.dao.iface.DAO;

/**
 * A JPA inspired EntityManager for managing persistence operations on entities. Demarcates transaction boundaries for the data operations.
 * 
 * @author regu.b
 *
 */
public class EntityManager<T> {

	/** The DAO instance for performing the persistence operations*/
	private DAO<T> dao;
	
	/** Constructor */
	@Inject
	public EntityManager(DAO<T> dao) {
		this.dao = dao;
	}
	
	/**
	 * Creates the specified entity
	 * @param entity the domain entity to persist
	 * @return the persisted entity
	 */
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)	
	public T create(T entity) {
		return (T)dao.create(entity);
	}
	
	/**
	 * Creates the specified entities
	 * @param entities the domain entities to persist
	 * @return the persisted entities
	 */
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
	public Object[] create(Object[] entities) {
		throw new PersistenceException("Generic bulk creation of entities is not supported!");
	}

	/**
	 * Updates the specified entity
	 * @param object the object to update
	 */
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
	public void update(T object) {
		dao.update(object);
	}
	
	/**
	 * Deletes the specified entity from persistence store
	 * @param entity the entity to delete
	 */
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
	public void remove(T entity) {
		dao.remove(entity);
	}
	
	/**
	 * Retrieves an instance of the specified entity using the specified object key, which may be single valued or a composite of multiple values
	 * @param key Object key that may be singular or composite
	 * @return instance of the entity type
	 */
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
	public T findEntity (Object key) {
		return dao.findEntity(key);
	}

	/**
	 * Retrieves instances of the specified entity using the specified object key, which may be single valued or a composite of multiple values
	 * @param key Object key that may be singular or composite
	 * @return instances of the entity type
	 */
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_ONLY, storage = Storage.SHARDED)
	public T[] findEntitities (Object key) {
		return dao.findEntities(key);
	}
	
    /**
     * Returns the DAO used for persistence operations
     * @return the DAO instance
     */
	protected DAO<T> getDAO() {
		return dao;
	}

}
