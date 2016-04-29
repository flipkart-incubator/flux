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

package com.flipkart.flux.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.initializer.FluxInitializer;
import com.flipkart.flux.rule.DbClearRule;
import com.flipkart.flux.runner.GuiceJunit4Runner;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.junit.*;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.io.IOException;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author shyam.akirala
 */
@RunWith(GuiceJunit4Runner.class)
public class SMResourceIntegrationTest {

    protected static String fluxUrl = "http://localhost:9999";

    @Inject
    @Rule
    public DbClearRule dbClearRule;

    private Client client;

    @BeforeClass
    public static void beforeClass() throws Exception {
        FluxInitializer.main(new String[]{"start"});
    }

    @AfterClass
    public static void afterClass() throws Exception {
        FluxInitializer.main(new String[]{"stop"});
    }
    @Before
    public void setup() {
        client = Client.create();
    }

    @Test
    public void createStateMachineTest() throws IOException {
        String json = "{\n" +
                "  \"name\": \"test_state_machine\",\n" +
                "  \"version\": 1,\n" +
                "  \"description\": \"test_description\",\n" +
                "  \"states\": [\n" +
                "    {\n" +
                "      \"version\": 1,\n" +
                "      \"name\": \"test_state1\",\n" +
                "      \"description\": \"test_state_desc1\",\n" +
                "      \"onEntryHook\": \"com.flipkart.flux.dao.DummyOnEntryHook\",\n" +
                "      \"task\": \"com.flipkart.flux.dao.DummyTask\",\n" +
                "      \"onExitHook\": \"com.flipkart.flux.dao.DummyOnExitHook\",\n" +
                "      \"retryCount\": \"5\",\n" +
                "      \"timeout\": \"100\",\n" +
                "      \"dependencies\": null\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        WebResource webResource = client.resource(fluxUrl+"/fsm/machines");
        ClientResponse clientResponse  = webResource.accept("application/json").type("application/json").post(ClientResponse.class, json);
        assertThat(clientResponse.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }
}
