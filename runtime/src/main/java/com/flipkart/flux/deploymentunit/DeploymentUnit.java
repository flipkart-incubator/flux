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

import java.lang.reflect.Method;
import java.util.Set;

/**
 * <code>DeploymentUnit</code> represents a Deployment Unit.
 * @author shyam.akirala
 */
public class DeploymentUnit {

    /** Name of the deployment unit */
    private String name;

    /** Class loader of the deployment unit*/
    private DeploymentUnitClassLoader deploymentUnitClassLoader;

    /** Tasks which belong to the deployment unit*/
    private Set<Method> taskMethods;

    /** Constructor*/
    public DeploymentUnit(String name, DeploymentUnitClassLoader deploymentUnitClassLoader, Set<Method> taskMethods) {
        this.name = name;
        this.deploymentUnitClassLoader = deploymentUnitClassLoader;
        this.taskMethods = taskMethods;
    }

    /** Accessor methods*/
    public String getName() {
        return name;
    }

    public DeploymentUnitClassLoader getDeploymentUnitClassLoader() {
        return deploymentUnitClassLoader;
    }

    public Set<Method> getTaskMethods() {
        return taskMethods;
    }
}
