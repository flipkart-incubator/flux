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

package com.flipkart.flux.util;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;

/**
 * Provides transactional boundaries to methods which are annotated with {@link com.flipkart.flux.util.Transactional}.
 * @author shyam.akirala
 */
public class TransactionInterceptor implements MethodInterceptor {

    private ThreadLocal<Session> threadLocalSession;

    public TransactionInterceptor() {
        threadLocalSession = new ThreadLocal<Session>();
    }

    public Object invoke(MethodInvocation invocation) throws Throwable {

        Transaction transaction = null;
        Session session = threadLocalSession.get();
        boolean startNewTransaction = true;

        if (session == null) {
            session = HibernateUtil.getSessionFactory().openSession();
            threadLocalSession.set(session);
            ManagedSessionContext.bind(session);
            transaction = session.getTransaction();
            transaction.begin();
        } else {
            //already in transaction
            startNewTransaction = false;
        }

        try {

            Object result = invocation.proceed();

            if (transaction != null) {
                transaction.commit();
                threadLocalSession.remove();
            }

            return result;

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
                threadLocalSession.remove();
            }
            throw e;
        } finally {
            if (startNewTransaction && session != null) {
                ManagedSessionContext.unbind(HibernateUtil.getSessionFactory());
                session.close();
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        threadLocalSession.remove();
        super.finalize();
    }

}

