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

import com.flipkart.flux.api.core.FluxError;
import com.flipkart.flux.deploymentunit.DeploymentUnit;
import com.flipkart.flux.deploymentunit.DeploymentUnitClassLoader;
import com.flipkart.flux.deploymentunit.DeploymentUnitUtil;
import com.flipkart.polyguice.config.YamlConfiguration;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

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
        Map<String, DeploymentUnit> deploymentUnits = new ConcurrentHashMap<>();

        try {
            List<String> deploymentUnitNames = deploymentUnitUtil.getAllDeploymentUnitNames();
            CountDownLatch duLoadingLatch = new CountDownLatch(deploymentUnitNames.size());

            for (String deploymentUnitName : deploymentUnitNames) {
                new Thread(new DeploymentUnitLoader(deploymentUnitUtil, deploymentUnitName, deploymentUnits, duLoadingLatch)).start();
            }

            duLoadingLatch.await(); //wait until all deployment units are loaded

        } catch (NullPointerException e) {
            throw new FluxError(FluxError.ErrorType.runtime, "No deployment units found at location mentioned in configuration.yml - deploymentUnitsPath key", e);
        } catch (InterruptedException e) {
            throw new FluxError(FluxError.ErrorType.runtime, "Error occurred while counting down the latch in Deployment unit loader", e);
        }
        return deploymentUnits;
    }

    /**
     * <code>DeploymentUnitLoader</code> class used to initialize a deployment unit and put it into deployment units map.
     */
    private class DeploymentUnitLoader implements Runnable {

        private DeploymentUnitUtil deploymentUnitUtil;

        private String deploymentUnitName;

        private Map<String, DeploymentUnit> deploymentUnitsMap;

        private CountDownLatch countDownLatch;

        DeploymentUnitLoader(DeploymentUnitUtil deploymentUnitUtil, String deploymentUnitName, Map<String, DeploymentUnit> deploymentUnitsMap, CountDownLatch countDownLatch) {
            this.deploymentUnitUtil = deploymentUnitUtil;
            this.deploymentUnitName = deploymentUnitName;
            this.deploymentUnitsMap = deploymentUnitsMap;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            try {
                DeploymentUnitClassLoader deploymentUnitClassLoader = deploymentUnitUtil.getClassLoader(deploymentUnitName);
                Set<Method> taskMethods = deploymentUnitUtil.getTaskMethods(deploymentUnitClassLoader);
                YamlConfiguration configuration = deploymentUnitUtil.getProperties(deploymentUnitClassLoader);
                deploymentUnitsMap.put(deploymentUnitName, new DeploymentUnit(deploymentUnitName, deploymentUnitClassLoader, taskMethods, configuration));

                //count down the latch once the deployment unit is loaded
                countDownLatch.countDown();
            } catch (Exception e) {
                throw new FluxError(FluxError.ErrorType.runtime, "Error occurred while loading Deployment Unit: "+deploymentUnitName, e);
            }
        }
    }
}
