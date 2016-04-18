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

package com.flipkart.flux.controller;

import static org.junit.Assert.assertTrue;
import static com.flipkart.flux.constant.RuntimeConstants.DASHBOARD_CONTEXT_PATH;
import static com.flipkart.flux.constant.RuntimeConstants.DASHBOARD_VIEW;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.flipkart.flux.guice.module.ConfigModule;
import com.flipkart.flux.guice.module.ContainerModule;
import com.google.common.io.ByteStreams;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Simple test spawing dashboard jetty server and asserting relevant URL paths.
 *
 * @author kartik.bommepally
 */
public class DashboardWebAppTest {

    private static Injector injector;
    private static Server server;
    private static int port;

    // TODO: Replace this with the GuiceJunit4Runner
    @BeforeClass
    public static void beforeClass() throws Exception {
        injector = Guice.createInjector(new ConfigModule(), new ContainerModule());
        server = injector.getInstance(Key.get(Server.class, Names.named("DashboardJettyServer")));
        server.start();
        port = injector.getInstance(Key.get(Integer.class, Names.named("Dashboard.service.port")));
    }

    @AfterClass
    public static void afterClass() throws Exception {
        server.stop();
    }

    private String getContent(URL url, int bytes) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        InputStream inputStream = conn.getInputStream();
        try {
            byte b[] = new byte[bytes];
            ByteStreams.readFully(inputStream, b);
            return new String(b);
        } finally {
            inputStream.close();
            conn.disconnect();
        }
    }
    
    @Test
    public void testPath_Dashboard() throws Exception {
        URL url = new URL("http://localhost:" + port + DASHBOARD_CONTEXT_PATH + "/" + DASHBOARD_VIEW);
        String content = getContent(url, 2048);
        assertTrue(content.contains("Phantom"));
    }

    @Test
    public void testPath_HystrixStreams() throws Exception {
        URL url = new URL("http://localhost:" + port + DASHBOARD_CONTEXT_PATH + "/hystrix.stream.command.local");
        String content = getContent(url, 16);
        assertTrue(content.contains("ping:"));

        url = new URL("http://localhost:" + port + DASHBOARD_CONTEXT_PATH + "/hystrix.stream.global");
        content = getContent(url, 16);
        assertTrue(content.contains("ping:"));

        url = new URL("http://localhost:" + port + DASHBOARD_CONTEXT_PATH + "/hystrix.stream.tp.local");
        content = getContent(url, 16);
        assertTrue(content.contains("ping:"));
    }
}