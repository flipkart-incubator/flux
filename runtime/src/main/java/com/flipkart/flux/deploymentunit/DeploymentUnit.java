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
import com.flipkart.flux.client.intercept.MethodId;
import com.flipkart.flux.client.model.Task;
import com.flipkart.flux.client.runtime.Stoppable;
import com.flipkart.polyguice.config.YamlConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static com.flipkart.flux.client.constant.ClientConstants._VERSION;

/**
 * <code>DeploymentUnit</code> represents a Deployment Unit.
 * @author shyam.akirala
 * @author gaurav.ashok
 */
public class DeploymentUnit {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentUnit.class);

    /** Key in the config file, which has list of workflow/task class FQNs*/
    private static final String WORKFLOW_CLASSES = "workflowClasses";

    /** Name of the deployment unit. */
    private String name;

    /** Version */
    private Integer version;

    /** Class loader of the deployment unit*/
    private DeploymentUnitClassLoader deploymentUnitClassLoader;

    /** Tasks which belong to the deployment unit*/
    private Map<String, Method> taskMethods;

    /** Object Mapper class instance which used for serialization/deserialization through out this deployment unit. Having it here to avoid multiple instances */
    private Object objectMapperInstance;

    /** Object of {@link com.google.inject.Injector} which is used to provide instances of classes which are in this deployment unit */
    private Object injectorClassInstance;

    /** Object of {@link Stoppable} to stop/close all acquired resources */
    private Object stoppableInstance;

    /** Configuration of this deployment unit which is specified in flux_config.yml file */
    private YamlConfiguration configuration;

    /** Constructor*/
    public DeploymentUnit(String name, Integer version, DeploymentUnitClassLoader deploymentUnitClassLoader, YamlConfiguration configuration) {
        this.name = name;
        this.version = version;
        this.deploymentUnitClassLoader = deploymentUnitClassLoader;
        this.configuration = configuration;
        this.taskMethods = new HashMap<>();

        // populate all methods annotated with {@link Task}
        populateTaskMethods();

        // load ClassLoaderInjector class from app class loader to deployment unit's class loader.
        loadClassLoaderInjector();

        // create object mapper instance per deployment unit which is used for serialization/deserialization of {@link com.flipkart.flux.domain.Event eventData}
        createObjectMapperInstance();

        // load {@link Stoppable} instance so that it can be used later to release resources.
        loadStoppableInstance();
    }

    public void close() {
        // release all acquired resources
        if(stoppableInstance != null) {
            try {
                Method stopMethod = stoppableInstance.getClass().getMethod("stop");
                stopMethod.invoke(stoppableInstance);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                LOGGER.error("Unexpected error while calling stop method for deploymentUnit: {}/{}", name, version);
            } catch (InvocationTargetException e) {
                LOGGER.error("Exception occurred when stop method called for deploymentUnit: {}/{}", name, version, e);
            }
        }

        // close the classLoader
        try {
            deploymentUnitClassLoader.close();
        } catch (IOException e) {
            LOGGER.error("IOexception while closing classLoader", e);
        }
    }

    /**
     * Loads {@Link ClassLoaderInjector} class into given deployment unit's class loader and returns it.
     */
    private void loadClassLoaderInjector() {

        Class injectorClass = null;

        try {
            //Convert the class into bytes
            byte[] classBytes = IOUtils.toByteArray(this.getClass().getResourceAsStream("/com/flipkart/flux/deploymentunit/ClassLoaderInjector.class"));
            injectorClass = deploymentUnitClassLoader.defineClass(ClassLoaderInjector.class.getCanonicalName(), classBytes);
        } catch(LinkageError le) {
            // This exception never comes in ideal world. Can occur while unit testing as class is already loaded
            // (while unit testing we use App classloader as parent for Deployment unit class loader, due to that this class would be already loaded)
            // TODO: Altering production code for unit testing is not good. Find a workaround.
            LOGGER.error("End of the world! Seems ClassloaderInjector.class is loaded already in this deployment.", le);
            try {
                injectorClass = deploymentUnitClassLoader.loadClass("com.flipkart.flux.deploymentunit.ClassLoaderInjector");
            } catch (ClassNotFoundException e) {
                throw new FluxError(FluxError.ErrorType.runtime, "Unable to load class ClassLoaderInjector into deployment unit's class loader.", e);
            }
        } catch(IOException e) {
            throw new FluxError(FluxError.ErrorType.runtime, "Unexpected error while converting ClassLoaderInjector.class to bytes.", e);
        }

        try {
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
        stoppableInstance = null;
        try {
            Method getInstanceMethod = injectorClassInstance.getClass().getMethod("getInstance", Class.class);
            Class objectMapper = deploymentUnitClassLoader.loadClass("com.fasterxml.jackson.databind.ObjectMapper");
            objectMapperInstance = getInstanceMethod.invoke(injectorClassInstance, objectMapper);
        } catch (Exception e) {
            throw new FluxError(FluxError.ErrorType.runtime, "Error occurred while creating Object Mapper instance for Deployment Unit: " + name + "/" + version, e);
        }
    }

    /** Loads {@link Stoppable} instance using the injector */
    private void loadStoppableInstance() {
        try {
            Method getInstanceMethod = injectorClassInstance.getClass().getMethod("getInstance", Class.class);
            Class stoppableClass = deploymentUnitClassLoader.loadClass("com.flipkart.flux.client.runtime.Stoppable");
            stoppableInstance = getInstanceMethod.invoke(injectorClassInstance, stoppableClass);
        } catch (Exception e) {
            LOGGER.error("Unable to find/load Stoppable instance for deploymentUnit: {}/{}", name, version, e);
        }
    }

    /**
     * Given a class loader, retrieves workflow classes names from config file, and returns methods
     * which are annotated with {@link com.flipkart.flux.client.model.Task} annotation in those classes.
     */
    private void populateTaskMethods() {

        List<String> classNames = (List<String>) configuration.getProperty(WORKFLOW_CLASSES);

        try {
            //loading this class separately in this class loader as the following isAnnotationPresent check returns false, if
            //we use default class loader's Task, as both class loaders don't have any relation between them.
            Class taskAnnotationClass = deploymentUnitClassLoader.loadClass(Task.class.getCanonicalName());

            for (String name : classNames) {
                Class clazz = deploymentUnitClassLoader.loadClass(name);

                for (Method method : clazz.getMethods()) {
                    if (method.isAnnotationPresent(taskAnnotationClass)) {
                        Annotation taskAnnotation = method.getAnnotationsByType(taskAnnotationClass)[0];
                        long version = 0;

                        for (Method annotationMethod : taskAnnotationClass.getDeclaredMethods()) {
                            if (annotationMethod.getName().equals("version")) {
                                version = (Long) annotationMethod.invoke(taskAnnotation);
                            }
                        }

                        MethodId methodId = new MethodId(method);
                        String taskIdentifier = methodId.toString() + _VERSION + version;

                        taskMethods.put(taskIdentifier, method);
                    }
                }
            }
        } catch (Exception e) {
            throw new FluxError(FluxError.ErrorType.runtime, "Error while getting task methods for deploymentUnit: " + name + "/" + version, e);
        }
    }

    /** Accessor methods*/
    public String getName() {
        return name;
    }

    public DeploymentUnitClassLoader getDeploymentUnitClassLoader() {
        return deploymentUnitClassLoader;
    }

    public Map<String, Method> getTaskMethods() {
        return taskMethods;
    }

    public Object getObjectMapperInstance() {
        return objectMapperInstance;
    }

    public Object getInjectorClassInstance() {
        return injectorClassInstance;
    }

    public Configuration getTaskConfiguration() {
        return configuration.subset("taskConfig");
    }

    public Integer getVersion() {
        return this.version;
    }
}
