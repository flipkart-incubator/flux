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

import com.flipkart.flux.client.model.Task;
import com.flipkart.flux.client.model.Workflow;
import com.flipkart.polyguice.config.YamlConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <code>DeploymentUnitUtil</code> performs operations on deployment units.
 * Ex:
 * Creating {@link URLClassLoader} for a deployment unit.
 * Scanning and returning methods annotated with {@link Workflow} annotation.
 * @author shyam.akirala
 */
public class DeploymentUnitUtil {

    private static final String CONFIG_FILE = "flux_config.yml";

    /** Key in the config file, which has list of workflow class FQNs*/
    private static final String WORKFLOWS = "workflows";

    public static List<String> getAllDeploymentUnits() {
        //TODO: IMPLEMENT IT.  Probably get the list from config service or scan the directory structure
//        throw new UnsupportedOperationException("Implement me");
        return new ArrayList<String>();
    }

    /**
     * Provides {@link java.net.URLClassLoader} for the given directory.
     * @param directory
     * @return class loader
     * @throws MalformedURLException
     */
    public static URLClassLoader getClassLoader(String directory) throws MalformedURLException {
        return ClassLoaderProvider.getClassLoader(directory);
    }

    /**
     * Given a class loader, retrieves workflow classes names from config file, and returns methods
     * which are annotated with {@link com.flipkart.flux.client.model.Workflow} annotation in those classes.
     * @param urlClassLoader
     * @return set of Methods
     * @throws ClassNotFoundException
     */
    public static Set<Method> getWorkflowMethods(URLClassLoader urlClassLoader) throws ClassNotFoundException, IOException {

        YamlConfiguration yamlConfiguration = getProperties(urlClassLoader);
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

    /**
     * Given a class loader, retrieves workflow classes names from config file, and returns methods
     * which are annotated with {@link com.flipkart.flux.client.model.Task} annotation in those classes.
     * @param urlClassLoader
     * @return set of Methods
     * @throws ClassNotFoundException
     */
    public static Set<Method> getTaskMethods(URLClassLoader urlClassLoader) throws ClassNotFoundException, IOException {

        YamlConfiguration yamlConfiguration = getProperties(urlClassLoader);
        List<String> classNames = (List<String>) yamlConfiguration.getProperty(WORKFLOWS);
        Set<Method> methods = new HashSet<>();

        //loading this class separately in this class loader as the following isAnnotationPresent check returns false, if
        //we use default class loader's Task, as both class loaders don't have any relation between them.
        Class taskClass = urlClassLoader.loadClass(Task.class.getCanonicalName());

        for(String name : classNames) {
            Class clazz = urlClassLoader.loadClass(name);

            for(Method method : clazz.getMethods()) {
                if(method.isAnnotationPresent(taskClass))
                    methods.add(method);
            }
        }

        return methods;
    }

    /**
     * Given deployment unit directory name retrieves properties from config file and returns them.
     * @param directory
     * @return
     * @throws IOException
     */
    public static YamlConfiguration getProperties(String directory) throws IOException {
        URLClassLoader classLoader = getClassLoader(directory);
        return getProperties(classLoader);
    }

    /**
     * Given deployment unit directory name retrieves properties from config file and returns them.
     * @param urlClassLoader
     * @return YamlConfiguration
     * @throws IOException
     */
    public static YamlConfiguration getProperties(URLClassLoader urlClassLoader) throws IOException, FileNotFoundException {
        YamlConfiguration yamlConfiguration;
        try {
            yamlConfiguration = new YamlConfiguration(urlClassLoader.getResource(CONFIG_FILE));
        } catch (NullPointerException e) {
            throw new FileNotFoundException(CONFIG_FILE+" is either not found or it's empty");
        }
        return yamlConfiguration;
    }

    /**
     * Provides {@link java.net.URLClassLoader for a given directory}
     */
    static class ClassLoaderProvider {

        /**
         * Builds and returns URLClassLoader for a provided directory.
         * This assumes the provided directory has the following structure.
         *
         * directory
         * |_____ main              //contains main jar
         * |_____ lib               //contains libraries on which main jar is depending
         * |_____ flux_config.yml   //properties file
         *
         * The constructed class loader is independent and doesn't share anything with System class loader.
         * @param directory
         * @return URLClassLoader
         * @throws java.net.MalformedURLException
         */
        static public URLClassLoader getClassLoader(String directory) throws MalformedURLException {

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
    }
}