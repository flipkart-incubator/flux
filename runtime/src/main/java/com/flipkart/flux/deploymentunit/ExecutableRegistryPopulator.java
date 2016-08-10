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
import com.flipkart.flux.client.registry.ExecutableRegistry;
import com.flipkart.flux.constant.RuntimeConstants;
import com.flipkart.flux.guice.annotation.ManagedEnv;
import com.flipkart.flux.registry.TaskExecutableImpl;
import com.flipkart.polyguice.core.Initializable;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

/**
 * <code>ExecutableRegistryPopulator</code> reads the available deployment units and puts the methods
 * which are annotated with {@link com.flipkart.flux.client.model.Task} in Executable Registry for the later execution.
 * @author shyam.akirala
 */
@Singleton
public class ExecutableRegistryPopulator implements Initializable {

    /** Logger for this class*/
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutableRegistryPopulator.class);

    private DeploymentUnitUtil deploymentUnitUtil;

    private ExecutableRegistry executableRegistry;

    /** Map with key as deployment unit name and value as corresponding {@link DeploymentUnit}*/
    private Map<String, DeploymentUnit> deploymentUnitsMap;

    @Inject
    public ExecutableRegistryPopulator(DeploymentUnitUtil deploymentUnitUtil, @ManagedEnv ExecutableRegistry executableRegistry, @Named("deploymentUnits")Map<String, DeploymentUnit> deploymentUnitsMap) {
        this.deploymentUnitUtil = deploymentUnitUtil;
        this.executableRegistry = executableRegistry;
        this.deploymentUnitsMap = deploymentUnitsMap;
    }

    @Override
    public void initialize() {

        //for each deployment unit
        for(Map.Entry<String, DeploymentUnit> deploymentUnitEntry : deploymentUnitsMap.entrySet()) {
            try {
                DeploymentUnit deploymentUnit = deploymentUnitEntry.getValue();

                //get required classes from deployment unit class loader
                DeploymentUnitClassLoader classLoader = deploymentUnit.getDeploymentUnitClassLoader();
                Class taskClass = classLoader.loadClass(Task.class.getCanonicalName());
                Class injectorClass = loadClassLoaderInjector(classLoader);
                Method getInstanceMethod = injectorClass.getMethod("getInstance", Class.class);
                Class guiceModuleClass = classLoader.loadClass("com.google.inject.Module");

                Object injectorClassInstance;
                String DUModuleClassFQN = String.valueOf(deploymentUnitUtil.getProperties(classLoader).getProperty("guiceModuleClass"));

                //check if user has specified any guice module class name in deployment unit configuration file, if not create an empty injector
                if(DUModuleClassFQN == null || DUModuleClassFQN.trim().isEmpty() || DUModuleClassFQN.equals("null"))
                    injectorClassInstance = injectorClass.newInstance();
                else {
                    injectorClassInstance = injectorClass.getConstructor(guiceModuleClass).newInstance(classLoader.loadClass(DUModuleClassFQN).newInstance());
                }

                Set<Method> taskMethods = deploymentUnit.getTaskMethods();
                //for every task method found in the deployment unit create an executable and keep it in executable registry
                for(Method method : taskMethods) {
                    String taskIdentifier = new MethodId(method).toString();
                    Annotation taskAnnotation = method.getAnnotationsByType(taskClass)[0];
                    Class<? extends Annotation> annotationType = taskAnnotation.annotationType();
                    long timeout = RuntimeConstants.defaultTaskTimeout;
                    for (Method annotationMethod : annotationType.getDeclaredMethods()) {
                        Object value = annotationMethod.invoke(taskAnnotation, (Object[])null);
                        if(annotationMethod.getName().equals("timeout")) { //todo: find a way get Task.timeout() name
                            timeout = (Long) value;
                        }
                    }

                    Object singletonMethodOwner = getInstanceMethod.invoke(injectorClassInstance, method.getDeclaringClass());
                    executableRegistry.registerTask(taskIdentifier, new TaskExecutableImpl(singletonMethodOwner, method, timeout, classLoader));
                }
            } catch (Exception e) {
                LOGGER.error("Unable to populate Executable Registry for deployment unit: {}. Exception: {}", deploymentUnitEntry.getKey(), e.getMessage());
                throw new FluxError(FluxError.ErrorType.runtime, "Unable to populate Executable Registry for deployment unit: "+deploymentUnitEntry.getKey(), e);
            }
        }

    }

    /**
     * Loads {@Link ClassLoaderInjector} class into given deployment unit's class loader and returns it.
     * @param deploymentUnitClassLoader
     * @return ClassLoaderInjector class
     */
    private Class loadClassLoaderInjector(DeploymentUnitClassLoader deploymentUnitClassLoader) {
        try {
            //Convert the class into bytes
            byte[] classBytes = IOUtils.toByteArray(this.getClass().getResourceAsStream("/com/flipkart/flux/deploymentunit/ClassLoaderInjector.class"));

            return deploymentUnitClassLoader.defineClass(ClassLoaderInjector.class.getCanonicalName(), classBytes);

        } catch (Exception e) {
            LOGGER.error("Unable to load class ClassLoaderInjector into deployment unit's class loader. Exception: {}" , e.getMessage());
            throw new RuntimeException("Unable to load class ClassLoaderInjector into deployment unit's class loader. Exception: " + e.getMessage());
        }
    }

}
