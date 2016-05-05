package com.flipkart.flux.controller;

import com.flipkart.flux.guice.module.ConfigModule;
import com.flipkart.flux.guice.module.ContainerModule;
import com.flipkart.flux.guice.module.HibernateModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;

import static com.flipkart.flux.constant.RuntimeConstants.API_CONTEXT_PATH;

public class APIJettyServerTest {

    private static Injector injector;
    private static Server server;
    private static int port;

    // TODO: Replace this with the GuiceJunit4Runner
    @BeforeClass
    public static void beforeClass() throws Exception {
        injector = Guice.createInjector(new ConfigModule(), new ContainerModule(), new HibernateModule());
        server = injector.getInstance(Key.get(Server.class, Names.named("APIJettyServer")));
        server.start();
        port = injector.getInstance(Key.get(Integer.class, Names.named("Api.service.port")));

    }

    @AfterClass
    public static void afterClass() throws Exception {
        server.stop();
    }


    @Test
    public void testPath_Dashboard() throws Exception {
        URL url = new URL("http://localhost:" + port + API_CONTEXT_PATH + "/" + "teams/bestestTeam/workflows/summary");
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url.toURI());
        HttpResponse httpResponse =  httpClient.execute(httpGet);
        //todo
        String response = httpResponse.getEntity().getContent().toString();
    }


}
