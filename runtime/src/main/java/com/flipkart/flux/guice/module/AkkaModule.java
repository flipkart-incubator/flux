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
import com.flipkart.polyguice.config.YamlConfiguration;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.apache.commons.configuration.Configuration;
import org.eclipse.jetty.util.ConcurrentHashSet;

import javax.inject.Named;
import javax.inject.Singleton;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <code>AkkaModule</code> is a Guice {@link AbstractModule} implementation used for wiring Akka related components
 * in the core flux runtime
 *
 * This depends on {@link ConfigModule } to read the configuration file and provide relevant instances
 * @author yogesh.nachnani
 *
 */public class AkkaModule extends AbstractModule {
    @Override
    protected void configure() {

    }
    /**
     * Returns set of all the available routers, include routers mentioned in configuration.yml and deployment unit routers //todo: handle dynamic deployments
     */
    @Provides
    @Singleton
    @Named("routerNames")
    public Set<String> getRouterNames(@Named("deploymentUnits") Map<String, DeploymentUnit> deploymentUnitsMap, YamlConfiguration yamlConfiguration) {
        Configuration routerConfig = yamlConfiguration.subset("routers");
        Set<String> routerNames = new ConcurrentHashSet<>();
        Iterator<String> propertyKeys = routerConfig.getKeys();
        while(propertyKeys.hasNext()) {
            String propertyKey = propertyKeys.next();
            String routerName = propertyKey.substring(0, propertyKey.lastIndexOf('.'));
            routerNames.add(routerName);
        }

        //'default' is not a router name, it's a placeholder for router default settings
        routerNames.remove("default");

        //add all deployment units' routers
        for(Map.Entry<String, DeploymentUnit> deploymentUnitEntry : deploymentUnitsMap.entrySet()) {
            Set<Method> taskMethods = deploymentUnitEntry.getValue().getTaskMethods();
            routerNames.addAll(taskMethods.stream().map(method -> new MethodId(method).getPrefix()).collect(Collectors.toList()));
        }
        return routerNames;
    }

}
