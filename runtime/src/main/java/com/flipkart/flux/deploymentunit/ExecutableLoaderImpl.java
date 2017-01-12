package com.flipkart.flux.deploymentunit;

import com.flipkart.flux.api.core.FluxError;
import com.flipkart.flux.client.intercept.MethodId;
import com.flipkart.flux.client.model.Task;
import com.flipkart.flux.client.registry.Executable;
import com.flipkart.flux.constant.RuntimeConstants;
import com.flipkart.flux.deploymentunit.iface.ExecutableLoader;
import com.flipkart.flux.registry.TaskExecutableImpl;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * <code>ExecutableLoaderImpl</code> reads the deployment units and puts the methods
 * which are annotated with {@link com.flipkart.flux.client.model.Task} in Executable Registry for the later execution.
 *
 * @author gaurav.ashok
 */
public class ExecutableLoaderImpl implements ExecutableLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutableLoaderImpl.class);
    private final int defaultTaskExecutionConcurrency;

    @Inject
    public ExecutableLoaderImpl(@Named("routers.default.instancesPerNode") int instancesPerNode) {
        this.defaultTaskExecutionConcurrency = instancesPerNode;
    }

    @Override
    public Map<String, Executable> loadExecutables(DeploymentUnit deploymentUnit) {
        try {
            //get required classes from deployment unit class loader
            DeploymentUnitClassLoader classLoader = deploymentUnit.getDeploymentUnitClassLoader();
            Class taskClass = classLoader.loadClass(Task.class.getCanonicalName());

            Object objectMapperInstance = deploymentUnit.getObjectMapperInstance();
            Object injectorClassInstance = deploymentUnit.getInjectorClassInstance();
            Class injectorClass = injectorClassInstance.getClass();
            Method getInstanceMethod = injectorClass.getMethod("getInstance", Class.class);

            Map<String, Method> taskMethods = deploymentUnit.getTaskMethods();
            Configuration taskConfigs = deploymentUnit.getTaskConfiguration();

            Map<String, Executable> registry = new HashMap<>();

            //for every task method found in the deployment unit create an executable and keep it in executable registry
            for(String taskId : taskMethods.keySet()) {
                Method method = taskMethods.get(taskId);
                Annotation taskAnnotation = method.getAnnotationsByType(taskClass)[0];
                Class<? extends Annotation> annotationType = taskAnnotation.annotationType();
                long timeout = RuntimeConstants.defaultTaskTimeout;
                for (Method annotationMethod : annotationType.getDeclaredMethods()) {
                    Object value = annotationMethod.invoke(taskAnnotation, (Object[])null);
                    if(annotationMethod.getName().equals("timeout")) { //todo: find a way to get Task.timeout() name
                        timeout = (Long) value;
                    }
                }

                MethodId methodId = new MethodId(method);

                /* get concurrency config for this task */
                Integer taskExecConcurrency = Optional.ofNullable((Integer)taskConfigs.getProperty(methodId.getPrefix() + ".executionConcurrency"))
                        .orElse(defaultTaskExecutionConcurrency);

                Object singletonMethodOwner = getInstanceMethod.invoke(injectorClassInstance, method.getDeclaringClass());
                registry.put(taskId, new TaskExecutableImpl(singletonMethodOwner, method, timeout, taskExecConcurrency, classLoader, objectMapperInstance));
            }

            return registry;

        } catch (Exception e) {
            LOGGER.error("Unable to populate Executable Registry for deployment unit: {}. Exception: {}", deploymentUnit.getName(), e.getMessage());
            throw new FluxError(FluxError.ErrorType.runtime, "Unable to populate Executable Registry for deployment unit: " + deploymentUnit.getName(), e);
        }
    }
}
