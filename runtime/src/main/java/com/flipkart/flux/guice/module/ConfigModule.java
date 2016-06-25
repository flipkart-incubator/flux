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

package com.flipkart.flux.guice.module;

import com.flipkart.flux.deploymentunit.DeploymentUnitUtil;
import com.flipkart.flux.deploymentunit.ExecutableRegistryPopulator;
import com.flipkart.polyguice.config.ApacheCommonsConfigProvider;
import com.flipkart.polyguice.config.YamlConfiguration;
import com.flipkart.polyguice.core.ConfigurationProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.name.Names;
import org.apache.commons.configuration.Configuration;
import org.eclipse.jetty.util.ConcurrentHashSet;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;

/**
 * <code>ConfigModule</code> is a Guice {@link AbstractModule} implementation used for wiring flux configuration.
 * @author kartik.bommepally
 */
public class ConfigModule extends AbstractModule {

    private URL configUrl;
    private final ConfigurationProvider configProvider;
    private final YamlConfiguration yamlConfiguration;

    public ConfigModule(URL configUrl) {
        try {
            this.configUrl = configUrl;
            configProvider = new ApacheCommonsConfigProvider().location(configUrl.getPath());
            yamlConfiguration = new YamlConfiguration(configUrl);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void configure() {
        bind(ConfigurationProvider.class).toInstance(configProvider);
        bindConfigProperties();
    }

    /**
     * Binds individual flattened key-value properties in the configuration yml
     * file. So one can directly inject something like this:
     * @Named("Hibernate.hibernate.jdbcDriver") String jdbcDriver OR
     * @Named("Dashboard.service.port") int port
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void bindConfigProperties() {
        bind(YamlConfiguration.class).toInstance(yamlConfiguration);
        Iterator<String> propertyKeys = yamlConfiguration.getKeys();
        while (propertyKeys.hasNext()) {
            String propertyKey = propertyKeys.next();
            Object propertyValue = yamlConfiguration.getProperty(propertyKey);
            LinkedBindingBuilder annotatedWith = bind(propertyValue.getClass()).annotatedWith(Names.named(propertyKey));
            annotatedWith.toInstance(propertyValue);
        }
    }

    public ConfigurationProvider getConfigProvider() {
        return configProvider;
    }

    /** Returns set of all the available routers, include routers mentioned in configuration.yml and deployment unit routers*/
    @Provides
    @Singleton
    @Named("router.names")
    public Set<String> getRouterNames(DeploymentUnitUtil deploymentUnitUtil, ExecutableRegistryPopulator executableRegistryPopulator) {
        Configuration routerConfig = yamlConfiguration.subset("routers");
        Set<String> routerNames = new ConcurrentHashSet<>();
        Iterator<String> propertyKeys = routerConfig.getKeys();
        while(propertyKeys.hasNext()) {
            String propertyKey = propertyKeys.next();
            String routerName = propertyKey.substring(0, propertyKey.lastIndexOf('.'));
            routerNames.add(routerName);
        }

        //add all deployment units' routers
        routerNames.addAll(executableRegistryPopulator.getDeploymentUnitRouterNames());

        //'default' is not a router name, it's a placeholder for router default settings
        routerNames.remove("default");

        return routerNames;
    }

}
