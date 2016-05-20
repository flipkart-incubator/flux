package com.flipkart.flux.impl.boot;

import akka.actor.ActorSystem;
import com.flipkart.polyguice.core.Configuration;
import com.flipkart.polyguice.core.Disposable;
import com.flipkart.polyguice.core.Initializable;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import kamon.Kamon;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Responsible for bringing up the entire akka runtime
 * @author yogesh.nachnani
 */
//TODO the lifecycle will be managed by the polyguice container
@Singleton
public class ActorSystemManager implements Disposable, Initializable {

    @Configuration(required = true,name = "runtime.actorsystem.name")
    @Inject
    @Named("runtime.actorsystem.name")
    private String actorSystemName;


    @Configuration(required = true,name = "runtime.actorsystem.configname")
    @Inject
    @Named("runtime.actorsystem.configname")
    private String configName;

    @Configuration(required = true,name = "runtime.actorsystem.metrics")
    @Inject
    @Named("runtime.actorsystem.metrics")
    private  Boolean withMetrics;

    private ActorSystem system;

    private boolean isInitialised;

    /* Used by Polyguice - not to be used in production*/
    ActorSystemManager() {
        this.isInitialised = false;
    }

    public ActorSystemManager(String actorSystemName,
                              String configName,
                              Boolean withMetrics
    ) {
        this();
        this.actorSystemName = actorSystemName;
        this.configName = configName;
        this.withMetrics = withMetrics;
        initialize(); // Calling initialise so we always create a "valid" object if we ever create this class ourselves in tests
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
        this.isInitialised = true;
    }

    @Override
    public void dispose() {
        if(withMetrics) {
            Kamon.shutdown();
        }
        if (system != null) {
            system.shutdown();
        }
    }
}
