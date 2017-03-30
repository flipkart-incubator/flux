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

package com.flipkart.flux.impl.eventscheduler;

import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.cluster.singleton.ClusterSingletonManager;
import akka.cluster.singleton.ClusterSingletonManagerSettings;
import com.flipkart.flux.client.runtime.FluxRuntimeConnector;
import com.flipkart.flux.eventscheduler.dao.EventSchedulerDao;
import com.flipkart.flux.eventscheduler.model.ScheduledEvent;
import com.flipkart.flux.eventscheduler.service.EventSchedulerService;
import com.flipkart.flux.impl.boot.ActorSystemManager;
import com.flipkart.flux.task.eventscheduler.EventSchedulerRegistry;
import com.flipkart.polyguice.core.Initializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * @author shyam.akirala
 */
public class EventSchedulerRegistryImpl implements EventSchedulerRegistry, Initializable {

    private static final Logger logger = LoggerFactory.getLogger(EventSchedulerRegistryImpl.class);

    /**
     * ActorSystemManager to create a singleton actor of {@link AkkaEventSchedulerService}
     */
    private ActorSystemManager actorSystemManager;

    private EventSchedulerService eventSchedulerService;

    private FluxRuntimeConnector fluxRuntimeConnector;

    private EventSchedulerDao eventSchedulerDao;

    @Inject
    public EventSchedulerRegistryImpl(ActorSystemManager actorSystemManager, EventSchedulerService eventSchedulerService,
                                      FluxRuntimeConnector fluxRuntimeConnector, EventSchedulerDao eventSchedulerDao) {
        this.actorSystemManager = actorSystemManager;
        this.eventSchedulerService = eventSchedulerService;
        this.fluxRuntimeConnector = fluxRuntimeConnector;
        this.eventSchedulerDao = eventSchedulerDao;
    }

    @Override
    public void initialize() {
        ActorSystem actorSystem = actorSystemManager.retrieveActorSystem();
        Props actorProps = Props.create(AkkaEventSchedulerService.class, eventSchedulerService);
        ClusterSingletonManagerSettings settings = ClusterSingletonManagerSettings.create(actorSystem);
        actorSystem.actorOf(ClusterSingletonManager.props(actorProps, PoisonPill.getInstance(), settings), "eventSchedulerServiceActor");
    }

    @Override
    public void registerEvent(String correlationId, String eventName, String eventData, Long scheduledTime) {
        logger.debug("Saving event: {} for state machine: {} with scheduledtime: {}", eventName, correlationId, scheduledTime);
        eventSchedulerDao.save(new ScheduledEvent(correlationId, eventName, scheduledTime, eventData));
    }

    @Override
    public void deregisterEvent(String correlationId, String eventName) {
        logger.debug("Deleting event: {} of state machine: {}", eventName, correlationId);
        eventSchedulerDao.delete(correlationId, eventName);
    }

    @Override
    public void triggerEvent(String eventName, Object eventData, String correlationId, String eventSource) {
        logger.info("Triggering event: {} for state machine: {}", eventName, correlationId);
        fluxRuntimeConnector.submitScheduledEvent(eventName, eventData, correlationId, eventSource, null);
    }

}
