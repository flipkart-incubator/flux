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

import java.util.Iterator;

import com.flipkart.flux.dao.AuditDAOImpl;
import com.flipkart.flux.dao.EventsDAOImpl;
import com.flipkart.flux.dao.StateMachinesDAOImpl;
import com.flipkart.flux.dao.StatesDAOImpl;
import com.flipkart.flux.dao.iface.AuditDAO;
import com.flipkart.flux.dao.iface.EventsDAO;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.dao.iface.StatesDAO;
import com.flipkart.flux.domain.AuditRecord;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.guice.interceptor.TransactionInterceptor;
import com.flipkart.flux.type.BlobType;
import com.flipkart.flux.type.StoreFQNType;
import com.flipkart.polyguice.config.YamlConfiguration;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import javax.transaction.Transactional;

/**
 * <code>HibernateModule</code> is a Guice {@link AbstractModule} implementation used for wiring SessionFactory, DAO and Interceptor classes.
 * @author shyam.akirala
 * @author kartik.bommepally
 */
public class HibernateModule extends AbstractModule {

    public static final String HIBERNATE_NAME_SPACE = "Hibernate";

    @Override
    protected void configure() {
        //bind entity classes
        bind(AuditDAO.class).to(AuditDAOImpl.class).in(Singleton.class);
        bind(EventsDAO.class).to(EventsDAOImpl.class).in(Singleton.class);
        bind(StateMachinesDAO.class).to(StateMachinesDAOImpl.class).in(Singleton.class);
        bind(StatesDAO.class).to(StatesDAOImpl.class).in(Singleton.class);

        //bind Transactional Interceptor to intercept methods which are annotated with javax.transaction.Transactional
        TransactionInterceptor transactionInterceptor = new TransactionInterceptor();
        requestInjection(transactionInterceptor);
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Transactional.class), transactionInterceptor);
    }

    /**
     * Adds annotated classes and custom types to passed Hibernate configuration.
     */
    private void addAnnotatedClassesAndTypes(Configuration configuration) {
        //register hibernate custom types
        configuration.registerTypeOverride(new BlobType(), new String[]{"BlobType"});
        configuration.registerTypeOverride(new StoreFQNType(), new String[]{"StoreFQNOnly"});

        //add annotated classes to configuration
        configuration.addAnnotatedClass(AuditRecord.class);
        configuration.addAnnotatedClass(Event.class);
        configuration.addAnnotatedClass(State.class);
        configuration.addAnnotatedClass(StateMachine.class);
    }

    /**
     * Creates hibernate configuration from the configuration yaml properties.
     * Since the yaml properties are already flattened in input param <code>yamlConfiguration</code>
     * the method loops over them to selectively pick Hibernate specific properties.
     */
    @Provides
    @Singleton
    public Configuration getConfiguration(YamlConfiguration yamlConfiguration) {
        Configuration configuration = new Configuration();
        addAnnotatedClassesAndTypes(configuration);
        org.apache.commons.configuration.Configuration hibernateConfig = yamlConfiguration.subset(HIBERNATE_NAME_SPACE);
        Iterator<String> propertyKeys = hibernateConfig.getKeys();
        while (propertyKeys.hasNext()) {
            String propertyKey = propertyKeys.next();
            Object propertyValue = hibernateConfig.getProperty(propertyKey);
            String propertyValueStr = propertyValue == null ? null : String.valueOf(propertyValue);
            configuration.setProperty(propertyKey, propertyValueStr);
        }
        return configuration;
    }

    /**
     * Provides SessionFactory singleton.
     */
    @Provides
    @Singleton
    public SessionFactory getSessionFactory(Configuration configuration) {
        return configuration.buildSessionFactory();
    }
}