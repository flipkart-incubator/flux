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
 *
 */

package com.flipkart.flux.impl.task.registry;

import akka.cluster.routing.ClusterRouterPoolSettings;
import com.flipkart.polyguice.core.ConfigurationProvider;
import javafx.util.Pair;
import org.junit.Before;
import org.junit.Test;
import scala.Option;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class LocalRouterConfigurationRegistryImplTest {
    @Test
    public void testGetConfigurations_shouldReadConfigsFromConfigProvider() throws Exception {
        final LocalRouterConfigurationRegistryImpl localRouterConfigurationRegistry = new LocalRouterConfigurationRegistryImpl(
            new HashSet<String>() {{
                add("foo");
            }},
            setupConfigProvider(new HashMap<String, Object>() {{
                put("routers.foo.totalInstances", 1);
                put("routers.foo.maxInstancesPerNode", 1);
            }}));

        assertThat(localRouterConfigurationRegistry.getConfigurations()).containsExactly(new Pair<>("foo",new ClusterRouterPoolSettings(1,1,true, Option.empty())));
    }

    @Test
    public void testGetConfigurations_shouldUseDefaultConfigurationsIfNotExplicitlyProvided() throws Exception {
        final LocalRouterConfigurationRegistryImpl localRouterConfigurationRegistry = new LocalRouterConfigurationRegistryImpl(
            new HashSet<String>() {{
                add("foo");
            }},
            setupConfigProvider(new HashMap<String, Object>() {{
                put("routers.default.totalInstances", 1);
                put("routers.default.maxInstancesPerNode", 1);
            }}));
        localRouterConfigurationRegistry.initialize();
        assertThat(localRouterConfigurationRegistry.getConfigurations()).containsExactly(new Pair<>("foo",new ClusterRouterPoolSettings(1,1,true, Option.empty())));
    }

    @Test(expected = ConfigurationException.class)
    public void testInit_shouldBombIfDefaultConfigsNotPresent() throws Exception {
        final LocalRouterConfigurationRegistryImpl localRouterConfigurationRegistry = new LocalRouterConfigurationRegistryImpl(
            new HashSet<String>() {{
                add("foo");
            }},
            setupConfigProvider(new HashMap<String, Object>() {{
                put("routers.foo.totalInstances", 1);
                put("routers.foo.maxInstancesPerNode", 1);
            }}));
        localRouterConfigurationRegistry.initialize();
    }

    private ConfigurationProvider setupConfigProvider(final Map<String,Object> configMap) {
        return new ConfigurationProvider() {
            @Override
            public boolean contains(String s) {
                return configMap.containsKey(s);
            }

            @Override
            public Object getValue(String s, Class<?> aClass) {
                return configMap.get(s);
            }
        };
    }
}