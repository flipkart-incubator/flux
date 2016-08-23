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

package com.flipkart.flux.runner;

import com.flipkart.flux.client.FluxClientInterceptorModule;
import com.flipkart.flux.constant.RuntimeConstants;
import com.flipkart.flux.deploymentunit.ExecutableRegistryPopulator;
import com.flipkart.flux.guice.module.AkkaModule;
import com.flipkart.flux.guice.module.ConfigModule;
import com.flipkart.flux.guice.module.ContainerModule;
import com.flipkart.flux.guice.module.HibernateModule;
import com.flipkart.flux.impl.boot.TaskModule;
import com.flipkart.flux.module.DeploymentUnitTestModule;
import com.flipkart.polyguice.core.support.Polyguice;
import com.google.inject.util.Modules;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import java.io.IOException;
import java.net.URL;

/**
 * <code>DeploymentUnitGuiceJunit4Runner</code> provides guice injection capabilities to a test class instance, used by E2E tests where they creation of dummy deployment unit is needed
 * @author shyam.akirala
 */
public class DeploymentUnitGuiceJunit4Runner extends BlockJUnit4ClassRunner {

    private static Polyguice polyguice;
    static {
        URL configUrl = DeploymentUnitGuiceJunit4Runner.class.getClassLoader().getResource(RuntimeConstants.CONFIGURATION_YML);
        polyguice = new Polyguice();
        final ConfigModule configModule = new ConfigModule(configUrl);
        polyguice.modules(Modules.override(configModule).with(new DeploymentUnitTestModule()), new HibernateModule(), new ContainerModule(), new AkkaModule(), new TaskModule(), new FluxClientInterceptorModule());
        polyguice.registerConfigurationProvider(configModule.getConfigProvider());
        polyguice.prepare();
        polyguice.getComponentContext().getInstance(ExecutableRegistryPopulator.class);
    }

    /**
     * Creates a BlockJUnit4ClassRunner to run {@code klass}
     *
     * @param klass
     * @throws org.junit.runners.model.InitializationError if the test class is malformed.
     */
    public DeploymentUnitGuiceJunit4Runner(Class<?> klass) throws InitializationError, IOException {
        super(klass);
    }

    @Override
    protected Object createTest() throws Exception {
        final Object testInstance = super.createTest();
        polyguice.getComponentContext().inject(testInstance);
        return testInstance;
    }

}
