/*
 * Copyright 2012-2018, the original author or authors.
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

package com.flipkart.flux.clientelb.dao;

import com.flipkart.flux.FluxRole;
import com.flipkart.flux.InjectFromRole;
import com.flipkart.flux.boot.SchedulerTestModule;
import com.flipkart.flux.domain.ClientElb;
import com.flipkart.flux.guice.module.ConfigModule;
import com.flipkart.flux.persistence.SessionFactoryContext;
import com.flipkart.flux.runner.GuiceJunit4Runner;
import com.flipkart.flux.runner.Modules;
import com.google.inject.Inject;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Timestamp;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author akif.khan
 */
@RunWith(GuiceJunit4Runner.class)
@Modules(orchestrationModules = {ConfigModule.class, SchedulerTestModule.class}, executionModules = {})
public class ClientElbDAOTest {

    @InjectFromRole(value = FluxRole.ORCHESTRATION)
    ClientElbDAOImpl clientElbDAOImpl;

    @InjectFromRole(value = FluxRole.ORCHESTRATION, name = "schedulerSessionFactoriesContext")
    SessionFactoryContext sessionFactory;

    private void clean() throws Exception {
        Session session = sessionFactory.getSchedulerSessionFactory().openSession();
        ManagedSessionContext.bind(session);
        Transaction tx = session.beginTransaction();
        try {
            session.createSQLQuery("delete from ClientElb").executeUpdate();
            tx.commit();
        } finally {
            if (session != null) {
                ManagedSessionContext.unbind(session.getSessionFactory());
                session.close();
                sessionFactory.clear();
            }
        }
    }
    @Before
    public void setUp() throws Exception {
        clean();
    }

    @After
    public void tearDown() throws Exception {
        clean();
    }

    @Test
    public void testCreateAndDelete() throws Exception {
        clientElbDAOImpl.create(new ClientElb("client_1", "http://2.2.2.2"));
        clientElbDAOImpl.create(new ClientElb("client_2", "http://1.1.1.1"));

        ClientElb clientElb1 = clientElbDAOImpl.findById("client_1");
        assertThat(clientElb1.getId()).isEqualTo("client_1");
        assertThat(clientElb1.getElbUrl()).isEqualTo("http://2.2.2.2");

        clientElbDAOImpl.delete("client_1");

        ClientElb clientElb2 = clientElbDAOImpl.findById("client_2");
        assertThat(clientElb2.getId()).isEqualTo("client_2");
        assertThat(clientElb2.getElbUrl()).isEqualTo("http://1.1.1.1");
    }

    @Test
    public void testFindByIdAndUpdate() throws Exception {
        clientElbDAOImpl.create(new ClientElb("client_1", "http://2.2.2.2"));
        clientElbDAOImpl.updateElbUrl("client_1", "http://1.1.1.1");

        ClientElb clientElb = clientElbDAOImpl.findById("client_1");
        assertThat(clientElb.getId()).isEqualTo("client_1");
        assertThat(clientElb.getElbUrl()).isEqualTo("http://1.1.1.1");
    }
}
