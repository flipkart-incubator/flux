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

package com.flipkart.flux.module;

import com.flipkart.flux.deploymentunit.DeploymentUnitsManagerImpl;
import com.flipkart.flux.deploymentunit.DummyDeploymentUnitUtil;
import com.flipkart.flux.deploymentunit.ExecutableLoaderImpl;
import com.flipkart.flux.deploymentunit.iface.DeploymentUnitUtil;
import com.flipkart.flux.deploymentunit.iface.DeploymentUnitsManager;
import com.flipkart.flux.deploymentunit.iface.ExecutableLoader;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * <code>DeploymentUnitTestModule</code> is a {@link AbstractModule} used in E2E tests for creating dummy deployment units.
 * @author shyam.akirala
 */
public class DeploymentUnitTestModule extends AbstractModule implements Provider<DeploymentUnitUtil>{
    @Override
    protected void configure() {
        bind(ExecutableLoader.class).to(ExecutableLoaderImpl.class).in(Singleton.class);
        bind(DeploymentUnitUtil.class).toProvider(this).in(Singleton.class);
        bind(DeploymentUnitsManager.class).to(DeploymentUnitsManagerImpl.class).in(Singleton.class);
    }
    @Override
    public DeploymentUnitUtil get() {
        return new DummyDeploymentUnitUtil();
    }
}
