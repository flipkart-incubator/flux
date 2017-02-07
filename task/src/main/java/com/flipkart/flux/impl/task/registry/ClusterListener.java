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
package com.flipkart.flux.impl.task.registry;

import akka.actor.Address;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.UnreachableMember;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.List;

/**
 * An Akka Actor that listens to Cluster membership changes and updates a list of member addresses 
 * @author regunath.balasubramanian
 *
 */
public class ClusterListener extends UntypedActor {

	/** Logger instance for this class*/
    private LoggingAdapter logger = Logging.getLogger(getContext().system(), this);
	
	/** Get a handle to the cluster that this Actor is part of*/
	private Cluster cluster = Cluster.get(getContext().system());
	
	/** Mutating list of current cluster member addresses*/
	private List<Address> memberAddresses;

	/**
	 * Constructor
	 * @param memberAddresses mutating list of current cluster member addresses
	 */
	public ClusterListener(List<Address> memberAddresses) {
		this.memberAddresses = memberAddresses;
	}
	
	/**
	 * Overridden super class method. Sets up subscription for cluster membership changes 
	 * @see akka.actor.UntypedActor#preStart()
	 */
	public void preStart() {
		cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(), 
				MemberEvent.class, UnreachableMember.class);
	}
	
	/**
	 * Overridden super class method. Un-subscribes from cluster membership changes
	 * @see akka.actor.UntypedActor#postStop()
	 */
	public void postStop() {
		cluster.unsubscribe(getSelf());
	}
	
	/**
	 * Overridden super class method. Receives callback messages for cluster membership changes.
	 * Updates list of current cluster member addresses based on these updates
	 * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
	 */
	public void onReceive(Object message) {
		if (message instanceof MemberUp) {
			MemberUp mUp = (MemberUp) message;
			logger.info("Cluster Member is Up: " + mUp.member());
			memberAddresses.add(mUp.member().address());
		} else if (message instanceof UnreachableMember) {
			UnreachableMember mUnreachable = (UnreachableMember) message;
			logger.info("Cluster Member detected as unreachable " + mUnreachable.member());
		} else if (message instanceof MemberRemoved) {
			MemberRemoved mRemoved = (MemberRemoved) message;
			logger.info("Cluster Member is Removed: " + mRemoved.member());
			memberAddresses.remove(mRemoved.member().address());
		} else if (message instanceof MemberEvent) {
			// ignore
		} else {
			unhandled(message);
		}
	}
	
}
