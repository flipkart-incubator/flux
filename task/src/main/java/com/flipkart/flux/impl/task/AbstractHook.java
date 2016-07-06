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
package com.flipkart.flux.impl.task;

import com.flipkart.flux.api.core.Hook;

/**
 * <code>AbstractHook</code> provides behavior common to all {@link Hook} implementations such as methods to control concurrency, execution timeouts etc.
 * 
 * @author regunath.balasubramanian
 *
 */
public abstract class AbstractHook implements Hook {
	
	/**
	 * Returns the name of this Hook. Used mostly for display and human interpretation
	 * @return the name of this Hook
	 */
	public abstract String getName();

	/**
	 * Returns the group name that this Hook is part of. Usually refers to the name of a deployment unit on Flux.
	 * @return String name of the hook group
	 */
	public abstract String getHookGroupName();
	
	/**
	 * Returns the number of concurrent executions of this Hook. Used to limit CPU consumption (via dedicated thread pools) for each Hook type.
	 * @return concurrency limit
	 */
	public abstract int getExecutionConcurrency();
	
	/**
	 * Returns the execution timeout for this Hook. Control returns to the caller immediately post invocation of this Hook. 
	 * @return execution timeout
	 */
	public abstract int getExecutionTimeout();
	
}

