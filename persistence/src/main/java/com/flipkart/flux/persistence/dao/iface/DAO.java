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
package com.flipkart.flux.persistence.dao.iface;

/**
 * A generic interface providing persistence operations on specific Entity types
 * @author regu.b
 *
 * @param <T> the Entity type that is persisted
 */
public interface DAO <T> {
	
	/**
	 * Creates the specified entity in the underlying data store
	 * @param entity the Entity to persist
	 * @return the persisted Entity
	 */
	public T create(T entity);
	
	/**
	 * Updates the specified entity in the underlying data store
	 * @param object the Entity to update
	 */
	public void update(T object);
	
	/**
	 * Removes the specified entity from the underlying data store
	 * @param entity the Entity to renove
	 */
	public void remove(T entity);
	
	/**
	 * Retrieves the specified entity from the underlying data store using the specified key/identifier 
	 * @param key the Entity retrieval key
	 * @return the retrieved Entity
	 */
	public T findEntity (Object key);
	
	/**
	 * Retrieves entities of the specfied type from the underlying data store using the specified key/identifier  
	 * @param key the entities retrieval key
	 * @return the retrieved entities
	 */
	public T[] findEntities (Object key);
	
	/**
	 * Returns the type of the enity persisted by this DAO
	 * @return Type of the entity persisted by this DAO
	 */
	@SuppressWarnings("rawtypes")
	public Class getPersistedEntityType();
	
}
