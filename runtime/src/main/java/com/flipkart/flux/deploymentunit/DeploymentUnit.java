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
import com.flipkart.polyguice.config.YamlConfiguration;
import org.apache.commons.io.IOUtils;

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

    /** Object of {@link com.google.inject.Injector} which is used to provide instances of classes which are in this deployment unit */
    private Object injectorClassInstance;

    /** Configuration of this deployment unit which is specified in flux_config.yml file */
    private YamlConfiguration configuration;

    /** Constructor*/
    public DeploymentUnit(String name, DeploymentUnitClassLoader deploymentUnitClassLoader, Set<Method> taskMethods, YamlConfiguration configuration) {
        this.name = name;
        this.deploymentUnitClassLoader = deploymentUnitClassLoader;
        this.taskMethods = taskMethods;
        this.configuration = configuration;

        // load ClassLoaderInjector class from app class loader to deployment unit's class loader.
        loadClassLoaderInjector();

        // create object mapper instance per deployment unit which is used for serialization/deserialization of {@link com.flipkart.flux.domain.Event eventData}
        createObjectMapperInstance();
    }

    /**
     * Loads {@Link ClassLoaderInjector} class into given deployment unit's class loader and returns it.
     */
    private void loadClassLoaderInjector() {
        try {
            //Convert the class into bytes
            byte[] classBytes = IOUtils.toByteArray(this.getClass().getResourceAsStream("/com/flipkart/flux/deploymentunit/ClassLoaderInjector.class"));
            Class injectorClass = deploymentUnitClassLoader.defineClass(ClassLoaderInjector.class.getCanonicalName(), classBytes);
            Class guiceModuleClass = deploymentUnitClassLoader.loadClass("com.google.inject.Module");
            String DUModuleClassFQN = String.valueOf(configuration.getProperty("guiceModuleClass"));

            //check if user has specified any guice module class name in deployment unit configuration file, if not create an empty injector
            if(DUModuleClassFQN == null || DUModuleClassFQN.trim().isEmpty() || DUModuleClassFQN.equals("null"))
                injectorClassInstance = injectorClass.newInstance();
            else {
                injectorClassInstance = injectorClass.getConstructor(guiceModuleClass).newInstance(deploymentUnitClassLoader.loadClass(DUModuleClassFQN).newInstance());
            }

        } catch (Exception e) {
            throw new FluxError(FluxError.ErrorType.runtime, "Unable to load class ClassLoaderInjector into deployment unit's class loader.", e);
        }
    }

    /** Creates an instance of the object mapper for this deployment unit */
    private void createObjectMapperInstance() {
        try {
            Method getInstanceMethod = injectorClassInstance.getClass().getMethod("getInstance", Class.class);
            Class objectMapper = deploymentUnitClassLoader.loadClass("com.fasterxml.jackson.databind.ObjectMapper");
            objectMapperInstance = getInstanceMethod.invoke(injectorClassInstance, objectMapper);
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

    public Object getInjectorClassInstance() {
        return injectorClassInstance;
    }
}
