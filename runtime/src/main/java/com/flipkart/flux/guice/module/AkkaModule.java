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

import com.flipkart.flux.config.TaskRouterUtil;
import com.flipkart.flux.deploymentunit.DeploymentUnit;
import com.flipkart.flux.deploymentunit.iface.DeploymentUnitsManager;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import javax.inject.Named;
import javax.inject.Singleton;
import java.lang.reflect.Method;
import java.util.Map;
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
     * currently a router is created per Task of deployment unit
     */
    @Provides
    @Singleton
    @Named("routerConfigMap")
    public Map<String, Integer> getRouterConfigs(DeploymentUnitsManager deploymentUnitsManager,
                                                 TaskRouterUtil taskRouterUtil) {
        Map<String, Integer> routerConfigMap = new ConcurrentHashMap<>();

        //add all deployment units' routers
        for (DeploymentUnit deploymentUnit : deploymentUnitsManager.getAllDeploymentUnits()) {
            Map<String, Method> taskMethods = deploymentUnit.getTaskMethods();
            for (Method taskMethod : taskMethods.values()) {
                String routerName = taskRouterUtil.getRouterName(taskMethod);
                Integer taskExecConcurrency = taskRouterUtil.getConcurrency(deploymentUnit, routerName);
                routerConfigMap.put(routerName, taskExecConcurrency);
            }
        }

        return routerConfigMap;
    }
}
