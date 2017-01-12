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

package com.flipkart.flux.client.registry;

import com.flipkart.flux.client.intercept.MethodId;
import com.flipkart.flux.client.intercept.UnknownIdentifierException;
import com.flipkart.flux.client.model.Task;
import com.google.inject.Injector;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is used to retrieve executables stored locally in the same JVM
 * @author yogesh.nachnani
 */
@Singleton
public class LocalExecutableRegistryImpl implements ExecutableRegistry {
    private final Map<String, Executable> identifierToMethodMap;
    private final Injector injector;

    @Inject
    public LocalExecutableRegistryImpl(Injector injector) {
        this(new ConcurrentHashMap<>(),injector);
    }

    public LocalExecutableRegistryImpl(Map<String, Executable> identifierToMethodMap, Injector injector) {
        this.identifierToMethodMap = identifierToMethodMap  ;
        this.injector = injector;
    }

    /**
     * The <code>LocalExecutableRegistryImpl</code> stores registered tasks in a concurrent map structure
     * If the given entry is not in the map strucuture, this implementation looks for the given class and tries to retrieve
     * it from the guice injector.
     * @param taskIdentifier- String that identifies a task to be executed
     * @return The executable method that can be invoked - either one that is previously registered or one that is loaded dynamically in the JVM
     */
    @Override
    public Executable getTask(String taskIdentifier) {
        final Executable cachedExecutable = this.identifierToMethodMap.get(taskIdentifier);
        if (cachedExecutable == null) {
            try {
                //taskIdentifier would be of form methodIdentifier_version<no>, so removing _version<no> and passing the remaining string
                final MethodId methodId = MethodId.fromIdentifier(taskIdentifier.substring(0, taskIdentifier.lastIndexOf('_')));

                final Class<?> clazz = Class.forName(methodId.getClassName());
                final Object classInstance = this.injector.getInstance(clazz);
                final Method methodToInvoke = clazz.getDeclaredMethod(methodId.getMethodName(), methodId.getParameterTypes());
                final Task taskAnnotation = methodToInvoke.getAnnotationsByType(Task.class)[0];
                return new ExecutableImpl(classInstance, methodToInvoke, taskAnnotation.timeout());
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                throw new UnknownIdentifierException("Could not load method corresponding to the given task identifier:" + taskIdentifier);
            }
        }
        return cachedExecutable;
    }

    @Override
    public Method getHook(String hookIdentifier) {
        return null;
    }

    @Override
    public void registerTask(String taskIdentifier, Executable method) {
        this.identifierToMethodMap.put(taskIdentifier,method);
    }

    @Override
    public void unregisterTask(String taskIdentifier) {
        this.identifierToMethodMap.remove(taskIdentifier);
    }
}
