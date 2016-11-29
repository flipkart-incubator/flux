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

import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.cluster.singleton.ClusterSingletonManager;
import akka.cluster.singleton.ClusterSingletonManagerSettings;
import com.flipkart.flux.client.runtime.FluxRuntimeConnector;
import com.flipkart.flux.impl.boot.ActorSystemManager;
import com.flipkart.flux.redriver.model.ScheduledMessage;
import com.flipkart.flux.redriver.service.MessageManagerService;
import com.flipkart.flux.redriver.service.RedriverService;
import com.flipkart.flux.task.redriver.RedriverRegistry;
import com.flipkart.polyguice.core.Initializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * <code>RedriverRegistryImpl</code> is an implementation of the {@link RedriverRegistry} that uses
 * {@link FluxRuntimeConnector} to submit taskExecution request and redriver's {@link MessageManagerService}
 * to register/deregister tasks
 * 
 * @author regunath.balasubramanian
 * @author gaurav.ashok
 */
@Singleton
public class RedriverRegistryImpl implements RedriverRegistry, Initializable {

	private static final Logger logger = LoggerFactory.getLogger(RedriverRegistryImpl.class);

	/**
	 * ActorSystemManager to create a singleton actor of {@link AkkaRedriverService}
	 */
	ActorSystemManager actorSystemManager;

	/**
	 * Flux runtime to submit task for execution.
	 */
	private FluxRuntimeConnector fluxRuntimeConnector;

	/**
	 * Redriver message service to register/deregister tasks
	 */
	private MessageManagerService redriverMessageService;

	/**
	 * Redriver service responsible for redriving any stuck tasks
	 */
	private RedriverService redriverService;

	@Inject
	public RedriverRegistryImpl(ActorSystemManager actorSystemManager, FluxRuntimeConnector fluxRuntimeConnector,
								MessageManagerService redriverMessageService, RedriverService redriverService) {
		this.actorSystemManager = actorSystemManager;
		this.fluxRuntimeConnector = fluxRuntimeConnector;
		this.redriverMessageService = redriverMessageService;
		this.redriverService = redriverService;
	}

	/**
	 * Initialize the singleton actor wrapping redriverService.
	 * @see AkkaRedriverService
	 */
	@Override
	public void initialize() {
		ActorSystem actorSystem = actorSystemManager.retrieveActorSystem();
		Props actorProps = Props.create(AkkaRedriverService.class, redriverService);
		ClusterSingletonManagerSettings settings = ClusterSingletonManagerSettings.create(actorSystem);
		actorSystem.actorOf(ClusterSingletonManager.props(actorProps, PoisonPill.getInstance(), settings), "redriverServiceActor");
	}

	/**
	 * RedriverRegistry method. Registers the Task with the redriver
	 * @see RedriverRegistry#registerTask(java.lang.Long, long)
	 */
	public void registerTask(Long taskId, long redriveDelay) {
		logger.debug("Register task : {} for redriver with time : {}", taskId, redriveDelay);
		redriverMessageService.saveMessage(new ScheduledMessage(taskId, System.currentTimeMillis() + redriveDelay));
	}

	/**
	 * RedriverRegistry method. Un-Registers the Task with the redriver
	 * @see RedriverRegistry#deRegisterTask(java.lang.Long)
	 */
	public void deRegisterTask(Long taskId) {
		logger.debug("DeRegister task : {} with redriver", taskId);
		redriverMessageService.scheduleForRemoval(taskId);
	}
	
	/**
	 * RedriverRegistry method. Re-drives the task identified by the <code>taskId</code> by submitting it back to the runtime.
	 * @see RedriverRegistry#redriveTask(java.lang.Long)
	 */
	public void redriveTask(Long taskId) {
		logger.debug("Redrive task with Id : {} ", taskId);
		fluxRuntimeConnector.redriveTask(taskId);
	}
}
