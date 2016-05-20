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

    ConfigurationProvider configurationProvider;

    @Before
    public void setUp() throws Exception {

    }

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