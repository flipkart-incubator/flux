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
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static com.flipkart.flux.client.constant.ClientConstants._VERSION;

/**
 * <code>ExecutableRegistryPopulator</code> reads the available deployment units and puts the methods
 * which are annotated with {@link com.flipkart.flux.client.model.Task} in Executable Registry for the later execution.
 * @author shyam.akirala
 */
@Singleton
public class ExecutableRegistryPopulator implements Initializable {

    /** Logger for this class*/
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutableRegistryPopulator.class);
    private final int defaultTaskExecutionConcurrency;

    private ExecutableRegistry executableRegistry;

    /** Map with key as deployment unit name and value as corresponding {@link DeploymentUnit}*/
    private Map<String, DeploymentUnit> deploymentUnitsMap;

    @Inject
    public ExecutableRegistryPopulator(@ManagedEnv ExecutableRegistry executableRegistry, @Named("deploymentUnits")Map<String, DeploymentUnit> deploymentUnitsMap,
                                       @Named("routers.default.instancesPerNode") int defaultNoOfActors) {
        this.executableRegistry = executableRegistry;
        this.deploymentUnitsMap = deploymentUnitsMap;
        defaultTaskExecutionConcurrency = defaultNoOfActors;
    }

    @Override
    public void initialize() {

        //count down latch to parallelly populate executable registry from multiple deployment units
        CountDownLatch duCountDownLatch = new CountDownLatch(deploymentUnitsMap.size());

        //for each deployment unit, start a new thread which populates executable registry
        for(Map.Entry<String, DeploymentUnit> deploymentUnitEntry : deploymentUnitsMap.entrySet()) {
            new Thread(new ExecutableRegistryLoader(deploymentUnitEntry.getKey(), deploymentUnitEntry.getValue(), duCountDownLatch)).start();
        }

        try {
            duCountDownLatch.await(); //wait until all deployment units' tasks are loaded into executable registry
        } catch (InterruptedException e) {
            LOGGER.error("Unable to populate executable registry. Deployment unit count down latch has been interrupted. Exception: {}", e.getMessage());
            throw new FluxError(FluxError.ErrorType.runtime, "Unable to populate executable registry. Deployment unit count down latch has been interrupted.", e);
        }
    }

    /**
     * <code>ExecutableRegistryLoader</code> loads tasks of a deployment unit in {@link ExecutableRegistryPopulator#executableRegistry}
     */
    private class ExecutableRegistryLoader implements Runnable {

        private String deploymentUnitName;

        private DeploymentUnit deploymentUnit;

        private CountDownLatch duCountDownLatch;

        ExecutableRegistryLoader(String deploymentUnitName, DeploymentUnit deploymentUnit, CountDownLatch duCountDownLatch) {
            this.deploymentUnitName = deploymentUnitName;
            this.deploymentUnit = deploymentUnit;
            this.duCountDownLatch = duCountDownLatch;
        }

        @Override
        public void run() {
            try {
                //get required classes from deployment unit class loader
                DeploymentUnitClassLoader classLoader = deploymentUnit.getDeploymentUnitClassLoader();
                Class taskClass = classLoader.loadClass(Task.class.getCanonicalName());

                Object objectMapperInstance = deploymentUnit.getObjectMapperInstance();
                Object injectorClassInstance = deploymentUnit.getInjectorClassInstance();
                Class injectorClass = injectorClassInstance.getClass();
                Method getInstanceMethod = injectorClass.getMethod("getInstance", Class.class);

                Set<Method> taskMethods = deploymentUnit.getTaskMethods();
                Configuration taskConfigs = deploymentUnit.getTaskConfiguration();

                //for every task method found in the deployment unit create an executable and keep it in executable registry
                for(Method method : taskMethods) {
                    Annotation taskAnnotation = method.getAnnotationsByType(taskClass)[0];
                    Class<? extends Annotation> annotationType = taskAnnotation.annotationType();
                    long timeout = RuntimeConstants.defaultTaskTimeout;
                    long version = 0;
                    for (Method annotationMethod : annotationType.getDeclaredMethods()) {
                        Object value = annotationMethod.invoke(taskAnnotation, (Object[])null);
                        if(annotationMethod.getName().equals("timeout")) { //todo: find a way get Task.timeout() name
                            timeout = (Long) value;
                        }
                        if(annotationMethod.getName().equals("version")) {
                            version = (Long) value;
                        }
                    }

                    MethodId methodId = new MethodId(method);
                    String taskIdentifier = methodId.toString() + _VERSION + version;

                    /* get concurrency config for this task */
                    Integer taskExecConcurrency = Optional.ofNullable((Integer)taskConfigs.getProperty(methodId.getPrefix() + ".executionConcurrency"))
                            .orElse(defaultTaskExecutionConcurrency);

                    Object singletonMethodOwner = getInstanceMethod.invoke(injectorClassInstance, method.getDeclaringClass());
                    executableRegistry.registerTask(taskIdentifier, new TaskExecutableImpl(singletonMethodOwner, method, timeout, taskExecConcurrency, classLoader, objectMapperInstance));
                }

                //count down the latch once the deployment unit's tasks are loaded into executable registry
                duCountDownLatch.countDown();

            } catch (Exception e) {
                LOGGER.error("Unable to populate Executable Registry for deployment unit: {}. Exception: {}", deploymentUnitName, e.getMessage());
                throw new FluxError(FluxError.ErrorType.runtime, "Unable to populate Executable Registry for deployment unit: "+deploymentUnitName, e);
            }
        }
    }
}
