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

import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;

import com.flipkart.flux.api.EventData;
import com.flipkart.flux.api.core.Task;
import com.flipkart.flux.client.runtime.FluxRuntimeConnector;
import com.flipkart.flux.domain.Event;
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
    
	/** Counter to help create unique actor names*/
	private static final AtomicLong INSTANCE_COUNTER = new AtomicLong();
    
	/** The Flux Runtime Connector instance for dispatching processed EventS*/
	@Inject
	private static FluxRuntimeConnector fluxRuntimeConnector;
    
    /**
	 * The Akka Actor callback method for processing the Task
	 * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
	 */
	public void onReceive(Object message) throws Exception {
		if (TaskAndEvents.class.isAssignableFrom(message.getClass())) {
			TaskAndEvents taskAndEvent = (TaskAndEvents)message;
			final Props supervisorProps = AkkaTaskSupervisor.getTaskSupervisorProps(taskAndEvent.getTaskIdentifier(), taskAndEvent.getRetryCount());
			String supName = taskAndEvent.getTaskName() + "-Supervisor-" + INSTANCE_COUNTER.incrementAndGet();
			ActorRef sup = getContext().actorOf(supervisorProps, supName);
			sup.tell(taskAndEvent, getSelf());
		} else if (Event.class.isAssignableFrom(message.getClass())) {
			Event returnedEvent = (Event)message;
			// relay the Event output from Task execution to the caller ONLY if a Workflow task Actor has been created, which we dont have currently
			// getContext().parent().tell(message, getSelf()); 
			getContext().stop(getSender()); // Stop the supervisor actor and its children 
			// now post the generated output event for further processing
			fluxRuntimeConnector.submitEvent(new EventData(returnedEvent.getName(), returnedEvent.getType(), returnedEvent.getEventData(), returnedEvent.getEventSource()), returnedEvent.getStateMachineInstanceId());
		} else {
			logger.error("Task received a message that it cannot process. Only com.flipkart.flux.impl.message.TaskAndEvents is supported. Message type received is : {}", message.getClass().getName());
			unhandled(message);
		}
	}
	
}
