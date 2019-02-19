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
import com.flipkart.flux.client.guice.annotation.IsolatedEnv;
import com.flipkart.flux.client.intercept.SimpleWorkflowForTest.IntegerEvent;
import com.flipkart.flux.client.intercept.SimpleWorkflowForTest.StringEvent;
import com.flipkart.flux.client.registry.ExecutableRegistry;
import com.flipkart.flux.client.runner.GuiceJunit4Runner;
import com.flipkart.flux.client.runtime.FluxRuntimeConnector;
import com.flipkart.flux.client.runtime.LocalContext;
import com.flipkart.flux.client.utils.TestHttpServer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.util.ReflectionTestUtils;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.stream.Stream;

import static com.flipkart.flux.client.constant.ClientConstants._VERSION;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author yogesh.nachnani
 */
@RunWith(GuiceJunit4Runner.class)
public class E2EWorkflowTest {

    @Inject
    SimpleWorkflowForTest simpleWorkflowForTest;

    @Inject
    LocalContext localContext;

    @Inject
    FluxRuntimeConnector fluxRuntimeConnector;

    @Inject
    @Rule
    public TestHttpServer testHttpServer;

    @Inject
    public DummyFluxRuntimeResource dummyFluxRuntimeResource;

    @Inject @IsolatedEnv
    ExecutableRegistry executableRegistry;


    @Test
    public void test_e2eSubmissionOfAWorkflow() throws Exception {
        simpleWorkflowForTest.simpleDummyWorkflow(new StringEvent("String one"),new IntegerEvent(2));
        /* verify submission to flux runtime */
        dummyFluxRuntimeResource.assertStateMachineReceived(simpleWorkflowForTest.getEquivalentStateMachineDefintion(new StringEvent("String one"),new IntegerEvent(2)), 1);
        /* verify registration in executable registry */
        final Map<String,Method> identifierToMethodMap = (Map<String, Method>) ReflectionTestUtils.getField(executableRegistry, "identifierToMethodMap");
        assertThat(identifierToMethodMap.keySet()).containsOnlyOnce(
            new MethodId(SimpleWorkflowForTest.class.getDeclaredMethod("simpleStringModifyingTask", StringEvent.class)).toString()+_VERSION+"1",
            new MethodId(SimpleWorkflowForTest.class.getDeclaredMethod("someTaskWithIntegerAndString", StringEvent.class, IntegerEvent.class)).toString()+_VERSION+"1",
            new MethodId(SimpleWorkflowForTest.class.getDeclaredMethod("simpleAdditionTask", IntegerEvent.class)).toString()+_VERSION+"1"
        );
    }

    @Test
    public void testSubmissionOfWorkflowWithExternalEvents() throws Exception {
        simpleWorkflowForTest.simpleDummyWorkflowWithExternalEvent(new IntegerEvent(2));
        final StateMachineDefinition submittedDefinition = dummyFluxRuntimeResource.smToCountMap.keySet().stream().findFirst().get();
        assertThat(submittedDefinition.getStates()).hasSize(3);
        final Stream<String> eventDefNames = submittedDefinition.getStates().stream().flatMap(stateDefinition -> stateDefinition.getDependencies().stream()).map(EventDefinition::getName);
        assertThat(eventDefNames.distinct().toArray()).containsOnlyOnce(SimpleWorkflowForTest.INTEGER_EVENT_NAME + "0", "someExternalEvent", "com.flipkart.flux.client.intercept.SimpleWorkflowForTest$StringEvent1");
    }
}
