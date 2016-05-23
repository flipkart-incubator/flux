package com.flipkart.flux.impl.boot;

import akka.actor.*;
import akka.cluster.routing.ClusterRouterPool;
import akka.cluster.routing.ClusterRouterPoolSettings;
import akka.cluster.singleton.ClusterSingletonManager;
import akka.cluster.singleton.ClusterSingletonManagerSettings;
import akka.cluster.singleton.ClusterSingletonProxy;
import akka.cluster.singleton.ClusterSingletonProxySettings;
import akka.remote.routing.RemoteRouterConfig;
import akka.routing.RoundRobinPool;
import com.flipkart.flux.impl.temp.Worker;
import com.flipkart.polyguice.core.Configuration;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import javafx.util.Pair;
import kamon.Kamon;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Responsible for bringing up the entire akka runtime
 * @author yogesh.nachnani
 */
//TODO the lifecycle will be managed by the polyguice container
@Singleton
public class TaskEnvironmentBooter {

    @Configuration(required = true,name = "runtime.actorsystem.name")
    private String actorSystemName;
    @Configuration(required = true,name = "runtime.actorsystem.configname")
    private String configName;
    private  Boolean withMetrics;
    private ActorSystem system;
    private ClusterSingletonManagerSettings settings;
    private final HashMap<String, ActorRef> proxyMap;

    @Inject
    public TaskEnvironmentBooter(String actorSystemName,
                                 String configName,
                                 Boolean withMetrics
                                 ) {
        this.actorSystemName = actorSystemName;
        this.configName = configName;
        this.withMetrics = withMetrics;
        proxyMap = new HashMap<String, ActorRef>();
    }

    /**
     * Reads the configurations and joins/creates the akka cluster
     */
    public void preInit(){
        if(withMetrics) {
            Kamon.start();
        }
        Config config = ConfigFactory.parseString(
            "akka.remote.netty.tcp.port=" + 2551).withFallback(
            ConfigFactory.load(configName));
        system = ActorSystem.create(actorSystemName, config);
        settings = ClusterSingletonManagerSettings.create(system);
    }

    /**
     * Iterates over given configurations and creates a cluster singleton router for each
     * @param routerConfigurations iterator for a number of pairs of type <router-name,configuration>
     */
    public void init(Iterator<Pair<String,ClusterRouterPoolSettings>> routerConfigurations) {
        Address[] addresses1 = {
            AddressFromURIString.parse("akka.tcp://"+actorSystemName+"@localhost:2551"),
        };
        while (routerConfigurations.hasNext()) {
            final Pair<String, ClusterRouterPoolSettings> next = routerConfigurations.next();
            system.actorOf(
                ClusterSingletonManager.props(new ClusterRouterPool(new RoundRobinPool(2), next.getValue()).props(
                    new RemoteRouterConfig(new RoundRobinPool(6), addresses1).props(
                        Props.create(Worker.class))), PoisonPill.getInstance(), settings),next.getKey());
            // TODO - does this belong in postInit ?
            ClusterSingletonProxySettings proxySettings =
                ClusterSingletonProxySettings.create(system);
            this.proxyMap.put(next.getKey(), system.actorOf(ClusterSingletonProxy.props("/user/" + next.getKey(),
                proxySettings), next.getKey() + "_routerProxy"));
        }
    }

    public void shutdown() {
        if(withMetrics) {
            Kamon.shutdown();
        }
    }

    public ActorRef getProxy(String forRouter) {
        return this.proxyMap.get(forRouter);
    }
}
