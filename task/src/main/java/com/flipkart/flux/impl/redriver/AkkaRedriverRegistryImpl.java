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

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.cluster.singleton.ClusterSingletonManager;
import akka.cluster.singleton.ClusterSingletonManagerSettings;
import akka.cluster.singleton.ClusterSingletonProxy;
import akka.cluster.singleton.ClusterSingletonProxySettings;
import com.flipkart.flux.task.redriver.RedriverRegistry;
import com.flipkart.flux.impl.boot.ActorSystemManager;
import com.flipkart.flux.impl.message.TaskRedriverDetails;
import com.flipkart.polyguice.core.Initializable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * <code>AkkaRedriverRegistryImpl</code> is an Akka based implementation of the {@link RedriverRegistry}. Uses a cluster singleton
 * actor to manage all redriver tasks.
 * 
 * @author regunath.balasubramanian
 */
@Singleton
public class AkkaRedriverRegistryImpl implements RedriverRegistry, Initializable {

	/** Access to the Actor system initialized by Flux*/
    private ActorSystemManager actorSystemManager;
    
    /** The number of redriver worker actor instances*/
    private int noOfRedriverWorkers;
    
    /** The Redriver Actor reference*/
    private ActorRef redriverActorProxy;
	
    /**
     * Constructor for this class
     * @param actorSystemManager the manager for accessing the initialized Actor syste,
     * @param noOfRedriverWorkers the no. of redriver worker instances
     */
    @Inject    
    public AkkaRedriverRegistryImpl(ActorSystemManager actorSystemManager, 
    		@Named("runtime.actorsystem.noOfRedriverWorkers") int noOfRedriverWorkers) {
    	this.actorSystemManager = actorSystemManager;
    	this.noOfRedriverWorkers = noOfRedriverWorkers;
    }

    /**
     * The Polyguice initialization life cycle method. Initializes the redriver Actor singleton and its proxy
     * @see com.flipkart.polyguice.core.Initializable#initialize()
     */
	public void initialize() {
        final ActorSystem actorSystem = actorSystemManager.retrieveActorSystem();
        ClusterSingletonManagerSettings settings = ClusterSingletonManagerSettings.create(actorSystem);
        // create the Redriver actor. Note that the path links this instance with the proxy that is created subsequently
        actorSystem.actorOf(ClusterSingletonManager.props(Props.create(AkkaRedriverTask.class, this.noOfRedriverWorkers),
        		PoisonPill.getInstance(),settings),"redriverActor");
		ClusterSingletonProxySettings proxySettings =
			    ClusterSingletonProxySettings.create(actorSystem);
		this.redriverActorProxy = actorSystem.actorOf(ClusterSingletonProxy.props("/user/redriverActor",
			    proxySettings), "redriverActorProxy");
	}

	/**
	 * RedriverRegistry method. Registers the Task with the redriver
	 * @see RedriverRegistry#registerTask(java.lang.Long, long)
	 */
	public void registerTask(Long taskId, long redriverDelay) {
		this.redriverActorProxy.tell(new TaskRedriverDetails(taskId, redriverDelay, TaskRedriverDetails.RegisterAction.Register), 
				ActorRef.noSender());
	}

	/**
	 * RedriverRegistry method. Un-Registers the Task with the redriver
	 * @see RedriverRegistry#deRegisterTask(java.lang.Long)
	 */
	public void deRegisterTask(Long taskId) {
		this.redriverActorProxy.tell(new TaskRedriverDetails(taskId, TaskRedriverDetails.RegisterAction.Deregister), 
				ActorRef.noSender());
	}
	
	/**
	 * RedriverRegistry method. Re-drives the Task identified by the Task Id.
	 * @see RedriverRegistry#redriveTask(java.lang.Long)
	 */
	public void redriveTask(Long taskId) {
		this.redriverActorProxy.tell(new TaskRedriverDetails(taskId, TaskRedriverDetails.RegisterAction.Redrive), 
				ActorRef.noSender());
	}

}
