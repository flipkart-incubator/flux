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
