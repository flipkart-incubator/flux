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

import com.flipkart.flux.api.core.Task;

/**
 * <code>AbstractTask</code> provides behavior common to all {@link Task} implementations such as methods to control concurrency, execution timeouts etc.
 * 
 * @author regunath.balasubramanian
 *
 */
public abstract class AbstractTask implements Task {
	
	/**
	 * Returns the name of this Task. Used mostly for display and human interpretation. This would be of the form className_methodName
	 * @return the name of this Task
	 */
	public abstract String getName();

	/**
	 * Returns the group name that this Task is part of. Usually refers to the name of a deployment unit on Flux.
	 * @return String name of the task group
	 */
	public abstract String getTaskGroupName();
	
	/**
	 * Returns the number of concurrent executions of this Task. Used to limit CPU consumption (via dedicated thread pools) for each Task type.
	 * @return concurrency limit
	 */
	public abstract int getExecutionConcurrency();
	
	/**
	 * Returns the execution timeout for this Task. Control returns to the caller based on this timeout and the outcome of execution from this Task is treated as failure, with
	 * a timeout. This Task may continue to execute and produce results that may not be consumed.
	 * @return execution timeout
	 */
	public abstract int getExecutionTimeout();
	
}
