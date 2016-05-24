package com.flipkart.flux.impl.task.registry;

import akka.cluster.routing.ClusterRouterPoolSettings;
import javafx.util.Pair;

/**
 * Provides configurations needed for creating routers in the system
 *
 * @see RouterRegistry
 * @author yogesh.nachnani
 */
public interface RouterConfigurationRegistry {

    /**
     *
     * @return an iterable that provides (Router Name, ClusterRouterPoolSettings) pairs
     */
    Iterable<Pair<String,ClusterRouterPoolSettings>> getConfigurations();
}
