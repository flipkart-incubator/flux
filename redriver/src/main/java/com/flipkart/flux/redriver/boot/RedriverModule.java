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

package com.flipkart.flux.redriver.boot;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.flipkart.flux.persistence.DataSourceType;
import com.flipkart.flux.persistence.SessionFactoryContext;
import com.flipkart.flux.guice.interceptor.TransactionInterceptor;
import com.flipkart.flux.persistence.impl.SessionFactoryContextImpl;
import com.flipkart.flux.redriver.dao.MessageDao;
import com.flipkart.flux.redriver.model.ScheduledMessage;
import com.flipkart.polyguice.config.YamlConfiguration;
import com.google.inject.*;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import javax.inject.Named;
import javax.inject.Provider;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import static com.flipkart.flux.Constants.METRIC_REGISTRY_NAME;

/**
 * Keeping a single module for redriver component for now
 * It may need a split in the future
 * Presently, it is assumed that {@link TransactionInterceptor} is already configured by another module
 */
public class RedriverModule extends AbstractModule {
    private static final String FLUX_REDRIVER_HIBERNATE_CONFIG_NAME_SPACE = "flux_redriver.Hibernate";

    @Override
    protected void configure() {
        Provider<SessionFactoryContext> provider = getProvider(Key.get(SessionFactoryContext.class, Names.named("redriverSessionFactoryContext")));
        bindInterceptor(Matchers.inPackage(MessageDao.class.getPackage()), Matchers.annotatedWith(Transactional.class), new TransactionInterceptor(provider));
    }

    @Provides
    public MetricRegistry metricRegistry() {
        return SharedMetricRegistries.getOrCreate(METRIC_REGISTRY_NAME);
    }

    /**
     * Creates hibernate configuration from the configuration yaml properties.
     * Since the yaml properties are already flattened in input param <code>yamlConfiguration</code>
     * the method loops over them to selectively pick Hibernate specific properties.
     */
    @Provides
    @Singleton
    @Named("redriverHibernateConfiguration")
    public Configuration getConfiguration(YamlConfiguration yamlConfiguration) {
        Configuration configuration = new Configuration();
        addAnnotatedClassesAndTypes(configuration);
        org.apache.commons.configuration.Configuration hibernateConfig = yamlConfiguration.subset(FLUX_REDRIVER_HIBERNATE_CONFIG_NAME_SPACE);
        Iterator<String> propertyKeys = hibernateConfig.getKeys();
        Properties configProperties = new Properties();
        while (propertyKeys.hasNext()) {
            String propertyKey = propertyKeys.next();
            Object propertyValue = hibernateConfig.getProperty(propertyKey);
            configProperties.put(propertyKey, propertyValue);
        }
        configuration.addProperties(configProperties);
        return configuration;
    }

    @Provides
    @Singleton
    @Named("redriverSessionFactoryContext")
    public SessionFactoryContext getSessionFactoryProvider(@Named("redriverHibernateConfiguration") Configuration configuration) {
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        Map<DataSourceType, SessionFactory> map = new HashMap<>();
        map.put(DataSourceType.READ_WRITE, sessionFactory);
        return new SessionFactoryContextImpl(map, DataSourceType.READ_WRITE);
    }

    private void addAnnotatedClassesAndTypes(Configuration configuration) {
        configuration.addAnnotatedClass(ScheduledMessage.class);
    }
}
