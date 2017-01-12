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
import com.flipkart.flux.integration.IntegerEvent;
import com.flipkart.flux.integration.StringEvent;
import com.flipkart.polyguice.config.YamlConfiguration;

import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <code>DummyDeploymentUnitUtil</code> is a {@link DeploymentUnitUtil} implementation used in E2E tests to provide dummy deployment units.
 * @author shyam.akirala
 */
public class DummyDeploymentUnitUtil implements DeploymentUnitUtil {

    private static DeploymentUnitClassLoader deploymentUnitClassLoader = new DeploymentUnitClassLoader(new URL[0], DummyDeploymentUnitUtil.class.getClassLoader());

    @Override
    public List<Path> listAllDirectoryUnits() {
        return Collections.singletonList(Paths.get("Dummy_deployment_unit"));
    }

    @Override
    public DeploymentUnit getDeploymentUnit(Path directory) throws Exception {
        if(directory.toString().equals("dynamic1/1")) {
            return new DeploymentUnit("dynamic1", 1, deploymentUnitClassLoader, getProperties(deploymentUnitClassLoader, "flux_config_2.yml"));
        }
        return new DeploymentUnit("Dummy_deployment_unit", 1, deploymentUnitClassLoader, getProperties(deploymentUnitClassLoader, "flux_config.yml"));
    }

    public YamlConfiguration getProperties(DeploymentUnitClassLoader classLoader, String file) throws Exception {
        return new YamlConfiguration(this.getClass().getClassLoader().getResource(file));
    }
}
