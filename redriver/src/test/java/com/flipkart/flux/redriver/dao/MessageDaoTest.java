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

package com.flipkart.flux.redriver.dao;

import com.flipkart.flux.guice.module.ConfigModule;
import com.flipkart.flux.redriver.boot.RedriverTestModule;
import com.flipkart.flux.redriver.model.ScheduledMessage;
import com.flipkart.flux.runner.GuiceJunit4Runner;
import com.flipkart.flux.runner.Modules;
import com.google.inject.Inject;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(GuiceJunit4Runner.class)
@Modules({ConfigModule.class,RedriverTestModule.class})
public class MessageDaoTest {
    @Inject
    MessageDao messageDao;

    @Inject
    SessionFactory sessionFactory;

    @Before
    public void setUp() throws Exception {
        Session session = sessionFactory.openSession();
        ManagedSessionContext.bind(session);
        Transaction tx = session.beginTransaction();
        try {
            sessionFactory.getCurrentSession().createSQLQuery("delete from ScheduledMessages").executeUpdate();
            tx.commit();
        } finally {
            if(session != null) {
                ManagedSessionContext.unbind(sessionFactory);
                session.close();
            }
        }
    }

    @Test
    public void testDeleteInBatch() throws Exception {
        messageDao.save(new ScheduledMessage(1l,2l));
        messageDao.save(new ScheduledMessage(2l,3l));
        messageDao.save(new ScheduledMessage(3l,4l));
        messageDao.deleteInBatch(Arrays.asList(1l, 2l));
        assertThat(messageDao.retrieveAll()).containsExactly(new ScheduledMessage(3l,4l));
    }
}