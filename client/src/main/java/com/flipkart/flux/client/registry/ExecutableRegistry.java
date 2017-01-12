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

import java.lang.reflect.Method;

/**
 * The <code>ExecutableRegistry</code> provides an interface using which the core runtime can execute client code.
 * Executables currently include already observed task and hook methods.
 * The client adds to the registry as an when it encounters new executables, which then become available to the
 * core runtime for execution
 *
 * @author yogesh.nachnani
 * */
public interface ExecutableRegistry {
    /* Retrieve an executable corresponding to a task given the taskIdentifier */
    Executable getTask(String taskIdentifier);
    /* Retrieve an executable hook given the hookIdentifier */
    Method getHook(String hookIdentifier);
    /* register a task executable against a given identifier. This operation must be idempotent and thread safe */
    void registerTask(String taskIdentifier, Executable method);
    /* unregister task executable. Must be thread safe */
    void unregisterTask(String taskIdentifier);
}
