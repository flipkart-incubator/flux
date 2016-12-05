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

package com.flipkart.flux.rule;

import com.flipkart.flux.domain.AuditRecord;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.persistence.SessionFactoryContext;
import com.flipkart.flux.redriver.model.ScheduledMessage;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;
import org.junit.rules.ExternalResource;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * <code>DbClearRule</code> is a Junit Rule which clears db tables before running a test.
 * @author shyam.akirala
 */
@Singleton
public class DbClearRule extends ExternalResource{

    private final SessionFactoryContext fluxSessionFactoryContext;
    private final SessionFactoryContext redriverSessionFactoryContext;

    /** List of entity tables which need to be cleared from flux db*/
    private static Class[] fluxTables = {StateMachine.class, State.class, AuditRecord.class, Event.class};

    /** List of entity tables which need to be cleared from flux redriver db*/
    private static Class[] fluxRedriverTables = {ScheduledMessage.class};


    @Inject
    public DbClearRule(@Named("fluxSessionFactoryContext") SessionFactoryContext fluxSessionFactoryContext,
                       @Named("redriverSessionFactoryContext") SessionFactoryContext redriverSessionFactoryContext) {
        this.fluxSessionFactoryContext = fluxSessionFactoryContext;
        this.redriverSessionFactoryContext = redriverSessionFactoryContext;
    }

    @Override
    protected void before() throws Throwable {
        fluxSessionFactoryContext.useDefault();
        clearDb(fluxTables,fluxSessionFactoryContext.getSessionFactory());
        fluxSessionFactoryContext.clear();

        redriverSessionFactoryContext.useDefault();
        clearDb(fluxRedriverTables,redriverSessionFactoryContext.getSessionFactory());
        redriverSessionFactoryContext.clear();
    }

    /** Clears all given tables which are mentioned using the given sessionFactory*/
    private void clearDb(Class[] tables, SessionFactory sessionFactory) {
        Session session = sessionFactory.openSession();
        ManagedSessionContext.bind(session);
        Transaction tx = session.beginTransaction();
        try {
            sessionFactory.getCurrentSession().createSQLQuery("set foreign_key_checks=0").executeUpdate();
            for (Class anEntity : tables) {
                sessionFactory.getCurrentSession().createSQLQuery("delete from " + anEntity.getSimpleName() + "s").executeUpdate(); //table name is plural form of class name, so appending 's'
            }
            sessionFactory.getCurrentSession().createSQLQuery("set foreign_key_checks=1").executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if(tx != null)
                tx.rollback();
            throw new RuntimeException("Unable to clear tables. Exception: "+e.getMessage(), e);
        } finally {
            if(session != null) {
                ManagedSessionContext.unbind(sessionFactory);
                session.close();
            }
        }
    }
}