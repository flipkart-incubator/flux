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

import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.Task;
import com.netflix.hystrix.HystrixCommand;

import akka.actor.UntypedActor;

/**
 * <code>AkkaTask</code> is an Akka {@link UntypedActor} that executes {@link Task} instances concurrently. Tasks are executed using a {@link TaskExecutor} where 
 * the execution of {@link Task#execute(com.flipkart.flux.domain.Event...)} is wrapped with a {@link HystrixCommand} to provide isolation and fault tolerance to 
 * the Flux runtime. 
 *  
 * @author regunath.balasubramanian
 */

public class AkkaTask extends UntypedActor {

	/** Logger instance for this class*/
    private LoggingAdapter logger = Logging.getLogger(getContext().system(), this);
    
    /** TaskRegistry instance to look up Task instances from */
    // TODO : Need a way to inject/lookup this registry
    private TaskRegistry taskRegistry;
	
	/**
	 * The Akka Actor callback method for processing this Task
	 * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public void onReceive(Object message) throws Exception {
		if (Event[].class.isAssignableFrom(message.getClass())) {
			AbstractTask task = this.taskRegistry.getTaskForEvents((Event<Object>[])message);
			if (task == null) {
				getSender().tell(new TaskExecutor(task, (Event<Object>[])message).execute(), getSelf());
			} else {
				logger.error("Task received EventS that it cannot process. Events received are : " + TaskRegistry.getEventsKey((Event<Object>[])message));				
			}
		} else {
			logger.error("Task received a message that it cannot process. Only com.flipkart.flux.domain.Event[] is supported. Message type received is : " + message.getClass().getName());
			unhandled(message);
		}
	}

}
