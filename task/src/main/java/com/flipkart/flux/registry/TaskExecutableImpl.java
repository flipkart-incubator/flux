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

package com.flipkart.flux.registry;

import com.flipkart.flux.client.intercept.MethodId;
import com.flipkart.flux.client.registry.Executable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;

/**
 * <code>TaskExecutableImpl</code> is a {@link com.flipkart.flux.client.registry.Executable} implementation
 * used in flux managed environment to provide a way to execute client code. All the task methods are stored
 * in {@link com.flipkart.flux.client.registry.ExecutableRegistry} as <taskIdentifier, Executable> which will be
 * retrieved and executed.
 *
 * @author shyam.akirala
 */
public class TaskExecutableImpl implements Executable {

    /** Task method which is annotated with {@link com.flipkart.flux.client.model.Task} */
    private final Method toInvoke;

    /** Object of the class which is used to invoke the above method 'toInvoke' */
    private final Object singletonMethodOwner;

    /** timeout of the task */
    private final long timeout;

    /** max allowed no. of concurrently running instances of this task per node */
    private final int executionConcurrency;

    /** Singleton ObjectMapper instance of the deployment unit */
    private final Object objectMapperInstance;

    /** Class loader of the deployment unit to which 'toInvoke' belongs */
    private final URLClassLoader deploymentUnitClassLoader;

    public TaskExecutableImpl(Object singletonMethodOwner, Method toInvoke, long timeout, int executionConcurrency, URLClassLoader classLoader, Object objectMapperInstance) {
        this.singletonMethodOwner = singletonMethodOwner;
        this.toInvoke = toInvoke;
        this.timeout = timeout;
        this.executionConcurrency = executionConcurrency;
        this.deploymentUnitClassLoader = classLoader;
        this.objectMapperInstance = objectMapperInstance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaskExecutableImpl that = (TaskExecutableImpl) o;

        if (timeout != that.timeout) return false;
        if (!toInvoke.equals(that.toInvoke)) return false;
        return singletonMethodOwner.equals(that.singletonMethodOwner);

    }

    @Override
    public int hashCode() {
        int result = toInvoke.hashCode();
        result = 31 * result + singletonMethodOwner.hashCode();
        result = 31 * result + (int) (timeout ^ (timeout >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Executable{" +
                "singletonMethodOwner=" + singletonMethodOwner +
                ", toInvoke=" + toInvoke +
                '}';
    }

    public String getName() {
        return new MethodId(toInvoke).getPrefix();
    }

    public long getTimeout() {
        return timeout;
    }

    public int getExecutionConcurrency() {
        return executionConcurrency;
    }

    @Override
    public Object execute(Object[] parameters) {
        try {
            return toInvoke.invoke(singletonMethodOwner,parameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return toInvoke.getParameterTypes();
    }

    public URLClassLoader getDeploymentUnitClassLoader() {
        return deploymentUnitClassLoader;
    }

    public Object getObjectMapperInstance() {
        return objectMapperInstance;
    }
}
