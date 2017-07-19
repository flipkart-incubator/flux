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

import com.flipkart.flux.persistence.CryptHashGenerator;
import com.flipkart.flux.persistence.DataStorage;
import com.flipkart.flux.persistence.SelectDataSource;
import com.flipkart.flux.persistence.SessionFactoryContext;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;


/**
 * <code>TransactionInterceptor</code> is a {@link MethodInterceptor} implementation to provide
 * transactional boundaries to methods which are annotated with {@link javax.transaction.Transactional}.
 * It appropriately selects a dataSource based on present {@link com.flipkart.flux.persistence.SelectDataSource} annotation.
 * <p>
 * Example:
 * {
 * method1(); //call method1 which is annotated with transactional
 * }
 *
 * @author shyam.akirala
 * @author amitkumar.o
 * @Transactional void method1() {
 * method2(); //call method2 which is annotated with transactional
 * }
 * @Transactional void method2() {}
 * <p>
 * In the above case a transaction would be started before method1 invocation using this interceptor and ended once method1's execution is over.
 * Same session and transaction would be used throughout.
 */
public class TransactionInterceptor implements MethodInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(TransactionInterceptor.class);

    private final Provider<SessionFactoryContext> contextProvider;

    public TransactionInterceptor(Provider<SessionFactoryContext> contextProvider) {
        this.contextProvider = contextProvider;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        Transaction transaction = null;
        Session session = null;
        SessionFactoryContext context = contextProvider.get();

        try {
            session = context.getThreadLocalSession();
        } catch (HibernateException e) {
        }

        if (session == null) {
            //start a new session if current session is null
            //get shardKey from method argument if there is any
            String shardKey;
            SessionFactory sessionFactory = null;

            try {
                DataStorage dataStorage = invocation.getMethod().getAnnotation(DataStorage.class);
                switch (dataStorage.value()) {
                    case SHARDED: {
                        try {
                            SelectDataSource selectedDS = invocation.getMethod().getAnnotation(SelectDataSource.class);
                            switch (selectedDS.value()) {
                                // in this case invocation method will provide shardKey as the first argument,
                                // whose sessionFactory will be used
                                case READ_ONLY: {
                                    Object[] args = invocation.getArguments();
                                    shardKey = (String) args[0];
                                    sessionFactory = context.getROSessionFactory(shardKey);
                                    break;
                                }
                                // in this case invocation method will provide shardKey i.e stateMachineId, as the first argument,
                                // which will determine to which master shard the query should go to.
                                case READ_WRITE: {
                                    Object[] args = invocation.getArguments();
                                    String shardKeyPrefix = CryptHashGenerator.getUniformCryptHash((String) args[0]);
                                    sessionFactory = context.getRWSessionFactory(shardKeyPrefix);
                                    break;
                                }
                            }
                        } catch (Exception ex) {
                            logger.error("Current Transactional Method doesn't have annotation @SelectDataSource Method_name:{} {}"
                                    , invocation.getMethod().getName(), ex.getStackTrace());
                            return new Error("Something wrong with Method's annotations");
                        }
                        break;
                    }
                    case REDRIVER: {
                        sessionFactory = context.getSchedulerSessionFactory();
                        break;
                    }
                }
            } catch (Exception ex) {
                logger.error("Current Transactional Method doesn't have annotation @DataStorage Method_name:{} {}"
                        , invocation.getMethod().getName(), ex.getStackTrace());
                return new Error("Something wrong with Method's annotations");
            }
            // open a new session, and set it in the ThreadLocal Context
            session = sessionFactory.openSession();
            context.setSession(session);
            logger.debug("Open new session for the thread, using it: {}, {}", invocation.getMethod().getName(), invocation.getMethod().getDeclaringClass());
            ManagedSessionContext.bind(session);
            transaction = session.getTransaction();
            transaction.begin();
            try {
                Object result = invocation.proceed();
                transaction.commit();
                return result;
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            } finally {
                ManagedSessionContext.unbind(session.getSessionFactory());
                logger.debug("Transaction completed for method : {} {} {}", invocation.getMethod().getName(), invocation.getMethod().getDeclaringClass());
                session.close();
                context.clear();
                logger.debug("Clearing session from ThreadLocal Context {} {} {}", invocation.getMethod().getName(), invocation.getMethod().getDeclaringClass());

            }

        } else {
            Object result = invocation.proceed();
            logger.debug("Use old session for the thread, reusing it: {}, {}", invocation.getMethod().getName(), invocation.getMethod().getDeclaringClass());
            return result;
        }
    }
}

