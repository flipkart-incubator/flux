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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.FluxError;
import com.flipkart.flux.domain.Task;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixCommandProperties.ExecutionIsolationStrategy;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;

import javafx.util.Pair;

/**
 * <code>TaskExecutor</code> wraps {@link Task} execution with Hystrix.
 * @author regunath.balasubramanian
 *
 */
public class TaskExecutor extends HystrixCommand<Event> {

	public static final String MANAGED_RUNTIME = "managedRuntime";
	/** The task to execute*/
	private Task task;
	
	/** The events used in Task execution*/
	private Event[] events;
	private final Long stateMachineId;
	private final String outputeEventName;
	// TODO - use a singleton instead
	private static final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * Constructor for this class
	 * @param task the task to execute
	 * @param fluxRuntimeConnector
	 * @param stateMachineId
	 */
	public TaskExecutor(AbstractTask task, Event[] events, Long stateMachineId,String outputeEventName) {
        super(Setter
        		.withGroupKey(HystrixCommandGroupKey.Factory.asKey(task.getTaskGroupName()))
                .andCommandKey(HystrixCommandKey.Factory.asKey(task.getName()))
                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(task.getName() + "-TP")) // creating a new thread pool per task by appending "-TP" to the task name
                .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter().withCoreSize(task.getExecutionConcurrency()))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                		.withExecutionIsolationStrategy(ExecutionIsolationStrategy.THREAD)
                		.withExecutionTimeoutInMilliseconds(task.getExecutionTimeout())));
		this.task = task;
		this.events = events;
		this.stateMachineId = stateMachineId;
		this.outputeEventName = outputeEventName;
	}
	
	/**
	 * The HystrixCommand run method. Executes the Task and returns the result or throws an exception in case of a {@link FluxError}
	 * @see com.netflix.hystrix.HystrixCommand#run()
	 */
	protected Event run() throws Exception {
		Pair<Object,FluxError> result = this.task.execute(events);
		if (result.getValue() != null) {
			throw result.getValue();
		}
		final Object returnObject = result.getKey();
		if (returnObject != null) {
			return new Event(outputeEventName, returnObject.getClass().getCanonicalName(), Event.EventStatus.triggered, stateMachineId, objectMapper.writeValueAsString(returnObject), MANAGED_RUNTIME);
		}
		return null;
	}
	
}
