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

import com.flipkart.flux.deploymentunit.iface.DeploymentUnitUtil;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * <code>NoOpDeploymentUnitUtil</code> is a {@link DeploymentUnitUtil} implementation to represent No operations.
 * Used in flux isolation mode as there is no deployment unit concept in it.
 *
 * @author shyam.akirala
 */
public class NoOpDeploymentUnitUtil implements DeploymentUnitUtil {
    @Override
    public List<Path> listAllDirectoryUnits() {
        return new ArrayList<>();
    }

    @Override
    public DeploymentUnit getDeploymentUnit(Path path) {
        return null;
    }
}
