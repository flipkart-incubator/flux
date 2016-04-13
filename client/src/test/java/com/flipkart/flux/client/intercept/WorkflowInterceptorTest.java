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

import com.flipkart.flux.client.runner.GuiceJunit4Runner;
import com.flipkart.flux.client.runtime.FluxRuntimeConnector;
import com.flipkart.flux.client.runtime.IllegalSignatureException;
import com.flipkart.flux.client.runtime.LocalContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;

@RunWith(GuiceJunit4Runner.class)
public class WorkflowInterceptorTest {

    @Inject
    SimpleWorkflowForTest simpleWorkflowForTest;

    @Inject
    LocalContext localContext;

    @Inject
    FluxRuntimeConnector fluxRuntimeConnector;

    @Test
    public void shouldRegisterNewDefinitionWithLocalContext() throws Exception {
        simpleWorkflowForTest.simpleDummyWorkflow("foo",2);
        Mockito.verify(localContext,times(1)).registerNew("com.flipkart.flux.client.intercept.SimpleWorkflowForTest_simpleDummyWorkflow_void_java.lang.String_java.lang.Integer");
    }

    @Test
    public void shouldSubmitNewDefinitionAfterMethodIsInvoked() throws Exception {
        simpleWorkflowForTest.simpleDummyWorkflow("foo",2);
        Mockito.verify(fluxRuntimeConnector, times(1)).submitNewWorkflow();
        assertThat(true).isFalse();
    }

    @Test(expected = IllegalSignatureException.class)
    public void shouldNotAllowWorkflowMethodsThatReturnAnything() throws Exception {
        simpleWorkflowForTest.badWorkflow();
    }
}