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

import com.flipkart.polyguice.config.YamlConfiguration;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

/**
 * <code>DeploymentUnitUtil</code> performs operations on deployment units.
 * The implementation for this class attached using configuration.yml's key deploymentType.
 * Ex:
 *      deploymentType: directory
 *
 * the above binds DirectoryBasedDeploymentUnitUtil.
 *
 * @author shyam.akirala
 */
public interface DeploymentUnitUtil {

    /** Lists all deployment units available*/
    List<String> getAllDeploymentUnitNames();

    /** Returns the class loader for passed deployment unit*/
    DeploymentUnitClassLoader getClassLoader(String deploymentUnitName) throws Exception;

    /** Returns set of methods which are annotated with @Task in the passed deployment unit*/
    Set<Method> getTaskMethods(DeploymentUnitClassLoader classLoader) throws Exception;

    /** Reads flux_config.yml and returns YamlConfiguration object for the passed deployment unit*/
    YamlConfiguration getProperties(DeploymentUnitClassLoader classLoader) throws Exception;

}
