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

package com.flipkart.flux.deploymentunit.iface;

import com.flipkart.flux.deploymentunit.DeploymentUnit;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

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

    /** Lists the paths of all deployment units available */
    List<Path> listAllDirectoryUnits() throws IOException;

    DeploymentUnit getDeploymentUnit(Path directory) throws Exception;
}
