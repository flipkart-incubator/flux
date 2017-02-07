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

import com.flipkart.flux.api.EventData;
import com.flipkart.flux.api.core.Hook;
import com.netflix.hystrix.*;
import com.netflix.hystrix.HystrixCommandProperties.ExecutionIsolationStrategy;

/**
 * <code>HookExecutor</code> wraps {@link Hook} execution with Hystrix.
 * @author regunath.balasubramanian
 *
 */
public class HookExecutor extends HystrixCommand<HookExecutor.STATUS> {
	
	/** Status of execution */
	public static enum STATUS {
		EXECUTION_SCHEDULED;
	}
	
	/** The Hook to execute*/
	private AbstractHook hook;
	
	/** The events used in Hook execution*/
	private EventData[] events;

	/**
	 * Constructor for this class
	 * @param hook the Hook to execute
	 */
	public HookExecutor(AbstractHook hook, EventData[] events) {
        super(Setter
        		.withGroupKey(HystrixCommandGroupKey.Factory.asKey(hook.getHookGroupName()))
                .andCommandKey(HystrixCommandKey.Factory.asKey(hook.getName()))
                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(hook.getName() + "-TP")) // creating a new thread pool per hook by appending "-TP" to the hook name
                .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter().withCoreSize(hook.getExecutionConcurrency()))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                		.withExecutionIsolationStrategy(ExecutionIsolationStrategy.THREAD)
                		.withExecutionTimeoutInMilliseconds(hook.getExecutionTimeout())));
		this.hook = hook;
	}
	
	/**
	 * The HystrixCommand run method. Executes the Hook.
	 * @see com.netflix.hystrix.HystrixCommand#run()
	 */
	protected HookExecutor.STATUS run() throws Exception {
		this.hook.execute(events);
		return HookExecutor.STATUS.EXECUTION_SCHEDULED; // Hook execution response is not tracked or used. So just return a status to indicate execution was scheduled.
	}
	
}
