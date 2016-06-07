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

import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.Task;
import com.flipkart.flux.impl.message.TaskAndEvents;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * <code>AkkaGatewayTask</code> is an Akka {@link UntypedActor} that receives and processes messages for {@link Task} instances. This Actor is at the root of the Actor hierarchy
 * for task execution in a Flux node. This Actor creates an instance of {@link AkkaTaskSupervisor} for every incoming request. 
 * 
 * @author regunath.balasubramanian
 *
 */
public class AkkaGatewayTask extends UntypedActor {

	/** Logger instance for this class*/
    private LoggingAdapter logger = Logging.getLogger(getContext().system(), this);
    
    /**
	 * The Akka Actor callback method for processing the Task
	 * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
	 */
	public void onReceive(Object message) throws Exception {
		if (TaskAndEvents.class.isAssignableFrom(message.getClass())) {
			TaskAndEvents taskAndEvent = (TaskAndEvents)message;
			final Props supervisorProps = AkkaTaskSupervisor.getTaskSupervisorProps(taskAndEvent.getTaskIdentifier(), taskAndEvent.getRetryCount());
			final ActorRef taskSupervisor = getContext().system().actorOf(supervisorProps, taskAndEvent.getTaskIdentifier()+"Supervisor");
			taskSupervisor.tell(taskAndEvent, getSelf());
		} else if (Event.class.isAssignableFrom(message.getClass())) {
			getContext().parent().tell(message, getSelf()); // relay the Event output from Task execution to the caller
			getContext().stop(getSender()); // Stop the supervisor actor and its children 
		} else {
			logger.error("Task received a message that it cannot process. Only com.flipkart.flux.impl.message.TaskAndEvents is supported. Message type received is : {}", message.getClass().getName());
			unhandled(message);
		}
	}
	
}
