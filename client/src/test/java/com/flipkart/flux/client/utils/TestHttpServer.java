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
 *
 */

package com.flipkart.flux.client.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.rules.ExternalResource;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Singleton
public class TestHttpServer extends ExternalResource {

    public static final int TEST_HTTP_SERVER_PORT = 9091;
    @SuppressWarnings("unused")
	private final Server httpServer;
    private final Map<Class<? extends TestResource>,TestResource> testResourceMap;

    @Inject
    public TestHttpServer(Set<TestResource> testResourceSet) {
        ObjectMapper objectMapper = new ObjectMapper();
        JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
        provider.setMapper(objectMapper);
        this.testResourceMap = new HashMap<>();
        testResourceSet.forEach((tr) -> this.testResourceMap.put(tr.getClass(), tr));
        final ResourceConfig resourceConfig = new ResourceConfig();
        this.testResourceMap.values().forEach(resourceConfig::register);
        resourceConfig.register(provider);
        httpServer = JettyHttpContainerFactory.createServer(UriBuilder.fromUri("http://localhost/").port(TEST_HTTP_SERVER_PORT).build(), resourceConfig);
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        this.testResourceMap.values().forEach(TestResource::reset);
    }
}
