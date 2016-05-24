package com.flipkart.flux.impl.task.registry;

import akka.actor.*;
import akka.cluster.routing.ClusterRouterPool;
import akka.cluster.routing.ClusterRouterPoolSettings;
import akka.cluster.singleton.ClusterSingletonManager;
import akka.cluster.singleton.ClusterSingletonManagerSettings;
import akka.cluster.singleton.ClusterSingletonProxy;
import akka.cluster.singleton.ClusterSingletonProxySettings;
import akka.remote.routing.RemoteRouterConfig;
import akka.routing.RoundRobinPool;
import com.flipkart.flux.impl.boot.ActorSystemManager;
import com.flipkart.flux.impl.temp.Worker;
import com.flipkart.polyguice.core.Initializable;
import javafx.util.Pair;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Eagerly creates and maintains references to all the required routers of the system
 * @see RouterRegistry
 * @author yogesh.nachnani
 */
@Singleton
public class EagerInitRouterRegistryImpl implements RouterRegistry, Initializable {

    private ActorSystemManager actorSystemManager;

    RouterConfigurationRegistry routerConfigurationRegistry;

    private final HashMap<String, ActorRef> proxyMap;


    @Inject
    public EagerInitRouterRegistryImpl(ActorSystemManager actorSystemManager, RouterConfigurationRegistry routerConfigurationRegistry) {
        this.proxyMap = new HashMap<>();
        this.actorSystemManager = actorSystemManager;
        this.routerConfigurationRegistry = routerConfigurationRegistry;
    }

    /**
     * This implementation looks up the local hashmap and returns the appropriate router
     * @param forWorker the (convention based) name for the router
     * @return ActorRef for the router or null if the router does not exist
     */
    @Override
    public ActorRef getRouter(String forWorker) {
        return this.proxyMap.get(forWorker);
    }

    /**
     * Iterates over given list of configured router names and creates a cluster singleton router for each
     *
     * */
    @Override
    public void initialize() {
        final ActorSystem actorSystem = actorSystemManager.retrieveActorSystem();
        ClusterSingletonManagerSettings settings = ClusterSingletonManagerSettings.create(actorSystem);
        // TODO - read addresses from the network
        Address[] addresses1 = {
            AddressFromURIString.parse("akka.tcp://" + actorSystem.name() + "@localhost:2551"),
        };
        final Iterable<Pair<String, ClusterRouterPoolSettings>> configurations = routerConfigurationRegistry.getConfigurations();
        for (Pair<String, ClusterRouterPoolSettings> next : configurations) {
            actorSystem.actorOf(
                ClusterSingletonManager.props(new ClusterRouterPool(new RoundRobinPool(2), next.getValue()).props(
                    new RemoteRouterConfig(new RoundRobinPool(6), addresses1).props(
                        Props.create(Worker.class))), PoisonPill.getInstance(), settings), next.getKey());
            ClusterSingletonProxySettings proxySettings =
                ClusterSingletonProxySettings.create(actorSystem);
            this.proxyMap.put(next.getKey(), actorSystem.actorOf(ClusterSingletonProxy.props("/user/" + next.getKey(),
                proxySettings), next.getKey() + "_routerProxy"));
        }

    }
}
