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

import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.Set;

/**
 * <code>DeploymentUnit</code> represents a Deployment Unit.
 * @author shyam.akirala
 */
public class DeploymentUnit {

    /** Name of the deployment unit */
    private String name;

    /** Class loader of the deployment unit*/
    private URLClassLoader urlClassLoader;

    /** Tasks which belong to the deployment unit*/
    private Set<Method> taskMethods;

    public DeploymentUnit(String name, URLClassLoader urlClassLoader, Set<Method> taskMethods) {
        this.name = name;
        this.urlClassLoader = urlClassLoader;
        this.taskMethods = taskMethods;
    }

    public String getName() {
        return name;
    }

    public URLClassLoader getUrlClassLoader() {
        return urlClassLoader;
    }

    public Set<Method> getTaskMethods() {
        return taskMethods;
    }
}
