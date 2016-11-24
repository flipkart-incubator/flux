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

package com.flipkart.flux.guice.module;

import com.flipkart.flux.client.intercept.MethodId;
import com.flipkart.flux.deploymentunit.DeploymentUnit;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.apache.commons.configuration.Configuration;

import javax.inject.Named;
import javax.inject.Singleton;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <code>AkkaModule</code> is a Guice {@link AbstractModule} implementation used for wiring Akka related components
 * in the core flux runtime
 * <p>
 * This depends on {@link ConfigModule } to read the configuration file and provide relevant instances
 *
 * @author yogesh.nachnani
 * @author shyam.akirala
 */
public class AkkaModule extends AbstractModule {

    @Override
    protected void configure() {
    }

    /**
     * Returns map of all the available routers and actors per router,
     * currently a router is created per Task of deployment unit //todo: handle dynamic deployments
     */
    @Provides
    @Singleton
    @Named("routerConfigMap")
    public Map<String, Integer> getRouterConfigs(@Named("deploymentUnits") Map<String, DeploymentUnit> deploymentUnitsMap,
                                                 @Named("routers.default.instancesPerNode") int defaultNoOfActors) {
        Map<String, Integer> routerConfigMap = new ConcurrentHashMap<>();

        //add all deployment units' routers
        for (Map.Entry<String, DeploymentUnit> deploymentUnitEntry : deploymentUnitsMap.entrySet()) {
            DeploymentUnit deploymentUnit = deploymentUnitEntry.getValue();
            Set<Method> taskMethods = deploymentUnit.getTaskMethods();
            Configuration taskConfiguration = deploymentUnit.getTaskConfiguration();

            for (Method taskMethod : taskMethods) {
                String routerName = new MethodId(taskMethod).getPrefix();
                Integer taskExecConcurrency = Optional.ofNullable((Integer) taskConfiguration.getProperty(routerName + ".executionConcurrency"))
                        .orElse(defaultNoOfActors);
                routerConfigMap.put(routerName, taskExecConcurrency);
            }
        }
        return routerConfigMap;
    }

}
