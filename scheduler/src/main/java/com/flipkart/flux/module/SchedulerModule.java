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

package com.flipkart.flux.module;

import com.flipkart.flux.eventscheduler.dao.EventSchedulerDao;
import com.flipkart.flux.eventscheduler.model.ScheduledEvent;
import com.flipkart.flux.guice.interceptor.TransactionInterceptor;
import com.flipkart.flux.persistence.SessionFactoryContext;
import com.flipkart.flux.persistence.impl.SessionFactoryContextImpl;
import com.flipkart.flux.redriver.dao.MessageDao;
import com.flipkart.flux.redriver.model.ScheduledMessage;
import com.flipkart.polyguice.config.YamlConfiguration;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import javax.inject.Provider;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * <code>SchedulerModule</code> is a Guice {@link AbstractModule} which binds all Scheduler related stuff.
 *
 * @author amitkumar.o
 * @author shyam.akirala
 */
public class SchedulerModule extends AbstractModule {
    private static final String FLUX_SCHEDULER_HIBERNATE_CONFIG_NAME_SPACE = "flux_scheduler.Hibernate";

    @Override
    protected void configure() {
        Provider<SessionFactoryContext> provider = getProvider(Key.get(SessionFactoryContext.class, Names.named("schedulerSessionFactoryContext")));
        final TransactionInterceptor transactionInterceptor = new TransactionInterceptor(provider);
        bindInterceptor(Matchers.inPackage(MessageDao.class.getPackage()), Matchers.annotatedWith(Transactional.class), transactionInterceptor);
        bindInterceptor(Matchers.inPackage(EventSchedulerDao.class.getPackage()), Matchers.annotatedWith(Transactional.class), transactionInterceptor);
    }

    /**
     * Creates hibernate configuration from the configuration yaml properties.
     * Since the yaml properties are already flattened in input param <code>yamlConfiguration</code>
     * the method loops over them to selectively pick Hibernate specific properties.
     */
    @Provides
    @Singleton
    @Named("schedulerHibernateConfiguration")
    public Configuration getConfiguration(YamlConfiguration yamlConfiguration) {
        Configuration configuration = new Configuration();
        addAnnotatedClassesAndTypes(configuration);
        org.apache.commons.configuration.Configuration hibernateConfig = yamlConfiguration.subset(FLUX_SCHEDULER_HIBERNATE_CONFIG_NAME_SPACE);
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

    /**
     * Returns {@link SessionFactoryContext} which holds the Session Factory for Scheduler DB.
     */
    @Provides
    @Singleton
    @Named("schedulerSessionFactory")
    public SessionFactory getSessionFactoryProvider(@Named("schedulerHibernateConfiguration") Configuration configuration) {
        return configuration.buildSessionFactory();
    }

    private void addAnnotatedClassesAndTypes(Configuration configuration) {
        configuration.addAnnotatedClass(ScheduledMessage.class);
        configuration.addAnnotatedClass(ScheduledEvent.class);
    }

    @Provides
    @Singleton
    @Named("schedulerSessionFactoriesContext")
    public SessionFactoryContext getSessionFactoryProvider(@Named("schedulerSessionFactory") SessionFactory redriverSessionFactory) {
        Map fluxRWSessionFactoriesMap = new HashMap<String, SessionFactory>();
        Map fluxROSessionFactoriesMap = new HashMap<String, SessionFactory>();
        return new SessionFactoryContextImpl(fluxRWSessionFactoriesMap, fluxROSessionFactoriesMap, redriverSessionFactory);
    }
}
