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

import com.flipkart.flux.deploymentunit.*;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A place for all DeploymentUnit related guice-foo
 */
public class DeploymentUnitModule extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger(DeploymentUnitModule.class);

    @Override
    protected void configure() {
        bind(DeploymentUnitUtil.class).toProvider(DeploymentUnitUtilProvider.class).in(Singleton.class);
    }

    /**
     * Returns Map<deploymentUnitName,DeploymentUnit> of all the deployment units available //todo: this shows only deployment units available at boot time, need to handle dynamic deployments.
     */
    @Provides
    @Singleton
    @Named("deploymentUnits")
    public Map<String, DeploymentUnit> getAllDeploymentUnits(DeploymentUnitUtil deploymentUnitUtil) throws Exception {
        Map<String, DeploymentUnit> deploymentUnits = new HashMap<>();

        try {
            List<String> deploymentUnitNames = deploymentUnitUtil.getAllDeploymentUnitNames();
            for (String deploymentUnitName : deploymentUnitNames) {
                DeploymentUnitClassLoader deploymentUnitClassLoader = deploymentUnitUtil.getClassLoader(deploymentUnitName);
                Set<Method> taskMethods = deploymentUnitUtil.getTaskMethods(deploymentUnitClassLoader);
                deploymentUnits.put(deploymentUnitName, new DeploymentUnit(deploymentUnitName, deploymentUnitClassLoader, taskMethods));
            }
        } catch (NullPointerException e) {
            logger.error("No deployment units found at location mentioned in configuration.yml - deploymentUnitsPath key");
        }
        return deploymentUnits;
    }
}
