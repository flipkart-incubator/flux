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

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;

import com.flipkart.flux.persistence.dao.iface.DAO;
import com.flipkart.flux.shard.ShardedEntity;

/**
 * A JPA inspired EntityManager for managing persistence operations on entities. Demarcates transaction boundaries for the data operations.
 * 
 * @author regu.b
 *
 */
public abstract class EntityManager<T> {

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
	 * Creates the specified entities. Throws an exception for unsupported entities or when entities belonging to different shards are persisted.
	 * @param entities the domain entities to persist
	 * @return the persisted entities
	 */
	@Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    @SuppressWarnings("unchecked")
	public Object[] create(Object[] entities) {
    	List<Object> persistedEntities = new LinkedList<Object>();
    	ShardedEntity uniqueShard = null;    	
    	for (Object entity : entities) {
    		if (ShardedEntity.class.isAssignableFrom(entity.getClass())) {
    			ShardedEntity newShard = (ShardedEntity)entity;
    			if (uniqueShard == null) {
    				uniqueShard = newShard;
    			} else if (!uniqueShard.equals(newShard)) {
    				throw new PersistenceException("Attempt to persist entities belonging to two different shards : " + uniqueShard.toString() + "," + newShard.toString());
    			}
    		}
    		if (entity.getClass().isAssignableFrom(dao.getPersistedEntityType())) {
	    		throw new PersistenceException("Unable to persist unsupported entity type : " + entity.getClass().getName());
    		}
    	}
    	for (Object entity : entities) {
    		persistedEntities.add(dao.create((T)entity));
    	}
    	return (Object[])persistedEntities.toArray(new Object[0]);
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
