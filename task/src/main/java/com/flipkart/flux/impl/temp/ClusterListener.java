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
 *
 */

package com.flipkart.flux.impl.temp;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import akka.actor.Address;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.UnreachableMember;

public class ClusterListener  extends UntypedActor {		
	Cluster cluster = Cluster.get(getContext().system());
	private List<Address> memberAddresses;
	
	public ClusterListener(List<Address> memberAddresses) {
		this.memberAddresses = memberAddresses;
	}
	
	  @Override
	  public void preStart() {
	    //#subscribe
	    cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(), 
	        MemberEvent.class, UnreachableMember.class);
	    //#subscribe
	  }

	  //re-subscribe when restart
	  @Override
	  public void postStop() {
	    cluster.unsubscribe(getSelf());
	  }

	  @Override
	  public void onReceive(Object message) {
	    if (message instanceof MemberUp) {
	      MemberUp mUp = (MemberUp) message;
	      System.out.println("Member is Up: " + mUp.member());
	      memberAddresses.add(mUp.member().address());
	    } else if (message instanceof UnreachableMember) {
	      UnreachableMember mUnreachable = (UnreachableMember) message;
	      System.out.println("Member detected as unreachable " + mUnreachable.member());
	    } else if (message instanceof MemberRemoved) {
	      MemberRemoved mRemoved = (MemberRemoved) message;
	      System.out.println("Member is Removed: " + mRemoved.member());
	    } else if (message instanceof MemberEvent) {
	      // ignore
	    } else {
	      unhandled(message);
	    }
	  }
	  
}
