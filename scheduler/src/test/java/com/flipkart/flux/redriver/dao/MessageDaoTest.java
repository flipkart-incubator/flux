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

import static org.assertj.core.api.Assertions.assertThat;

import com.flipkart.flux.FluxRuntimeRole;
import com.flipkart.flux.InjectFromRole;
import com.flipkart.flux.boot.SchedulerTestModule;
import com.flipkart.flux.persistence.SessionFactoryContext;
import com.flipkart.flux.redriver.model.ScheduledMessage;
import com.flipkart.flux.redriver.model.SmIdAndTaskIdWithExecutionVersion;
import com.flipkart.flux.runner.GuiceJunit4Runner;
import com.flipkart.flux.runner.Modules;
import java.util.Arrays;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(GuiceJunit4Runner.class)
@Modules(orchestrationModules = {SchedulerTestModule.class}, executionModules = {})
public class MessageDaoTest {

    @InjectFromRole(value = FluxRuntimeRole.ORCHESTRATION)
    MessageDao messageDao;

    @InjectFromRole(value = FluxRuntimeRole.ORCHESTRATION, name="schedulerSessionFactoriesContext")
    SessionFactoryContext context;

    @Before
    public void setUp() throws Exception {
        context.setThreadLocalSession(context.getSchedulerSessionFactory().openSession());
        Session session = context.getThreadLocalSession();
        ManagedSessionContext.bind(session);
        Transaction tx = session.beginTransaction();
        try {
            session.createSQLQuery("delete from ScheduledMessages").executeUpdate();
            tx.commit();
        } finally {
            if(session != null) {
                ManagedSessionContext.unbind(context.getThreadLocalSession().getSessionFactory());
                session.close();
                context.clear();
            }
        }
    }

    @Test
    public void testDeleteInBatch() throws Exception {
        messageDao.save(new ScheduledMessage(1l, "sample-state-machine-uuid", 2l,0l));
        messageDao.save(new ScheduledMessage(2l, "sample-state-machine-uuid", 3l,0l));
        messageDao.save(new ScheduledMessage(3l, "sample-state-machine-uuid", 4l,0l));
        messageDao.deleteInBatch(Arrays.asList(new SmIdAndTaskIdWithExecutionVersion("sample-state-machine-uuid", 1l, 0l),
                new SmIdAndTaskIdWithExecutionVersion("sample-state-machine-uuid", 2l,0l)));

        assertThat(messageDao.retrieveOldest(0, 10)).containsExactly(new ScheduledMessage(3l, "sample-state-machine-uuid", 4l,0l));
    }

    @Test
    public void testRetrieveOldest() throws Exception {
        messageDao.save(new ScheduledMessage(1l, "sample-state-machine-uuid", 2l,0l));
        messageDao.save(new ScheduledMessage(2l, "sample-state-machine-uuid", 3l,0l));
        messageDao.save(new ScheduledMessage(3l, "sample-state-machine-uuid", 4l,0l));

        assertThat(messageDao.retrieveOldest(0, 1)).containsExactly(new ScheduledMessage(1l, "sample-state-machine-uuid", 2l,0l));
        assertThat(messageDao.retrieveOldest(1, 3)).hasSize(2);
        assertThat(messageDao.retrieveOldest(1, 3)).containsSequence(new ScheduledMessage(2l, "sample-state-machine-uuid", 3l,0l),
                new ScheduledMessage(3l, "sample-state-machine-uuid", 4l,0l));
    }

    @Test
    public void testRemoveSingleEntry() throws Exception {
        messageDao.save(new ScheduledMessage(1l, "sample-state-machine-uuid", 2l,0l));
        messageDao.save(new ScheduledMessage(1l, "sample-state-machine-uuid", 2l,1l));
        messageDao.delete(new SmIdAndTaskIdWithExecutionVersion("sample-state-machine-uuid", 1l,0l));
        assertThat(messageDao.retrieveOldest(0, 10)).containsExactly(new ScheduledMessage(1l, "sample-state-machine-uuid", 2l,1l));
    }
}