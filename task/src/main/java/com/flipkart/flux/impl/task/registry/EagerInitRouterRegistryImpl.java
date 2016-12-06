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

package com.flipkart.flux.impl.task.registry;

import akka.actor.*;
import akka.routing.RoundRobinPool;
import com.flipkart.flux.impl.boot.ActorSystemManager;
import com.flipkart.flux.impl.task.AkkaTask;
import com.flipkart.polyguice.core.Initializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

import static akka.actor.SupervisorStrategy.escalate;
import static akka.actor.SupervisorStrategy.restart;

/**
 * Eagerly creates and maintains references to all the required routers of the system
 * @see RouterRegistry
 * @author yogesh.nachnani
 * @author shyam.akirala
 */
@Singleton
public class EagerInitRouterRegistryImpl implements RouterRegistry, Initializable {

    /** Logger instance of this class */
    private static final Logger logger = LoggerFactory.getLogger(EagerInitRouterRegistryImpl.class);

	/** Access to the Actor system initialized by Flux*/
    private ActorSystemManager actorSystemManager;

    /** Contains list of routers and actors per router*/
    private Map<String, Integer> routerConfigMap;

    /** Local Map of initialized Router instances*/
    private final HashMap<String, ActorRef> routerMap;

    /** The global max retries for creating Task Actors*/
    private int maxTaskActorCreateRetries;
    
    @Inject
    public EagerInitRouterRegistryImpl(ActorSystemManager actorSystemManager,
                                       @Named("runtime.actorsystem.maxTaskActorCreateRetries") int maxTaskActorCreateRetries,
                                       @Named("routerConfigMap")Map<String, Integer> routerConfigMap) {
        this.routerMap = new HashMap<>();
        this.actorSystemManager = actorSystemManager;
        this.maxTaskActorCreateRetries = maxTaskActorCreateRetries;
        this.routerConfigMap = routerConfigMap;
    }

    /**
     * This implementation looks up the local hashmap and returns the appropriate router
     * @param forWorker the (convention based) name for the router
     * @return ActorRef for the router or null if the router does not exist
     */
    @Override
    public ActorRef getRouter(String forWorker) {
        return this.routerMap.get(forWorker);
    }

    /**
     * Iterates over given list of configured router names and creates a cluster singleton router for each
     * */
    @Override
    public void initialize() {
        final ActorSystem actorSystem = actorSystemManager.retrieveActorSystem();

        for (Map.Entry<String, Integer> routerConfig : routerConfigMap.entrySet()) {
            String routerName = routerConfig.getKey();
            Integer noOfActors = routerConfig.getValue();
            //create a local router with configured no.of task actors and keep the router reference in routerMap
            ActorRef router = actorSystem.actorOf(new RoundRobinPool(noOfActors).withSupervisorStrategy(getTasksuperviseStrategy())
                    .props(Props.create(AkkaTask.class)), routerName);
            this.routerMap.put(routerName, router);
            logger.info("Created router: {} with no.of actors: {}", routerName, noOfActors);
        }
    }
    
    /**
     * Helper method to create supervision strategy for every Task Actor {@link AkkaTask} loaded into Flux
     * @return SupervisorStrategy for Task actor {@link AkkaTask}
     */
    private SupervisorStrategy getTasksuperviseStrategy() {
    	return new OneForOneStrategy(this.maxTaskActorCreateRetries, Duration.Inf(), t -> {
	        if (t instanceof RuntimeException) { //All Akka exceptions eg. Actor killed, initialization, interrupted exceptions are Runtime exceptions
	            return restart();
	        } else {
	            return escalate();
	        }
    	});
    }
}
