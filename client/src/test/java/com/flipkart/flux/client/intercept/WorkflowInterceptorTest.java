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
import com.flipkart.flux.client.runtime.FluxRuntimeConnector;
import com.flipkart.flux.client.runtime.LocalContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Method;

import static com.flipkart.flux.client.utils.TestUtil.dummyInvocation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class WorkflowInterceptorTest {

    SimpleWorkflowForTest simpleWorkflowForTest = new SimpleWorkflowForTest();

    @Mock
    LocalContext localContext;

    @Mock
    FluxRuntimeConnector fluxRuntimeConnector;

    WorkflowInterceptor workflowInterceptor;

    @Before
    public void setUp() throws Exception {
        workflowInterceptor = new WorkflowInterceptor(localContext,fluxRuntimeConnector);
    }

    @Test
    public void shouldRegisterNewDefinitionWithLocalContext() throws Throwable {
        final Method invokedMethod = simpleWorkflowForTest.getClass().getDeclaredMethod("simpleDummyWorkflow", SimpleWorkflowForTest.StringEvent.class, SimpleWorkflowForTest.IntegerEvent.class);
        workflowInterceptor.invoke(dummyInvocation(invokedMethod));
        final String expectedMethodIdentifer = new MethodId(invokedMethod).toString();
        Mockito.verify(localContext, times(1)).registerNew(expectedMethodIdentifer, 1, "");
    }

    @Test
    public void shouldSubmitNewDefinitionAfterMethodIsInvoked() throws Throwable {
        workflowInterceptor.invoke(dummyInvocation(simpleWorkflowForTest.getClass().getDeclaredMethod("simpleDummyWorkflow", SimpleWorkflowForTest.StringEvent.class, SimpleWorkflowForTest.IntegerEvent.class)));
        Mockito.verify(fluxRuntimeConnector, times(1)).submitNewWorkflow(any(StateMachineDefinition.class)); // Not verifying the actual state machine here, that is taken care in the e2e test. Besides, localContext is a mock anyway
    }

    @Test
    public void shouldRefreshLocalContext() throws Throwable {
        try {
            workflowInterceptor.invoke(dummyInvocation(simpleWorkflowForTest.getClass().getDeclaredMethod("badWorkflow")));
        } catch (IllegalSignatureException e) {
            // Expected
        }
        verify(localContext, times(1)).reset();
    }

    @Test(expected = IllegalSignatureException.class)
    public void shouldNotAllowWorkflowMethodsThatReturnAnything() throws Throwable {
        workflowInterceptor.invoke(dummyInvocation(simpleWorkflowForTest.getClass().getDeclaredMethod("badWorkflow")));
    }

}