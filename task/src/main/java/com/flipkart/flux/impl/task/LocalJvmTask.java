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
 *
 */

package com.flipkart.flux.impl.task;

import com.flipkart.flux.api.EventData;
import com.flipkart.flux.api.core.FluxError;
import com.flipkart.flux.client.registry.Executable;
import com.flipkart.flux.registry.TaskExecutableImpl;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * A task that can be executed locally within the same JVM
 * @author yogesh.nachnani
 * @author shyam.akirala
 */
public class LocalJvmTask extends AbstractTask {

    private final Executable toInvoke;

    private static final Logger logger = LoggerFactory.getLogger(LocalJvmTask.class);

    public LocalJvmTask(Executable toInvoke) {
        this.toInvoke = toInvoke;
    }

    @Override
    public String getName() {
        return this.toInvoke.getName();
    }

    @Override
    public String getTaskGroupName() {
        /* TODO - pull from deployment unit */
        return "flux";
    }

    @Override
    public int getExecutionConcurrency() {
        return ((TaskExecutableImpl)toInvoke).getExecutionConcurrency();
    }

    @Override
    public int getExecutionTimeout() {
        return (int)toInvoke.getTimeout(); // TODO - fix this. Let all timeouts be in int
    }

    @Override
    public Pair<Object, FluxError> execute(EventData[] events) {
        Object[] parameters = new Object[events.length];
        Class<?>[] parameterTypes = toInvoke.getParameterTypes();
        try {
            ClassLoader classLoader = ((TaskExecutableImpl)toInvoke).getDeploymentUnitClassLoader();
            Object objectMapperInstance = ((TaskExecutableImpl)toInvoke).getObjectMapperInstance();
            Class objectMapper = objectMapperInstance.getClass();

			for (int i = 0; i < parameterTypes.length; i++) {
				if (parameterTypes[i].isAssignableFrom(Class.forName(events[i].getType(), true, classLoader))) {
					parameters[i] = objectMapper.getMethod("readValue", String.class, Class.class).invoke(objectMapperInstance, events[i].getData(),Class.forName(events[i].getType(), true, classLoader));
				} else {
					logger.warn("Parameter type {} did not match with event: {}", parameterTypes[i], events[i]);
					throw new RuntimeException(
							"Parameter type " + parameterTypes[i] + " did not match with event: " + events[i]);
				}
			}

            Method writeValueAsString = objectMapper.getMethod("writeValueAsString", Object.class);

            SerializedEvent serializedEvent = null;

            try {
                //set current thread contextClassLoader to Deployment Unit classloader
                Thread.currentThread().setContextClassLoader(((TaskExecutableImpl) toInvoke).getDeploymentUnitClassLoader());

                final Object returnObject = toInvoke.execute(parameters);
                if (returnObject != null) {
                    serializedEvent = new SerializedEvent(returnObject.getClass().getCanonicalName(), (String) writeValueAsString.invoke(objectMapperInstance, returnObject));
                }
            } finally {
                //reset the current thread contextClassLoader to Flux app class loader
                Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            }

            return new Pair<>(serializedEvent, null);

        } catch (Exception e) {
            logger.error("Bad things happened while trying to execute {}",toInvoke,e);
            return new Pair<>(null,new FluxError(FluxError.ErrorType.runtime,e.getMessage(),e));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocalJvmTask that = (LocalJvmTask) o;

        return toInvoke.equals(that.toInvoke);

    }

    @Override
    public int hashCode() {
        return toInvoke.hashCode();
    }
}
