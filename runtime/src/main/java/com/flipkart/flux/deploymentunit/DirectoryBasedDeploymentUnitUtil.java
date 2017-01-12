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
import com.flipkart.flux.deploymentunit.iface.DeploymentUnitUtil;
import com.flipkart.polyguice.config.YamlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <code>DeploymentUnitUtil</code> is a {@link DeploymentUnitUtil} implementation which does operations
 * on deployment units assuming they are present in this machine as directories.
 * If you want to use this implementation append the below keys in configuration.yml file
 *
 * deploymentType: directory
 * deploymentUnitsPath: "/tmp/workflows/"   #location on this machine where all deployment units reside
 *
 * @author shyam.akirala
 */
public class DirectoryBasedDeploymentUnitUtil implements DeploymentUnitUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryBasedDeploymentUnitUtil.class);

    /** Configuration file of a deployment unit*/
    private static final String CONFIG_FILE = "flux_config.yml";

    /** Location of deployment units on the system */
    private String deploymentUnitsPath;

    public DirectoryBasedDeploymentUnitUtil(String deploymentUnitsPath) {
        this.deploymentUnitsPath = deploymentUnitsPath;
    }

    /** Lists all deployment unit names by directory scanning */
    public List<Path> listAllDirectoryUnits() throws IOException {
        if(deploymentUnitsPath != null) {
            return getSubDirectories(Paths.get(deploymentUnitsPath)).map(this::getSubDirectories).
                    flatMap(t -> t).collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public DeploymentUnit getDeploymentUnit(Path path) throws ClassNotFoundException, IOException, NumberFormatException {
        if(!path.isAbsolute()) {
            path = Paths.get(deploymentUnitsPath, path.toString());
        }
        int nameCount = path.getNameCount();
        String deploymentUnitName = path.getName(nameCount - 2).toString();
        Integer version = Integer.parseInt(path.getName(nameCount - 1).toString());

        DeploymentUnitClassLoader deploymentUnitClassLoader = ClassLoaderProvider.getClassLoader(path);
        YamlConfiguration configuration = getProperties(deploymentUnitClassLoader);

        return new DeploymentUnit(deploymentUnitName, version, deploymentUnitClassLoader, configuration);
    }

    /**
     * Given a deployment unit classloader retrieves properties from config file and returns them.
     * @param classLoader
     * @return YamlConfiguration
     * @throws IOException
     */
    private YamlConfiguration getProperties(DeploymentUnitClassLoader classLoader) throws IOException {
        return new YamlConfiguration(classLoader.getResource(CONFIG_FILE));
    }

    /**
     * Helper method to list sub-directories.
     * @param path
     * @return {@code Stream<Path>} representing sub-directories
     */
    private Stream<Path> getSubDirectories(Path path) {
        try {
            return Files.list(path).filter(e -> Files.isDirectory(e));
        }
        catch (IOException ioe) {
            return Stream.empty();
        }
    }


    /**
     * Provides {@link DeploymentUnitClassLoader for a given deployment unit}
     */
    static class ClassLoaderProvider {

        /**
         * Builds and returns DeploymentUnitClassLoader for a provided deployment unit.
         * This assumes the provided deployment unit directory has the following structure.
         *
         * DeploymentUnit
         * |_____ main              //contains main jar
         * |_____ lib               //contains libraries on which main jar is depending
         * |_____ flux_config.yml   //properties file
         *
         * The constructed class loader is independent and doesn't share anything with System class loader.
         * @param path directory of deploymentUnit
         * @return DeploymentUnitClassLoader
         * @throws FileNotFoundException
         */
        public static DeploymentUnitClassLoader getClassLoader(Path path) throws FileNotFoundException {

            Path mainPath = path.resolve("main");
            Path libPath = path.resolve("lib");
            Path configFilePath = path.resolve(CONFIG_FILE);

            List<Path> mainJars = Collections.EMPTY_LIST;
            try {
                mainJars = Files.list(mainPath).collect(Collectors.toList());
            }
            catch (IOException e) { /* consume it here, proper exception is thrown later */}

            if(mainJars.size() == 0) {
                throw new FileNotFoundException("Unable to build class loader. Required directory " + mainPath + " is empty/not present");
            }

            List<Path> libJars = Collections.EMPTY_LIST;
            try {
                libJars = Files.list(libPath).collect(Collectors.toList());
            }
            catch (IOException e) {/* lib folder is optional, so ignore this exception */}

            if(!Files.isRegularFile(configFilePath)) {
                throw new FileNotFoundException("Unable to build class loader. Config file not found");
            }

            URL[] urls = Stream.concat(Stream.concat(mainJars.stream(), libJars.stream()), Stream.of(path)).
                    map(e -> {
                        try {
                            return e.toUri().toURL();
                        } catch (MalformedURLException ex) {
                            /* This exception will not occur. Logging it in any case. */
                            LOGGER.error("Unexpected malformedURL exception in path.toURL.", ex);
                        }
                        /* unreachable */
                        return null;
                    }).toArray(size -> new URL[size]);

            return new DeploymentUnitClassLoader(urls, null);
        }
    }
}