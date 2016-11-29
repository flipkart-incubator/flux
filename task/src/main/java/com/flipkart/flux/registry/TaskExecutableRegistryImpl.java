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

import com.flipkart.flux.client.registry.Executable;
import com.flipkart.flux.client.registry.ExecutableRegistry;

import javax.inject.Singleton;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <code>TaskExecutableRegistryImpl</code> is a {@link com.flipkart.flux.client.registry.ExecutableRegistry} implementation used in flux managed environment,
 * to store task methods for later execution.
 *
 * @author shyam.akirala
 */
@Singleton
public class TaskExecutableRegistryImpl implements ExecutableRegistry {

    private final Map<String, Executable> identifierToMethodMap;

    public TaskExecutableRegistryImpl() {
        this.identifierToMethodMap = new ConcurrentHashMap<>();
    }

    @Override
    public Executable getTask(String taskIdentifier) {
        final Executable cachedExecutable = this.identifierToMethodMap.get(taskIdentifier);
        if (cachedExecutable == null) {
            throw new TaskNotFoundException("Did not find task identifier:" + taskIdentifier+" in identifierToMethodMap, seems it didn't get registered. Something terribly went wrong!");
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