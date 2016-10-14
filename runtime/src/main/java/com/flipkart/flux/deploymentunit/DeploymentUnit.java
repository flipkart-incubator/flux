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

import com.flipkart.flux.api.core.FluxError;

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

    /** Object Mapper class instance which used for serialization/deserialization through out this deployment unit. Having it here to avoid multiple instances */
    private Object objectMapperInstance;

    /** Constructor*/
    public DeploymentUnit(String name, DeploymentUnitClassLoader deploymentUnitClassLoader, Set<Method> taskMethods) {
        this.name = name;
        this.deploymentUnitClassLoader = deploymentUnitClassLoader;
        this.taskMethods = taskMethods;

        // create object mapper instance per deployment unit which is used for serialization/deserialization of {@link com.flipkart.flux.domain.Event eventData}
        createObjectMapperInstance();
    }

    private void createObjectMapperInstance() {
        try {
            Class objectMapper = deploymentUnitClassLoader.loadClass("com.fasterxml.jackson.databind.ObjectMapper");
            objectMapperInstance = objectMapper.newInstance();
            Method registerModuleMethod = objectMapper.getMethod("registerModule", deploymentUnitClassLoader.loadClass("com.fasterxml.jackson.databind.Module"));

            /* Register {@link com.fasterxml.jackson.datatype.joda.JodaModule} with Object mapper instance */
            Object jodaModuleInstance = deploymentUnitClassLoader.loadClass("com.fasterxml.jackson.datatype.joda.JodaModule").newInstance();
            registerModuleMethod.invoke(objectMapperInstance, jodaModuleInstance);

            /* Register {@link com.fasterxml.jackson.datatype.jdk8.Jdk8Module} with Object mapper instance */
            Object jdk8ModuleInstance = deploymentUnitClassLoader.loadClass("com.fasterxml.jackson.datatype.jdk8.Jdk8Module").newInstance();
            registerModuleMethod.invoke(objectMapperInstance, jdk8ModuleInstance);

        } catch (Exception e) {
            throw new FluxError(FluxError.ErrorType.runtime, "Error occurred while creating Object Mapper instance for Deployment Unit: " + name, e);
        }
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

    public Object getObjectMapperInstance() {
        return objectMapperInstance;
    }
}
