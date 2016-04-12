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

import com.flipkart.flux.dao.AuditDAOImpl;
import com.flipkart.flux.dao.EventsDAOImpl;
import com.flipkart.flux.dao.StateMachinesDAOImpl;
import com.flipkart.flux.dao.StatesDAOImpl;
import com.flipkart.flux.dao.iface.AuditDAO;
import com.flipkart.flux.dao.iface.EventsDAO;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.dao.iface.StatesDAO;
import com.flipkart.flux.guice.interceptor.TransactionInterceptor;
import com.flipkart.flux.guice.provider.ConfigurationProvider;
import com.flipkart.flux.guice.provider.SessionFactoryProvider;
import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import javax.inject.Singleton;
import javax.transaction.Transactional;

/**
 * <code>HibernateModule</code> is a Guice {@link AbstractModule} implementation used for wiring SessionFactory, DAO and Interceptor classes.
 * @author shyam.akirala
 */
public class HibernateModule extends AbstractModule {

    @Override
    protected void configure() {
        //bind hibernate configuration and session factory
        bind(Configuration.class).toProvider(ConfigurationProvider.class).in(Singleton.class);
        bind(SessionFactory.class).toProvider(SessionFactoryProvider.class).in(Singleton.class);

        //bind entity classes
        bind(AuditDAO.class).to(AuditDAOImpl.class);
        bind(EventsDAO.class).to(EventsDAOImpl.class);
        bind(StateMachinesDAO.class).to(StateMachinesDAOImpl.class);
        bind(StatesDAO.class).to(StatesDAOImpl.class);

        //bind Transactional Interceptor to intercept methods which are annotated with javax.transaction.Transactional
        TransactionInterceptor transactionInterceptor = new TransactionInterceptor();
        requestInjection(transactionInterceptor);
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Transactional.class), transactionInterceptor);
    }

}