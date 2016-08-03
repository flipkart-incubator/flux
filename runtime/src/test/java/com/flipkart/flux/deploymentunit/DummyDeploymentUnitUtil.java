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

package com.flipkart.flux.deploymentunit;

import com.flipkart.flux.integration.IntegerEvent;
import com.flipkart.flux.integration.StringEvent;
import com.flipkart.polyguice.config.YamlConfiguration;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <code>DummyDeploymentUnitUtil</code> is a {@link DeploymentUnitUtil} implementation used in E2E tests to provide dummy deployment units.
 * @author shyam.akirala
 */
public class DummyDeploymentUnitUtil implements DeploymentUnitUtil {

    private static DeploymentUnitClassLoader deploymentUnitClassLoader = new DeploymentUnitClassLoader(new URL[0], DummyDeploymentUnitUtil.class.getClassLoader());

    @Override
    public List<String> getAllDeploymentUnitNames() {
        return Collections.singletonList("Dummy_deployment_unit");
    }

    @Override
    public DeploymentUnitClassLoader getClassLoader(String deploymentUnitName) throws Exception {
        return deploymentUnitClassLoader;
    }

    @Override
    public Set<Method> getTaskMethods(DeploymentUnitClassLoader classLoader) throws Exception {
        Set<Method> taskMethods = new HashSet<>();

        Class testWorkflow = this.getClass().getClassLoader().loadClass("com.flipkart.flux.dao.TestWorkflow");
        Method testTask = testWorkflow.getMethod("testTask"); taskMethods.add(testTask);
        Method dummyTask = testWorkflow.getMethod("dummyTask"); taskMethods.add(dummyTask);

        Class simpleWorkflow = this.getClass().getClassLoader().loadClass("com.flipkart.flux.integration.SimpleWorkflow");
        Method simpleStringReturningTask = simpleWorkflow.getMethod("simpleStringReturningTask", StringEvent.class); taskMethods.add(simpleStringReturningTask);
        Method simpleIntegerReturningTask = simpleWorkflow.getMethod("simpleIntegerReturningTask"); taskMethods.add(simpleIntegerReturningTask);
        Method someTaskWithIntegerAndString = simpleWorkflow.getMethod("someTaskWithIntegerAndString", StringEvent.class, IntegerEvent.class); taskMethods.add(someTaskWithIntegerAndString);

        return taskMethods;
    }

    @Override
    public YamlConfiguration getProperties(DeploymentUnitClassLoader classLoader) throws Exception {
        return new YamlConfiguration(this.getClass().getClassLoader().getResource("packaged/configuration.yml"));
    }
}
