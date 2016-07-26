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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.flipkart.flux.api.core.Task;
import com.flipkart.flux.impl.message.TaskRedriverDetails;

import akka.actor.ActorRef;
import akka.actor.Address;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent.CurrentClusterState;
import akka.cluster.ClusterEvent.LeaderChanged;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.ActorRefRoutee;
import akka.routing.RoundRobinRoutingLogic;
import akka.routing.Routee;
import akka.routing.Router;

/**
 * <code>AkkaRedriverTask</code> is an Akka {@link UntypedActor} that uses a cluster singleton scheduler to execute redrivers for
 * stalled {@link Task}
 * 
 * @author regunath.balasubramanian
 */
public class AkkaRedriverTask extends UntypedActor {

	/** Logger instance for this class*/
    private LoggingAdapter logger = Logging.getLogger(getContext().system(), this);
    
    /** Status to indicate if this instance is the Leader Actor for scheduling*/
    private AtomicBoolean isLeader = new AtomicBoolean(false);

	/** Get a handle to the cluster that this Actor is part of*/
	private Cluster cluster = Cluster.get(getContext().system());
	
	/** The Router instance that manages local redriver task execution Actors */
	private Router router;

	/**
	 * Constructor for this class. Creates a router with list of RedriverWorker instances
	 */
	public AkkaRedriverTask(int noOfRedriverWorkers) {
		List<Routee> routees = new ArrayList<Routee>();
		for (int i = 0; i < noOfRedriverWorkers; i++) {
			ActorRef r = getContext().actorOf(Props.create(RedriverWorker.class));
			getContext().watch(r);
			routees.add(new ActorRefRoutee(r));
		}
		this.router = new Router(new RoundRobinRoutingLogic(), routees);
	}
	
	/**
	 * The Akka Actor callback method. Registers for Cluster events on Leader changes
	 * @see akka.actor.UntypedActor#preStart()
	 */
	public void preStart() {
		cluster.subscribe(getSelf(),LeaderChanged.class);
	}
	
	/**
	 * The Akka Actor callback method for processing redriver commands and cluster leader changes
	 * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
	 */
	public void onReceive(Object message) throws Exception {
		if (message instanceof LeaderChanged) {
			this.checkAndSetAsLeader(((LeaderChanged)message).getLeader());
		} else if (message instanceof CurrentClusterState) {
			this.checkAndSetAsLeader(((CurrentClusterState)message).getLeader());
		} else if (message instanceof Terminated) {
			// add back any terminated RedriverWorker instances
			this.router = this.router.removeRoutee(((Terminated) message).actor());
			ActorRef r = getContext().actorOf(Props.create(RedriverWorker.class));
			getContext().watch(r);
			this.router = this.router.addRoutee(new ActorRefRoutee(r));
		} else if (message instanceof TaskRedriverDetails) {
			TaskRedriverDetails redriverDetails = (TaskRedriverDetails)message;
			switch(redriverDetails.getAction()) {
			case Register:
				// register the Task with the redriver scheduler
				break;
			case Deregister:
				// de-register the Task with the redriver scheduler
				break;
			case Redrive:
				// Redrive the Task using the Redriver Router
				this.router.route(message, getSelf());
				break;
			}
		} else {
			logger.error("Redriver Task received a message that it cannot process. Message type received is : {}", message.getClass().getName());
			unhandled(message);
		}
	}
	
	/**
	 * Helper to determine if this instance of the Actor is the leader and load the scheduled redriver tasks using the scheduler
	 * @param leaderAddress the fully qualified leader address
	 */
	private void checkAndSetAsLeader(Address leaderAddress) {
		Address selfAddress = getSelf().path().address();
		if (leaderAddress.equals(selfAddress)) {
			if (!this.isLeader.get()) {
				this.isLeader.set(true);
				logger.info("Promoting Actor : {} as the leader for cluster-wide redriver Scheduler", leaderAddress);
				// notify the scheduler to load all scheduled tasks
				// register this actor for callbacks when redriver times elapse for registered tasks
			}
		}
	}
	
	/**
	 * Actor for redriving Tasks
	 * @author regunath.balasubramanian
	 */
	private class RedriverWorker extends UntypedActor {
		/**
		 * Actor call back method for executing stalled Tasks 
		 * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
		 */
		public void onReceive(Object message) throws Exception {
			if (message instanceof TaskRedriverDetails && 
					((TaskRedriverDetails)message).getAction().equals(TaskRedriverDetails.RegisterAction.Redrive)) {
				// Check if the Task has stalled. If yes, recreate the appropriate TaskAndEvents and drive the task
			}else {
				logger.error("Redriver Task Wroker received a message that it cannot process (or) a non-redriver action. Message type received is : {}", message.getClass().getName());
				unhandled(message);
			}
		}
	}
    
}
