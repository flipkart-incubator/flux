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

package com.flipkart.flux.impl;

import java.util.HashMap;
import java.util.Map;

import com.flipkart.flux.domain.Context;

/**
 * <code>RAMContext</code> is a sub-type of {@link Context} that uses main memory as storage.
 * Data stored in this context is transient. This context is therefore only suitable for testing and not
 * production use.
 * 
 * @author regunath.balasubramanian
 *
 */

public class RAMContext<T> extends Context<T> {

	/** Map for storing the context data in memory*/
	private Map<String,Object> contextDataMap = new HashMap<String, Object>();
	
	/** Constructor */
	public RAMContext(Long startTime, String contextId) {
		this.startTime = startTime;
		this.contextId = contextId;
	}
	
	/**
	 * Abstract method implementation. Stores the data in JVM heap
	 * @see com.flipkart.flux.domain.Context#storeData(java.lang.String, java.lang.Object)
	 */
	public void storeData(String key, Object data) {
		this.contextDataMap.put(key, data);
	}

	/**
	 * Abstract method implementation. Retrieves the context data for the specified key from
	 * JVM heap
	 * @see com.flipkart.flux.domain.Context#retrieve(java.lang.String)
	 */
	public Object retrieve(String key) {
		return this.contextDataMap.get(key);
	}

}
