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

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.ActorRefRoutee;
import akka.routing.Router;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.Task;
import com.flipkart.flux.impl.message.HookAndEvents;
import com.flipkart.flux.impl.message.TaskAndEvents;
import com.netflix.hystrix.HystrixCommand;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

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
	@Inject
    private static TaskRegistry taskRegistry;
    
    /** Router instance for the Hook actors*/
    @Inject
    @Named("HookRouter")
    private Router hookRouter;

	//todo this setter to be removed when we have figured out the way to inject registry in actor. And corresponding unit test can be changed accordingly.
	public void setTaskRegistry(TaskRegistry taskRegistry) {
		this.taskRegistry = taskRegistry;
	}

	/**
	 * The Akka Actor callback method for processing the Task
	 * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public void onReceive(Object message) throws Exception {

		//todo actor can take a call when to throw TaskResumableException
		if (TaskAndEvents.class.isAssignableFrom(message.getClass())) {
			TaskAndEvents taskAndEvent = (TaskAndEvents)message;
		 	AbstractTask task = this.taskRegistry.retrieveTask(taskAndEvent.getTaskIdentifier());
			if (task != null) {
				// Execute any pre-exec HookS
				this.executeHooks(this.taskRegistry.getPreExecHooks(task), taskAndEvent.getEvents());
				getSender().tell(new TaskExecutor(task, taskAndEvent.getEvents()).execute(), getSelf());
				// Execute any post-exec HookS 
				this.executeHooks(this.taskRegistry.getPostExecHooks(task), taskAndEvent.getEvents());
			} else {
				logger.error("Task received EventS that it cannot process. Events received are : {}",TaskRegistry.getEventsKey(taskAndEvent.getEvents()));
			}
		} else if (HookExecutor.STATUS.class.isAssignableFrom(message.getClass())) {
			// do nothing as we don't process or interpret Hook execution responses
		} else if (message instanceof Terminated) { 
			/*
			 * add a fresh local AkkaHook instance to the router. This happens only for local JVM routers i.e when Flux runtime 
			 * in local and not distributed/clustered
			 */
			hookRouter = hookRouter.removeRoutee(((Terminated) message).actor());
			ActorRef r = getContext().actorOf(Props.create(AkkaHook.class));
			getContext().watch(r);
			hookRouter = hookRouter.addRoutee(new ActorRefRoutee(r));			
		} else {
			logger.error("Task received a message that it cannot process. Only com.flipkart.flux.domain.Event[] is supported. Message type received is : {}", message.getClass().getName());
			unhandled(message);
		}
	}
	
	/**
	 * Helper method to execute pre and post Task execution Hooks as independent Actor invocations
	 */
	private void executeHooks(List<AbstractHook> hooks, Event[] events) {
		if (hooks != null) {
			for (AbstractHook hook : hooks) {
				HookAndEvents hookAndEvents = new HookAndEvents(hook, events);
				hookRouter.route(hookAndEvents, getSelf());
			}
		}
	}


}
