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
import org.apache.commons.configuration.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Method;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author shyam.akirala
 */
@RunWith(MockitoJUnitRunner.class)
public class AkkaModuleTest {

    private AkkaModule akkaModule;

    @Mock
    Configuration configuration;

    @Mock
    DeploymentUnit deploymentUnit;

    @Mock
    DeploymentUnitsManager deploymentUnitManager;

    @Before
    public void setup() {
        akkaModule = new AkkaModule();
    }

    @Test
    public void testGetRouterConfigurations_shouldGetConcurrencyValueFromConfigFile() throws Exception {
        when(configuration.getProperty("com.flipkart.flux.integration.SimpleWorkflow_simpleIntegerReturningTask.executionConcurrency")).thenReturn(5);
        when(deploymentUnit.getTaskConfiguration()).thenReturn(configuration);
        Map<String, DeploymentUnit> deploymentUnitsMap = new HashMap<String, DeploymentUnit>(){{
            put("DeploymentUnit1", deploymentUnit);
        }};
        int defaultNoOfActors = 10;

        Class simpleWorkflowClass = this.getClass().getClassLoader().loadClass("com.flipkart.flux.integration.SimpleWorkflow");
        Map<String, Method> taskMethods = Collections.singletonMap("com.flipkart.flux.integration.SimpleWorkflow_simpleIntegerReturningTask_com.flipkart.flux.integration.IntegerEvent_version1", simpleWorkflowClass.getMethod("simpleIntegerReturningTask"));
        when(deploymentUnit.getTaskMethods()).thenReturn(taskMethods);

        when(deploymentUnitManager.getAllDeploymentUnits()).thenReturn(Collections.singleton(deploymentUnit));

        assertThat(akkaModule.getRouterConfigs(deploymentUnitManager, new TaskRouterUtil(defaultNoOfActors))).
                containsEntry("com.flipkart.flux.integration.SimpleWorkflow_simpleIntegerReturningTask", 5);
    }

    @Test
    public void testGetRouterConfigurations_shouldUseDefaultConcurrentValue() throws Exception {
        when(deploymentUnit.getTaskConfiguration()).thenReturn(configuration);
        Map<String, DeploymentUnit> deploymentUnitsMap = new HashMap<String, DeploymentUnit>(){{
            put("DeploymentUnit1", deploymentUnit);
        }};
        int defaultNoOfActors = 10;

        Class simpleWorkflowClass = this.getClass().getClassLoader().loadClass("com.flipkart.flux.integration.SimpleWorkflow");
        Map<String, Method> taskMethods = Collections.singletonMap("com.flipkart.flux.integration.SimpleWorkflow_simpleIntegerReturningTask_com.flipkart.flux.integration.IntegerEvent_version1", simpleWorkflowClass.getMethod("simpleIntegerReturningTask"));
        when(deploymentUnit.getTaskMethods()).thenReturn(taskMethods);

        when(deploymentUnitManager.getAllDeploymentUnits()).thenReturn(Collections.singleton(deploymentUnit));

        assertThat(akkaModule.getRouterConfigs(deploymentUnitManager, new TaskRouterUtil(defaultNoOfActors))).
                containsEntry("com.flipkart.flux.integration.SimpleWorkflow_simpleIntegerReturningTask", defaultNoOfActors);
    }
}