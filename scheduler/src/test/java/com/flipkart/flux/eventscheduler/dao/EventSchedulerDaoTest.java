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

package com.flipkart.flux.eventscheduler.dao;

import com.flipkart.flux.FluxRuntimeRole;
import com.flipkart.flux.InjectFromRole;
import com.flipkart.flux.boot.SchedulerTestModule;
import com.flipkart.flux.eventscheduler.model.ScheduledEvent;
import com.flipkart.flux.persistence.SessionFactoryContext;
import com.flipkart.flux.runner.GuiceJunit4Runner;
import com.flipkart.flux.runner.Modules;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author shyam.akirala
 */
@RunWith(GuiceJunit4Runner.class)
@Modules(orchestrationModules = {SchedulerTestModule.class}, executionModules = {})
public class EventSchedulerDaoTest {

    @InjectFromRole(value = FluxRuntimeRole.ORCHESTRATION)
    EventSchedulerDao eventSchedulerDao;

    @InjectFromRole(value = FluxRuntimeRole.ORCHESTRATION, name = "schedulerSessionFactoriesContext")
    SessionFactoryContext sessionFactory;

    @Before
    public void setUp() throws Exception {
        Session session = sessionFactory.getSchedulerSessionFactory().openSession();
        ManagedSessionContext.bind(session);
        Transaction tx = session.beginTransaction();
        try {
            session.createSQLQuery("delete from ScheduledEvents").executeUpdate();
            tx.commit();
        } finally {
            if (session != null) {
                ManagedSessionContext.unbind(session.getSessionFactory());
                session.close();
                sessionFactory.clear();
            }
        }
    }

    @Test
    public void testSaveAndDelete() throws Exception {
        eventSchedulerDao.save(new ScheduledEvent("smCorId", "event_name1", System.currentTimeMillis()/1000, "data"));
        Long triggerTime = System.currentTimeMillis()/1000;
        eventSchedulerDao.save(new ScheduledEvent("smCorId", "event_name2", triggerTime, "data"));
        assertThat(eventSchedulerDao.retrieveOldest(10)).hasSize(2);

        eventSchedulerDao.delete("smCorId", "event_name1");
        assertThat(eventSchedulerDao.retrieveOldest(10)).containsExactly(new ScheduledEvent("smCorId", "event_name2", triggerTime, "data"));
    }

    @Test
    public void testRetrieveOldest() throws Exception {
        Long triggerTime = System.currentTimeMillis()/1000;
        eventSchedulerDao.save(new ScheduledEvent("smCorId1", "event_name1", triggerTime, "data"));
        eventSchedulerDao.save(new ScheduledEvent("smCorId2", "event_name1", triggerTime+1, "data"));
        eventSchedulerDao.save(new ScheduledEvent("smCorId2", "event_name2", triggerTime+2, "data"));

        assertThat(eventSchedulerDao.retrieveOldest(1)).containsExactly(new ScheduledEvent("smCorId1", "event_name1", triggerTime, "data"));
        assertThat(eventSchedulerDao.retrieveOldest(3)).hasSize(3);
        assertThat(eventSchedulerDao.retrieveOldest(3)).containsSequence(
                new ScheduledEvent("smCorId1", "event_name1", triggerTime, "data"),
                new ScheduledEvent("smCorId2", "event_name1", triggerTime+1, "data"),
                new ScheduledEvent("smCorId2", "event_name2", triggerTime+2, "data"));
    }
}
