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
import com.flipkart.flux.domain.Event;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * A task that can be executed locally within the same JVM
 * @author yogesh.nachnani
 * */
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
        /* TODO - pull from deployment unit/ client definition */
        return 10;
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
            /* TODO
            While this works for methods with all unique param types, it
            will fail for methods where we have mutliple params of the same type.
             */
            ClassLoader classLoader = toInvoke.getDeploymentUnitClassLoader() != null ? toInvoke.getDeploymentUnitClassLoader() : this.getClass().getClassLoader();
            Class objectMapper = classLoader.loadClass("com.fasterxml.jackson.databind.ObjectMapper");
            Object objectMapperInstance = objectMapper.newInstance();

            for (int i = 0 ; i < parameterTypes.length ; i++) {
                for (EventData anEvent : events) {
                    if(Class.forName(anEvent.getType(), true, classLoader).equals(parameterTypes[i])) {
                        parameters[i] = objectMapper.getMethod("readValue", String.class, Class.class).invoke(objectMapperInstance, anEvent.getData(), Class.forName(anEvent.getType(), true, classLoader));
                    }
                }
                if (parameters[i] == null) {
                    logger.warn("Could not find a paramter of type {} in event list {}",parameterTypes[i],events);
                    throw new RuntimeException("Could not find a paramter of type " + parameterTypes[i]);
                }
            }

            Method writeValueAsString = objectMapper.getMethod("writeValueAsString", Object.class);

            final Object returnObject = toInvoke.execute(parameters);
            Event returnEvent = null;
            if(returnObject!=null)
                returnEvent = new Event(null, returnObject.getClass().getCanonicalName(), null, null, (String) writeValueAsString.invoke(objectMapperInstance, returnObject), null);
            return new Pair<>(returnEvent,null);
        } catch (Exception e) {
            logger.warn("Bad things happened while trying to execute {}",toInvoke,e);
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
