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

import com.flipkart.flux.client.model.Workflow;
import com.flipkart.polyguice.config.YamlConfiguration;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <code>ClassLoaderUtil</code> provides {@link URLClassLoader} for a particular client provided directory.
 * The constructed class loader is independent and doesn't share anything with System class loader.
 * @author shyam.akirala
 */
@Singleton
public class ClassLoaderUtil {

    private static final String CONFIG_FILE = "flux_config.yml";

    /** Key in the config file, which has list of workflow class FQNs*/
    private static final String WORKFLOWS = "workflows";

    /**
     * Builds and returns URLClassLoader for a provided directory.
     * This assumes the provided directory has the following structure.
     *
     * directory
     * |_____ main              //contains main jar
     * |_____ lib               //contains libraries on which main jar is depending
     * |_____ flux_config.yml   //properties file
     *
     * @param directory
     * @return URLClassLoader
     * @throws MalformedURLException
     */
    public URLClassLoader getClassLoader(String directory) throws MalformedURLException {

        if(!directory.endsWith("/"))
            directory = directory + "/";

        File[] mainJars = new File(directory+"main").listFiles();
        File[] libJars = new File(directory+"lib").listFiles();

        if(mainJars == null)
            throw new RuntimeException("Unable to build class loader. Required directory "+directory+"main is empty.");

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
     * Given a class loader, retrieves workflow classes names from config file, and returns methods
     * which are annotated with {@link com.flipkart.flux.client.model.Workflow} annotation in those classes.
     * @param urlClassLoader
     * @return set of Methods
     * @throws ClassNotFoundException
     */
    public Set<Method> getWorkflowMethods(URLClassLoader urlClassLoader) throws ClassNotFoundException, IOException {

        YamlConfiguration yamlConfiguration = new YamlConfiguration(urlClassLoader.getResource(CONFIG_FILE));
        List<String> classNames = (List<String>) yamlConfiguration.getProperty(WORKFLOWS);
        Set<Method> methods = new HashSet<>();

        //loading this class separately in this class loader as the following isAnnotationPresent check returns false, if
        //we use default class loader's Workflow, as both class loaders don't have any relation between them.
        Class workflowClass = urlClassLoader.loadClass(Workflow.class.getCanonicalName());

        for(String name : classNames) {
            Class clazz = urlClassLoader.loadClass(name);

            for(Method method : clazz.getMethods()) {
                if(method.isAnnotationPresent(workflowClass))
                    methods.add(method);
            }
        }

        return methods;
    }

}