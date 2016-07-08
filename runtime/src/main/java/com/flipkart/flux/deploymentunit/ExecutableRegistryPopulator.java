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
import com.flipkart.flux.client.intercept.TaskInterceptor;
import com.flipkart.flux.client.model.Task;
import com.flipkart.flux.client.registry.ExecutableImpl;
import com.flipkart.flux.client.registry.ExecutableRegistry;
import com.flipkart.polyguice.core.Initializable;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.List;
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

    private Set<String> routerNames;

    //todo: included this to call generateTaskIdentifier(), it's better to move that method to separate class like util class
    private TaskInterceptor taskInterceptor;

    @Inject
    public ExecutableRegistryPopulator(DeploymentUnitUtil deploymentUnitUtil, ExecutableRegistry executableRegistry, TaskInterceptor taskInterceptor) {
        this.deploymentUnitUtil = deploymentUnitUtil;
        this.executableRegistry = executableRegistry;
        this.taskInterceptor = taskInterceptor;
    }

    @Override
    public void initialize() {
        routerNames = new HashSet<String>();
        List<String> deploymentUnitNames = deploymentUnitUtil.getAllDeploymentUnits();

        //for each deployment unit
        for(String deploymentUnitName : deploymentUnitNames) {
            try {

                //create a class loader and get required classes from it
                URLClassLoader classLoader = deploymentUnitUtil.getClassLoader(deploymentUnitName);
                Class TaskClass = classLoader.loadClass(Task.class.getCanonicalName());
                Class InjectorClass = loadClassLoaderInjector(classLoader);
                Method getClassInstanceMethod = InjectorClass.getMethod("getClassInstance", Class.class);
                Class guiceModuleClass = classLoader.loadClass("com.google.inject.Module");

                Object injectorClassInstance;
                String DUModuleClassFQN = String.valueOf(deploymentUnitUtil.getProperties(classLoader).getProperty("guiceModuleClass"));

                //check if user has specified any guice module class name in deployment unit configuration file, if not create an empty injector
                if(DUModuleClassFQN == null || DUModuleClassFQN.trim().isEmpty() || DUModuleClassFQN.equals("null"))
                    injectorClassInstance = InjectorClass.newInstance();
                else {
                    injectorClassInstance = InjectorClass.getConstructor(guiceModuleClass).newInstance(classLoader.loadClass(DUModuleClassFQN).newInstance());
                }

                Set<Method> taskMethods = deploymentUnitUtil.getTaskMethods(classLoader);
                //for every task method found in the deployment unit create an executable and keep it in executable registry
                for(Method method : taskMethods) {
                    String taskIdentifier = taskInterceptor.generateTaskIdentifier(method);
                    Annotation taskAnnotation = method.getAnnotationsByType(TaskClass)[0];
                    Class<? extends Annotation> annotationType = taskAnnotation.annotationType();
                    long timeout = 1000l; //default timeout
                    for (Method annotationMethod : annotationType.getDeclaredMethods()) {
                        Object value = annotationMethod.invoke(taskAnnotation, (Object[])null);
                        if(annotationMethod.getName().equals("timeout")) { //todo: find a way get Task.timeout() name
                            timeout = (Long) value;
                        }
                    }

                    Object singletonMethodOwner = getClassInstanceMethod.invoke(injectorClassInstance, method.getDeclaringClass());
                    executableRegistry.registerTask(taskIdentifier, new ExecutableImpl(singletonMethodOwner, method, timeout, classLoader));

                    //add the router name to deployment unit routers
                    routerNames.add(method.getDeclaringClass().getName() + "_" + method.getName()); //convention: task router name is classFQN_taskMethodName
                }
            } catch (Exception e) {
                LOGGER.error("Unable to populate Executable Registry for deployment unit: "+deploymentUnitName+". Exception: "+e.getMessage());
                throw new FluxError(FluxError.ErrorType.runtime, "Unable to populate Executable Registry for deployment unit: "+deploymentUnitName, e);
            }
        }

    }

    /**
     * Loads {@Link ClassLoaderInjector} class into given deployment unit's class loader and returns it.
     * @param deploymentUnitClassLoader
     * @return ClassLoaderInjector class
     */
    private Class loadClassLoaderInjector(ClassLoader deploymentUnitClassLoader) {
        try {
            //Convert the class into bytes
            byte[] classBytes = IOUtils.toByteArray(this.getClass().getResourceAsStream("/com/flipkart/flux/deploymentunit/ClassLoaderInjector.class"));

            Class classLoaderInjectorClass;
            Class classLoaderClass = Class.forName("java.lang.ClassLoader");
            java.lang.reflect.Method method = classLoaderClass.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);

            // protected method invocation
            method.setAccessible(true);
            try {
                Object[] args = new Object[]{ClassLoaderInjector.class.getCanonicalName(), classBytes, 0, classBytes.length};
                classLoaderInjectorClass = (Class) method.invoke(deploymentUnitClassLoader, args);
            } finally {
                method.setAccessible(false);
            }

            return classLoaderInjectorClass;
        } catch (Exception e) {
            LOGGER.error("Unable to load class ClassLoaderInjector into deployment unit's class loader. Exception: " + e.getMessage());
            throw new RuntimeException("Unable to load class ClassLoaderInjector into deployment unit's class loader. Exception: " + e.getMessage());
        }
    }

    public Set<String> getDeploymentUnitRouterNames() {
        return routerNames;
    }

}
