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

package com.flipkart.flux.impl.boot;

import akka.actor.*;
import akka.cluster.Cluster;
import com.flipkart.flux.impl.recovery.DeadLetterActor;
import com.flipkart.polyguice.core.Initializable;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import kamon.Kamon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Responsible for bringing up the entire akka runtime
 * Lifecycle of this class is managed using polyguice
 * @author yogesh.nachnani
 * @author regunath.balasubramanian
 */
@Singleton
public class ActorSystemManager implements Initializable {

	/** Logger for this class*/
	private static final Logger LOGGER = LoggerFactory.getLogger(ActorSystemManager.class);
	
    private String actorSystemName;
    private String configName;
    private  Boolean withMetrics;

    private ActorSystem system;

    private boolean isInitialised;

    ActorSystemManager() {
        this.isInitialised = false;
    }

    @Inject
    public ActorSystemManager(@Named("runtime.actorsystem.name") String actorSystemName,
                              @Named("runtime.actorsystem.configname") String configName,
                              @Named("runtime.actorsystem.metrics") Boolean withMetrics
    ) {
        this();
        this.actorSystemName = actorSystemName;
        this.configName = configName;
        this.withMetrics = withMetrics;
    }

    public ActorSystem retrieveActorSystem() {
        if(isInitialised) {
            return this.system;
        }
        throw new IllegalStateException("Actor system not initialised yet");
    }

    /**
     * Reads the configurations and joins/creates the akka cluster
     */
    @Override
    public void initialize() {
        if(withMetrics) {
            Kamon.start();
        }
        Config config = ConfigFactory.load(configName);
        system = ActorSystem.create(actorSystemName, config);
        // register the Dead Letter Actor
        final ActorRef deadLetterActor = system.actorOf(Props.create(DeadLetterActor.class));
        system.eventStream().subscribe(deadLetterActor, DeadLetter.class);        
        this.isInitialised = true;

        registerShutdownHook();

    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(
                new Thread() {
                    @Override
                    public void run() {
                        if(withMetrics) {
                            Kamon.shutdown();
                        }
                        if (system != null) {
                            Cluster cluster = Cluster.get(system);
                            Address selfAddress = cluster.selfAddress();
                            cluster.leave(selfAddress);
                            try {
                                Thread.sleep(5000); //we'll wait for five seconds to allow the node to send cluster leaving message
                            } catch (InterruptedException e) {
                                LOGGER.error("Interrupted while waiting for a second before calling Actor system terminate");
                            }
                            Future<Terminated> terminateFut = system.terminate();
                            try {
                                LOGGER.info("Shutting down Actor system");
                                Await.result(terminateFut, Duration.Inf()); // we'll wait for infinite time. Flux shutdown can decide to kill the JVM
                            } catch (Exception e) {
                                LOGGER.error("Error shutting down Actor system", e);
                            }
                        }
                    }
                }
        );
    }

    public boolean isInitialised() {
        return isInitialised;
    }
}
