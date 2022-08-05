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

import javax.persistence.PersistenceException;
import javax.transaction.Transactional;

import com.flipkart.flux.persistence.PersistenceConstants.Operation;
import com.flipkart.flux.persistence.dao.iface.DAO;
import com.flipkart.flux.shard.ShardedEntity;
import com.flipkart.flux.utils.Pair;

/**
 * A persistence manager for working with multiple entity types, usually within a single transaction scope. 
 * @author regu.b
 *
 */
public abstract class MultiEntityManager {
	
	@SuppressWarnings("rawtypes")
	private List<DAO> daos;

	/** Constructor */
	public MultiEntityManager() {
	}

	@SuppressWarnings("rawtypes")
	public List<DAO> getDaos() {
		return daos;
	}
	@SuppressWarnings("rawtypes")
	public void setDaos(List<DAO> daos) {
		this.daos = daos;
	}
	
	/**
	 * Creates the specified entities. Throws an exception for unsupported entities or when entities belonging to different shards are persisted.
	 * @param entities the domain entities to persist
	 * @return the persisted entities
	 */
	@Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
	public Object[] create(Object[] entities) {
    	this.checkPersistenceContraints(entities);
		return this.createEntities(entities);
	}

	/**
	 * Updates the specified entities. Throws an exception for unsupported entities or when entities belonging to different shards are persisted.
	 * @param entities the domain entities to update
	 * @return the persisted entities
	 */
	@Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
	public Object[] update(Object[] entities) {
    	this.checkPersistenceContraints(entities);
		return this.updateEntities(entities);
	}
	
	/**
	 * Creates and updates the respective specified set of entities.Throws an exception for unsupported entities or when entities belonging to different shards are persisted.
	 * @param createEntities the domain entities to persist
	 * @param updateEntities the domain entities to update
	 * @return the persisted entities
	 */
	@Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public Pair<PersistenceConstants.Operation, Object[]>[] createAndUpdate(Object[] createEntities, Object[] updateEntities) {		
		Object[] allInputEntities = new Object[createEntities.length + updateEntities.length];
		System.arraycopy(createEntities, 0, allInputEntities, 0, createEntities.length);
		System.arraycopy(updateEntities, 0, allInputEntities, createEntities.length, updateEntities.length);
		this.checkPersistenceContraints(allInputEntities);
		Pair[] results = {
				new Pair<PersistenceConstants.Operation, Object[]>(Operation.Create,this.createEntities(createEntities)),
				new Pair<PersistenceConstants.Operation, Object[]>(Operation.Update,this.updateEntities(updateEntities)),
		};
		return results;
	}
	
	/**
	 * Creates the entities using the respective DAO. 
	 */
    @SuppressWarnings("unchecked")
	private Object[] createEntities(Object[] entities) {
    	List<Object> persistedEntities = new LinkedList<Object>();
    	for (Object entity : entities) {
    		persistedEntities.add(
    				daos.stream().filter(dao -> entity.getClass().isAssignableFrom(dao.getPersistedEntityType())) 
    					.findFirst().get().create(entity));
    	}
    	return (Object[])persistedEntities.toArray(new Object[0]);	
	}
    
	/**
	 * Updates the entities using the respective DAO. 
	 */
    @SuppressWarnings("unchecked")
    private Object[] updateEntities(Object[] entities) {
    	List<Object> persistedEntities = new LinkedList<Object>();
    	for (Object entity : entities) {
    		daos.stream().filter(dao -> entity.getClass().isAssignableFrom(dao.getPersistedEntityType())) 
    			.findFirst().get().update(entity);
    		persistedEntities.add(entity);
    	}
    	return (Object[])persistedEntities.toArray(new Object[0]);    	
    }
    

    /**
     * Checks the specified entities for persistence constraints - such as requiring all entities to belong to a single shard for ensuring transaction
     * guarantees, entities that have DAOs available for persistence.
     */
    private void checkPersistenceContraints(Object[] entities) {
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
    		if (!daos.stream().anyMatch(dao -> entity.getClass().isAssignableFrom(dao.getPersistedEntityType()))) {
	    		throw new PersistenceException("Unable to persist unsupported entity type : " + entity.getClass().getName());
    		}
    	}    	
    }
	
}
