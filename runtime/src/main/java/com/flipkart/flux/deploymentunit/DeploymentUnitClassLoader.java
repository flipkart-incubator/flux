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

import java.net.URL;
import java.net.URLClassLoader;

/**
 * <code>DeploymentUnitClassLoader</code> extends {@link java.net.URLClassLoader} and used by Flux runtime to create class loader for a Deployment Unit.
 * @author shyam.akirala
 */
public class DeploymentUnitClassLoader extends URLClassLoader {

    /** constructor */
    DeploymentUnitClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    /** Defines a Class from it's bytes */
    protected Class defineClass(String name, byte[] bytes) {
        return super.defineClass(name, bytes, 0, bytes.length);
    }

}
