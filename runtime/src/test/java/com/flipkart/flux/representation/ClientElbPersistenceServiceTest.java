package com.flipkart.flux.representation;

import com.flipkart.flux.clientelb.dao.ClientElbDAOImpl;
import com.flipkart.flux.domain.ClientElb;
import com.flipkart.flux.guice.module.ConfigModule;
import com.flipkart.flux.guice.module.SchedulerModuleTest;
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

import javax.inject.Named;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(GuiceJunit4Runner.class)
@Modules({ConfigModule.class, SchedulerModuleTest.class})
public class ClientElbPersistenceServiceTest {

    @Inject
    ClientElbDAOImpl clientElbDAO;

    @Inject
    @Named("schedulerSessionFactoriesContext")
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
    public void testClientElbCacheRefresherLRUPolicy() {
        Integer clientElbCacheSize = 2;
        ClientElbPersistenceService clientElbPersistenceService = new ClientElbPersistenceService(
                clientElbDAO, clientElbCacheSize);

        String clientId1 = "id1";
        String elbUrl1 = "http://10.3.3.3";
        clientElbDAO.create(clientId1, new ClientElb(clientId1, elbUrl1));

        String clientId2 = "id2";
        String elbUrl2 = "http://10.4.3.3";
        clientElbDAO.create(clientId2, new ClientElb(clientId2, elbUrl2));

        String clientId3 = "id3";
        String elbUrl3 = "http://10.4.3.3";
        clientElbDAO.create(clientId3, new ClientElb(clientId3, elbUrl3));

        String clientId4 = "id4";
        String elbUrl4 = "http://10.4.3.3";
        clientElbDAO.create(clientId4, new ClientElb(clientId4, elbUrl4));

        clientElbPersistenceService.findByIdClientElb(clientId1);
        clientElbPersistenceService.findByIdClientElb(clientId2);
        clientElbPersistenceService.findByIdClientElb(clientId3);
        clientElbPersistenceService.findByIdClientElb(clientId1);

        assertThat(clientElbPersistenceService.getClientElbCacheSize()).isEqualTo(2);
        assertThat(clientElbPersistenceService.clientElbCacheContainsKey("id1")).isTrue();
        assertThat(clientElbPersistenceService.clientElbCacheContainsKey("id3")).isTrue();
        assertThat(clientElbPersistenceService.clientElbCacheContainsKey("id4")).isFalse();
        assertThat(clientElbPersistenceService.clientElbCacheContainsKey("id2")).isFalse();
    }

    @Test
    public void testClientElbCacheUpdate() {
        Integer clientElbCacheSize = 2;
        ClientElbPersistenceService clientElbPersistenceService = new ClientElbPersistenceService(
                clientElbDAO, clientElbCacheSize);

        String clientId1 = "id1";
        String elbUrl1 = "http://10.3.3.3";
        clientElbDAO.create(clientId1, new ClientElb(clientId1, elbUrl1));

        String clientId2 = "id2";
        String elbUrl2 = "http://10.4.3.3";
        clientElbDAO.create(clientId2, new ClientElb(clientId2, elbUrl2));

        String clientId3 = "id3";
        String elbUrl3 = "http://10.4.3.3";
        clientElbDAO.create(clientId3, new ClientElb(clientId3, elbUrl3));

        String clientId4 = "id4";
        String elbUrl4 = "http://10.4.3.3";
        clientElbDAO.create(clientId4, new ClientElb(clientId4, elbUrl4));

        clientElbPersistenceService.findByIdClientElb(clientId1);
        clientElbPersistenceService.findByIdClientElb(clientId2);
        clientElbPersistenceService.findByIdClientElb(clientId3);
        clientElbPersistenceService.findByIdClientElb(clientId1);

        clientElbPersistenceService.updateClientElb("id1", "http://10.240.23.65");
        clientElbPersistenceService.updateClientElb("id2", "http://10.4.3.65");

        String updatedUrl = clientElbPersistenceService.getClientElbCacheUrl("id1");

        assertThat(clientElbPersistenceService.getClientElbCacheSize()).isEqualTo(2);
        assertThat(updatedUrl.equalsIgnoreCase("http://10.240.23.65")).isTrue();
    }

    @Test
    public void testClientElbCacheDelete() {
        Integer clientElbCacheSize = 2;
        ClientElbPersistenceService clientElbPersistenceService = new ClientElbPersistenceService(
                clientElbDAO, clientElbCacheSize);

        String clientId1 = "id1";
        String elbUrl1 = "http://10.3.3.3";
        clientElbDAO.create(clientId1, new ClientElb(clientId1, elbUrl1));

        String clientId2 = "id2";
        String elbUrl2 = "http://10.4.3.3";
        clientElbDAO.create(clientId2, new ClientElb(clientId2, elbUrl2));

        String clientId3 = "id3";
        String elbUrl3 = "http://10.4.3.3";
        clientElbDAO.create(clientId3, new ClientElb(clientId3, elbUrl3));

        String clientId4 = "id4";
        String elbUrl4 = "http://10.4.3.3";
        clientElbDAO.create(clientId4, new ClientElb(clientId4, elbUrl4));

        clientElbPersistenceService.findByIdClientElb(clientId1);
        clientElbPersistenceService.findByIdClientElb(clientId2);
        clientElbPersistenceService.findByIdClientElb(clientId3);
        clientElbPersistenceService.findByIdClientElb(clientId1);

        clientElbPersistenceService.deleteClientElb("id3");
        clientElbPersistenceService.deleteClientElb("id2");

        assertThat(clientElbPersistenceService.getClientElbCacheSize()).isEqualTo(1);
        assertThat(clientElbPersistenceService.clientElbCacheContainsKey("id3")).isFalse();
    }
}