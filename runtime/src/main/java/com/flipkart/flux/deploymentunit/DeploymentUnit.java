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
import com.flipkart.flux.constant.RuntimeConstants;
import com.flipkart.polyguice.config.YamlConfiguration;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * <code>DeploymentUnit</code> represents a Deployment Unit.
 * @author shyam.akirala
 */
public class DeploymentUnit {

    /** Logger instance for this class */
    private static final Logger logger = LoggerFactory.getLogger(DeploymentUnit.class);

    /** Name of the deployment unit */
    private String name;

    /** Class loader of the deployment unit*/
    private DeploymentUnitClassLoader deploymentUnitClassLoader;

    /** Tasks which belong to the deployment unit*/
    private Set<Method> taskMethods;

    /** Configuration of the deployment unit which is provided in flux_config.yml */
    private YamlConfiguration configuration;

    /** Reference of {@link ClassLoaderInjector} class which is loaded into current deployment unit's classloader */
    private Class injectorClass;

    /** Instance of {@link ClassLoaderInjector} class which is loaded into current deployment unit's classloader */
    private Object injectorClassInstance;

    /** Constructor*/
    public DeploymentUnit(String name, DeploymentUnitClassLoader deploymentUnitClassLoader, Set<Method> taskMethods, YamlConfiguration configuration) {
        this.name = name;
        this.deploymentUnitClassLoader = deploymentUnitClassLoader;
        this.taskMethods = taskMethods;
        this.configuration = configuration;

        //load guice injector class into deployment unit class loader
        loadClassLoaderInjector();

        //execute life cycle class's init() method
        initialize();
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

    public YamlConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Loads {@link ClassLoaderInjector} class into current deployment unit's class loader .
     */
    private void loadClassLoaderInjector() {
        try {
            //Convert the class into bytes
            byte[] classBytes = IOUtils.toByteArray(this.getClass().getResourceAsStream("/com/flipkart/flux/deploymentunit/ClassLoaderInjector.class"));

            //load the class into deployment unit class loader
            injectorClass = deploymentUnitClassLoader.defineClass(ClassLoaderInjector.class.getCanonicalName(), classBytes);

            Class guiceModuleClass = deploymentUnitClassLoader.loadClass("com.google.inject.Module");
            String DUModuleClassFQN = String.valueOf(configuration.getProperty(RuntimeConstants.GUICE_MODULE_CLASS));

            //check if user has specified any guice module class name in deployment unit configuration file, if not create an empty injector
            if(DUModuleClassFQN == null || DUModuleClassFQN.trim().isEmpty() || DUModuleClassFQN.equals("null"))
                injectorClassInstance = injectorClass.newInstance();
            else {
                injectorClassInstance = injectorClass.getConstructor(guiceModuleClass).newInstance(deploymentUnitClassLoader.loadClass(DUModuleClassFQN).newInstance());
            }

        } catch (Exception e) {
            logger.error("Unable to load class ClassLoaderInjector into deployment unit's class loader. Exception: {}" , e.getMessage());
            throw new FluxError(FluxError.ErrorType.runtime, "Unable to load class ClassLoaderInjector into deployment unit's class loader.", e);
        }
    }

    /**
     * Returns instance of the provided class using guice injector.
     * @param clazz
     * @return object of the clazz
     */
    public Object getInstance(Class clazz) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method getInstanceMethod = injectorClass.getMethod("getInstance", Class.class);
        return getInstanceMethod.invoke(injectorClassInstance, clazz);
    }

    /**
     * Performs deployment unit initialization by calling life cycle class's init() method.
     */
    private void initialize() {
        try {
            String lifeCycleClassName = String.valueOf(configuration.getProperty(RuntimeConstants.DU_LIFECYCLE_CLASS));
            if (!(lifeCycleClassName == null || lifeCycleClassName.trim().isEmpty() || lifeCycleClassName.equals("null"))) {
                Class lifeCycleClass = deploymentUnitClassLoader.loadClass(lifeCycleClassName);
                Object lifeCycleClassInstance = getInstance(lifeCycleClass);
                Method initMethod = lifeCycleClass.getMethod("init");
                initMethod.invoke(lifeCycleClassInstance);
            }
        } catch (Exception e) {
            throw new FluxError(FluxError.ErrorType.runtime, "Unable to perform deployment unit: "+ getName() +" initialization.", e);
        }
    }

    /**
     * Performs deployment unit clean up operations by calling life cycle class's destroy() method. This would be called when flux is shutting down.
     */
    public void destroy() {
        try {
            String lifeCycleClassName = String.valueOf(configuration.getProperty(RuntimeConstants.DU_LIFECYCLE_CLASS));
            if (!(lifeCycleClassName == null || lifeCycleClassName.trim().isEmpty() || lifeCycleClassName.equals("null"))) {
                Class lifeCycleClass = deploymentUnitClassLoader.loadClass(lifeCycleClassName);
                Object lifeCycleClassInstance = getInstance(lifeCycleClass);
                Method initMethod = lifeCycleClass.getMethod("destroy");
                initMethod.invoke(lifeCycleClassInstance);
            }
        } catch (NoSuchMethodException e) {
            //Not doing anything as destroy is optional -- todo: revisit
        } catch (Exception e) { //log the error and proceed with execution
            logger.error("Error occurred while performing destroy of deployment unit: {}", getName(), e);
        }
    }
}
