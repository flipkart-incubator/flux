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
package com.flipkart.flux.impl.redriver;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.remote.RemoteActorRefProvider;
import akka.routing.ActorRefRoutee;
import akka.routing.RoundRobinRoutingLogic;
import akka.routing.Routee;
import akka.routing.Router;
import com.flipkart.flux.api.core.Task;
import com.flipkart.flux.impl.message.TaskRedriverDetails;
import com.flipkart.flux.redriver.model.ScheduledMessage;
import com.flipkart.flux.redriver.scheduler.MessageScheduler;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * <code>AkkaRedriverTask</code> is an Akka {@link UntypedActor} that uses a cluster singleton scheduler to execute redrivers for
 * stalled {@link Task}
 * 
 * @author regunath.balasubramanian
 */
public class AkkaRedriverTask extends UntypedActor {

	/** Logger instance for this class*/
    private LoggingAdapter logger = Logging.getLogger(getContext().system(), this);
    
	/** The Router instance that manages local redriver task execution Actors */
	private Router router;

	/** The message scheduler used to register redriver tasks */
	@Inject
	private static MessageScheduler messageScheduler;

	/**
	 * Constructor for this class. Creates a router with list of RedriverWorker instances
	 */
	public AkkaRedriverTask(int noOfRedriverWorkers) {
		List<Routee> routees = new ArrayList<Routee>();
		for (int i = 0; i < noOfRedriverWorkers; i++) {
			ActorRef r = getContext().actorOf(Props.create(AkkaRedriverWorker.class));
			getContext().watch(r);
			routees.add(new ActorRefRoutee(r));
		}
		this.router = new Router(new RoundRobinRoutingLogic(), routees);
	}
	
	/**
	 * The Akka Actor callback method. Starts Message Scheduler thread which performs Global redriver operations
	 * @see akka.actor.UntypedActor#preStart()
	 */
	public void preStart() {
		Address selfAddress = ((RemoteActorRefProvider) getContext().system().provider()).transport().defaultAddress();
		logger.info("Promoting Actor at address : {} as the leader for cluster-wide redriver Scheduler", selfAddress);
		messageScheduler.start();
	}
	
	/**
	 * The Akka Actor callback method. Stops Message Scheduler thread
	 * @see akka.actor.UntypedActor#postStop()
	 */
	public void postStop() {
		Address selfAddress = ((RemoteActorRefProvider) getContext().system().provider()).transport().defaultAddress();
		logger.info("Demoting Actor at address : {} as the leader for cluster-wide redriver Scheduler", selfAddress);
		messageScheduler.stop();
	}
	
	/**
	 * The Akka Actor callback method for processing redriver commands and cluster leader changes
	 * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
	 */
	public void onReceive(Object message) throws Exception {
		if (message instanceof Terminated) {
			// add back any terminated RedriverWorker instances
			this.router = this.router.removeRoutee(((Terminated) message).actor());
			ActorRef r = getContext().actorOf(Props.create(AkkaRedriverWorker.class));
			getContext().watch(r);
			this.router = this.router.addRoutee(new ActorRefRoutee(r));
		} else if (message instanceof TaskRedriverDetails) {
			TaskRedriverDetails redriverDetails = (TaskRedriverDetails)message;
			switch(redriverDetails.getAction()) {
			case Register:
				logger.debug("Register task : {} for redriver with time : {}", redriverDetails.getTaskId(), redriverDetails.getRedriverDelay());
				messageScheduler.addMessage(new ScheduledMessage(redriverDetails.getTaskId(),System.currentTimeMillis() + redriverDetails.getRedriverDelay()));
				break;
			case Deregister:
				logger.debug("DeRegister task : {} with redriver", redriverDetails.getTaskId());
				messageScheduler.removeMessage(redriverDetails.getTaskId());
				break;
			case Redrive:
				// Redrive the Task using the Redriver Router
				logger.debug("Redrive task with Id : {} ", redriverDetails.getTaskId());
				this.router.route(message, getSelf());
				break;
			}
		} else {
			logger.error("Redriver Task received a message that it cannot process. Message type received is : {}", message.getClass().getName());
			unhandled(message);
		}
	}
	
}
