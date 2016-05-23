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

package com.flipkart.flux.loader;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;

import javax.inject.Singleton;
import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;

/**
 * <code>ClassLoaderUtil</code> provides methods to do operations on {@link URLClassLoader}.
 * Operations
 * 1. to build and provide {@link URLClassLoader} instance for a provided directory.
 * 2. get methods annotated with a particular annotation.
 * @author shyam.akirala
 */
@Singleton
public class ClassLoaderUtil {

    /**
     * Builds and returns URLClassLoader for a provided directory.
     * This assumes the provided directory has the following structure.
     *
     * directory
     * |_____ jar   //contains main jar
     * |_____ lib   //contains libraries on which main jar is depending
     * |_____ *.yml //properties files
     *
     * @param directory
     * @return URLClassLoader
     * @throws MalformedURLException
     */
    public URLClassLoader getClassLoader(String directory) throws MalformedURLException {

        if(!directory.endsWith("/"))
            directory = directory + "/";

        File[] mainJars = new File(directory+"jar").listFiles();
        File[] libJars = new File(directory+"lib").listFiles();

        if(mainJars == null)
            throw new RuntimeException("Unable to build class loader. Required directory <state_machine_path>/jar is empty.");

        URL[] urls = new URL[mainJars.length + (libJars != null ? libJars.length : 0) + 1];
        int urlIndex = 0;

        for (File mainJar : mainJars) {
            if (mainJar.isFile()) {
                urls[urlIndex++] = mainJar.toURI().toURL();
            }
        }

        if(libJars != null) {
            for (File libJar : libJars) {
                if (libJar.isFile()) {
                    urls[urlIndex++] = libJar.toURI().toURL();
                }
            }
        }

        urls[urlIndex] = new File(directory).toURI().toURL();

        return new URLClassLoader(urls, null);
    }

    /**
     * Searches and returns all the methods which are annotated with annotationClass in the provided class loader.
     * @param classLoader
     * @param annotationClass
     * @return Set of methods annotated with annotationClass
     */
    public Set<Method> getMethodsAnnotatedWithWorkflow(URLClassLoader classLoader, Class annotationClass) {
        Reflections reflections = new Reflections(new MethodAnnotationsScanner(), classLoader);
        return reflections.getMethodsAnnotatedWith(annotationClass);
    }

}
