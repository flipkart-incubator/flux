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

import javax.inject.Provider;
import javax.persistence.PersistenceException;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.flipkart.flux.persistence.CryptHashGenerator;
import com.flipkart.flux.persistence.DataSourceType;
import com.flipkart.flux.persistence.SelectDataSource;
import com.flipkart.flux.persistence.SessionFactoryContext;
import com.flipkart.flux.persistence.Storage;
import com.flipkart.flux.shard.ShardId;
import com.flipkart.flux.shard.ShardedEntity;

/**
 * @author shyam.akirala
 * @author amitkumar.o
 * <p>
 * <code>TransactionInterceptor</code> is a {@link MethodInterceptor} implementation to provide
 * transactional boundaries to methods which are annotated with {@link javax.transaction.Transactional}.
 * It appropriately selects a dataSource based on present {@link com.flipkart.flux.persistence.SelectDataSource} annotation.
 * <p>
 * Example:
 * {
 * method1(); //call method1 which is annotated with transactional
 * }
 * @Transactional void method1() {
 * method2(); //call method2 which is annotated with transactional
 * }
 * @Transactional void method2() {
 * }
 * <p>
 * In the above case a transaction would be started before method1 invocation using this interceptor and ended once method1's execution is over.
 * Same session and transaction would be used throughout.
 **/
public class TransactionInterceptor implements MethodInterceptor {

    private static final Logger logger = LogManager.getLogger(TransactionInterceptor.class);

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
                Storage storage = invocation.getMethod().getAnnotation(SelectDataSource.class).storage();
                switch (storage) {
                    case SHARDED: {
                        try {
                            DataSourceType dataSourceType = invocation.getMethod().getAnnotation(SelectDataSource.class).type();
                            switch (dataSourceType) {
                                // in this case invocation method will provide shardKey as the first argument,
                                // whose sessionFactory will be used
                                case READ_ONLY: {
                                    Object[] args = invocation.getArguments();
                                    //TODO remove this hack
                                    if (args[0] instanceof ShardedEntity) {
                                    	ShardedEntity shardedEntity = (ShardedEntity)args[0];
                                    	if (shardedEntity.getShardId() != null) {
                                            sessionFactory = context.getROSessionFactory(shardedEntity.getShardId());                                    		
                                    	} else {
                                            sessionFactory = context.getRWSessionFactory(CryptHashGenerator.getUniformCryptHash(shardedEntity.getShardKey()));                                    		
                                    	}
                                    	break;
                                    }
                                    ShardId shardId = (ShardId) args[0];
                                    sessionFactory = context.getROSessionFactory(shardId);
                                    break;
                                }
                                // in this case invocation method will provide shardKey i.e stateMachineId, as the first argument,
                                // which will determine to which shard the query should go to.
                                case READ_WRITE: {
                                    Object[] args = invocation.getArguments();
                                    //TODO remove this hack
                                    if (args[0] instanceof ShardedEntity) {
                                    	ShardedEntity shardedEntity = (ShardedEntity)args[0];
                                    	if (shardedEntity.getShardId() != null) {
                                            sessionFactory = context.getROSessionFactory(shardedEntity.getShardId());                                    		
                                    	} else {
                                            sessionFactory = context.getRWSessionFactory(CryptHashGenerator.getUniformCryptHash(shardedEntity.getShardKey()));                                    		
                                    	}
                                    	break;
                                    }
                                    shardKey = (String) args[0];
                                    String shardKeyPrefix = CryptHashGenerator.getUniformCryptHash(shardKey);
                                    sessionFactory = context.getRWSessionFactory(shardKeyPrefix);
                                    break;
                                }
                            }
                        } catch (Exception ex) {
                            logger.error("Error reading Transactional Method with annotation @SelectDataSource Method_name:{} "
                                    , invocation.getMethod().getName(), ex);
                            return new PersistenceException("Something wrong with Method's annotations " + ex.getMessage(), ex);
                        }
                        break;
                    }
                    case SCHEDULER: {
                        sessionFactory = context.getSchedulerSessionFactory();
                        break;
                    }
                }
            } catch (Exception ex) {
                logger.error("Current Transactional Method doesn't have annotation @SelectDataSourceType Method_name:{} {}"
                        , invocation.getMethod().getName(), ex.getStackTrace());
                return new Error("Something wrong with Method's annotations " + ex.getMessage());
            }
            // open a new session, and set it in the ThreadLocal Context
            session = sessionFactory.openSession();
            context.setThreadLocalSession(session);
            logger.debug("Open new session for the thread transaction started, using it: {}, {}", invocation.getMethod().getName(), invocation.getMethod().getDeclaringClass());
            transaction = session.getTransaction();
            transaction.begin();
            try {
                Object result = invocation.proceed();
                transaction.commit();
                return result;
            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback();
                }
                throw e;
            } finally {
                logger.debug("Transaction completed for method : {} {}", invocation.getMethod().getName(), invocation.getMethod().getDeclaringClass());
                if (session != null) {
                    session.close();
                }
                context.clear();
                logger.debug("Clearing session from ThreadLocal Context : {} {}", invocation.getMethod().getName(), invocation.getMethod().getDeclaringClass());
            }
        } else {
            Object result = invocation.proceed();
            logger.debug("Use old session for the thread, reusing it: {}, {}", invocation.getMethod().getName(), invocation.getMethod().getDeclaringClass());
            return result;
        }
    }
}

