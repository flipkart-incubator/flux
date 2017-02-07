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

package com.flipkart.flux.client.intercept;

import com.flipkart.flux.api.StateMachineDefinition;
import com.flipkart.flux.client.utils.TestResource;
import org.apache.commons.lang3.mutable.MutableInt;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A <code>TestResource</code> that mimics the Core Flux Runtime
 */
@Path("/flux")
@Singleton
public class DummyFluxRuntimeResource implements TestResource {

    Map<StateMachineDefinition,MutableInt> smToCountMap;
    @Inject
    public DummyFluxRuntimeResource() {
        smToCountMap = new HashMap<>();

    }

    @Path("/machines")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    public String receiveTestStateMachines(StateMachineDefinition stateMachineDefinition) {
        if (!smToCountMap.containsKey(stateMachineDefinition)) {
            smToCountMap.put(stateMachineDefinition,new MutableInt(0));
        }
        smToCountMap.get(stateMachineDefinition).increment();
        return "some-id";
    }

    @Override
    public void reset() {
        this.smToCountMap.clear();
    }

    public void assertStateMachineReceived(StateMachineDefinition stateMachineDefinition, Integer expectedCount) {
        assertThat(smToCountMap).containsKey(stateMachineDefinition);
        assertThat(smToCountMap.get(stateMachineDefinition).intValue()).isEqualTo(expectedCount);
    }
}
