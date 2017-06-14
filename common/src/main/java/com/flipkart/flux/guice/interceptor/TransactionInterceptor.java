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

package com.flipkart.flux.guice.interceptor;
import com.flipkart.flux.persistence.SelectDataSource;
import com.flipkart.flux.persistence.ShardedSessionFactoryContext;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;

import javax.inject.Provider;
import java.util.LinkedList;
import java.util.List;

/**
 * <code>TransactionInterceptor</code> is a {@link MethodInterceptor} implementation to provide
 * transactional boundaries to methods which are annotated with {@link javax.transaction.Transactional}.
 * It appropriately selects a dataSource based on present {@link com.flipkart.flux.persistence.SelectDataSource} annotation.
 *
 * Example:
 * {
 *     method1(); //call method1 which is annotated with transactional
 * }
 *
 * @Transactional
 * void method1() {
 *      method2(); //call method2 which is annotated with transactional
 * }
 *
 * @Transactional
 * void method2() {}
 *
 * In the above case a transaction would be started before method1 invocation using this interceptor and ended once method1's execution is over.
 * Same session and transaction would be used throughout.
 *
 * @author shyam.akirala
 */
public class TransactionInterceptor implements MethodInterceptor {

    private final Provider<ShardedSessionFactoryContext> contextProvider;

    public TransactionInterceptor(Provider<ShardedSessionFactoryContext> contextProvider) {
        this.contextProvider = contextProvider;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        Transaction transaction = null;
        Session session = null;
        ShardedSessionFactoryContext context = contextProvider.get();

        try {
            SessionFactory currentSessionFactory = context.getCurrentSessionFactory();
            if(currentSessionFactory != null) {
                session = currentSessionFactory.getCurrentSession();
            }
        } catch (HibernateException e) {}

        if (session == null) {
            //start a new session and transaction if current session is null
            //get DataSourceType first
            List sessionFactories = new LinkedList<SessionFactory>();
            Object[] methodArguments = invocation.getArguments();
            SelectDataSource selectedDS = invocation.getMethod().getAnnotation(SelectDataSource.class);
            if(selectedDS == null) {
                context.useDefault();
                sessionFactories = context.getDefaultShardedSessionFactory().getSessionFactories();
            } else {
                context.useShardedSessionFactory(selectedDS.value());
                sessionFactories = context.getShardedSessionFactory(selectedDS.value()).getSessionFactories();
            }
            // set the correct sessionFactory in Context from all sessionFactories, based on argument
            session = (sessionFactories, methodArguments);
            session = context.getCurrentSessionFactory().openSession();
            ManagedSessionContext.bind(session);
            transaction = session.getTransaction();
            transaction.begin();
        }

        try {

            Object result = invocation.proceed();

            if (transaction != null) {
                transaction.commit();
            }

            return result;

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        } finally {
            if (transaction != null && session != null) {
                ManagedSessionContext.unbind(context.getCurrentSessionFactory());
                session.close();
                context.clear();
            }
        }
    }

}

