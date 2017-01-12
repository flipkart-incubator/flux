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
import com.flipkart.flux.deploymentunit.iface.DeploymentUnitUtil;
import com.flipkart.flux.deploymentunit.iface.DeploymentUnitsManager;
import com.flipkart.flux.deploymentunit.iface.ExecutableLoader;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * A place for all DeploymentUnit related guice-foo
 */
public class DeploymentUnitModule extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger(DeploymentUnitModule.class);

    @Override
    protected void configure() {
        bind(ExecutableLoader.class).to(ExecutableLoaderImpl.class).in(Singleton.class);
        bind(DeploymentUnitsManager.class).to(DeploymentUnitsManagerImpl.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    public DeploymentUnitUtil getDeploymentUnitUtil(@Named("deploymentType") String deploymentType,
                                                    @Named("deploymentUnitsPath") String deploymentUnitsPath ) {
        if("directory".equals(deploymentType)) {
            return new DirectoryBasedDeploymentUnitUtil(deploymentUnitsPath);
        }
        return new NoOpDeploymentUnitUtil();
    }
}
