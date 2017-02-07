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

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.flipkart.flux.api.core.Hook;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.impl.message.HookAndEvents;
import com.netflix.hystrix.HystrixCommand;

import java.util.concurrent.Future;

/**
 * <code>AkkaHook</code> is an Akka {@link UntypedActor} that executes {@link Hook} instances concurrently. HookS are executed using a {@link HookExecutor} where 
 * the execution of {@link Hook#execute(Event[])} is wrapped with a {@link HystrixCommand} to provide isolation and fault tolerance to the Flux runtime. 
 *  
 * @author regunath.balasubramanian
 */

public class AkkaHook extends UntypedActor {

	/** Logger instance for this class*/
    private LoggingAdapter logger = Logging.getLogger(getContext().system(), this);
    
	/**
	 * The Akka Actor callback method for processing the Hook
	 * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
	 */
	public void onReceive(Object message) throws Exception {
		if (HookAndEvents.class.isAssignableFrom(message.getClass())) {
			@SuppressWarnings("unused")
			Future<HookExecutor.STATUS> hookResult = new HookExecutor(((HookAndEvents)message).getHook(), ((HookAndEvents)message).getEvents()).queue();
			// we don't wait on the Future and just return the status that Hook execution was scheduled.
			getSender().tell(HookExecutor.STATUS.EXECUTION_SCHEDULED, getSelf());
		} else {
			logger.error("Hook received a message that it cannot process. Only com.flipkart.flux.impl.message.HookAndEvents is supported. Message type received is : " + message.getClass().getName());
			unhandled(message);
		}
	}

}
