package com.flipkart.flux.impl.task.registry;

import akka.cluster.routing.ClusterRouterPoolSettings;
import javafx.util.Pair;

/**
 * Provides configurations needed for creating routers in the system
 *
 * @see RouterRegistry
 */
public interface RouterConfigurationRegistry {

    /**
     * Get an iterator that provided (Router Name, Config) pairs
     * @return
     */
    Iterable<Pair<String,ClusterRouterPoolSettings>> getConfigurations();
}
