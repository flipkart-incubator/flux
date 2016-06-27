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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.api.EventData;
import com.flipkart.flux.api.StateMachineDefinition;
import com.flipkart.flux.client.intercept.SimpleWorkflowForTest.IntegerEvent;
import com.flipkart.flux.client.intercept.SimpleWorkflowForTest.StringEvent;
import com.flipkart.flux.client.intercept.SimpleWorkflowForTest.StringEventWithContext;
import com.flipkart.flux.client.model.Event;
import com.flipkart.flux.client.runtime.FluxRuntimeConnector;
import com.flipkart.flux.client.runtime.LocalContext;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Method;
import java.util.Collection;

import static com.flipkart.flux.client.utils.TestUtil.dummyInvocation;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WorkflowInterceptorTest {

    SimpleWorkflowForTest simpleWorkflowForTest = new SimpleWorkflowForTest();

    @Mock
    LocalContext localContext;

    @Mock
    FluxRuntimeConnector fluxRuntimeConnector;

    WorkflowInterceptor workflowInterceptor;
    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        workflowInterceptor = new WorkflowInterceptor(localContext,fluxRuntimeConnector);
        objectMapper = new ObjectMapper();
    }

    @Test
    public void shouldRegisterNewDefinitionWithLocalContext() throws Throwable {
        final Method invokedMethod = simpleWorkflowForTest.getClass().getDeclaredMethod("simpleDummyWorkflow", StringEvent.class, IntegerEvent.class);
        workflowInterceptor.invoke(dummyInvocation(invokedMethod));
        final String expectedMethodIdentifer = new MethodId(invokedMethod).toString();
        Mockito.verify(localContext, times(1)).registerNew(expectedMethodIdentifer, 1, "",null);
    }

    @Test
    public void shouldSubmitNewDefinitionAfterMethodIsInvoked() throws Throwable {
        workflowInterceptor.invoke(dummyInvocation(simpleWorkflowForTest.getClass().getDeclaredMethod("simpleDummyWorkflow", StringEvent.class, IntegerEvent.class)));
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

    @Test
    public void testWorkflowInterception_WithActualParameters() throws Throwable {
        /* setup */
        final MutableInt getEventNameCall = new MutableInt(0);
        doAnswer(invocation -> {
            Event argument = (Event) invocation.getArguments()[0];
            final int currentValue = getEventNameCall.intValue();
            getEventNameCall.increment();
            return argument.name() + currentValue;
        }).when(localContext).generateEventName(any(Event.class));

        final Method invokedMethod = simpleWorkflowForTest.getClass().getDeclaredMethod("simpleDummyWorkflow", StringEvent.class, IntegerEvent.class);
        final StringEvent testStringEvent = new StringEvent("someEvent");
        final IntegerEvent testIntegerEvent = new IntegerEvent(1);
        /* invoke method */
        workflowInterceptor.invoke(dummyInvocation(invokedMethod,new Object[]{testStringEvent,testIntegerEvent}));
        /* verifications */
        verify(localContext,times(1)).addEvents(
            new EventData(SimpleWorkflowForTest.STRING_EVENT_NAME + "0", StringEvent.class.getName(), objectMapper.writeValueAsString(testStringEvent), WorkflowInterceptor.CLIENT),
            new EventData(SimpleWorkflowForTest.INTEGER_EVENT_NAME + "1", IntegerEvent.class.getName(), objectMapper.writeValueAsString(testIntegerEvent), WorkflowInterceptor.CLIENT)
        );
    }

    @Test
    public void testWorkflowInterception_WithActualParameters_WithContextId() throws Throwable {
        when(localContext.generateEventName(any(Event.class))).thenReturn("someNewEvent");

        final Method invokedMethod = simpleWorkflowForTest.getClass().getDeclaredMethod("simpleDummyWorkflowWithCorrelationEvent", StringEventWithContext.class, IntegerEvent.class);
        final StringEventWithContext testStringEvent = new StringEventWithContext("someString","aContextId");
        final IntegerEvent testIntegerEvent = new IntegerEvent(1);

        /* invoke method */
        workflowInterceptor.invoke(dummyInvocation(invokedMethod,new Object[]{testStringEvent,testIntegerEvent}));
        /* verifications */
        verify(localContext,times(1)).registerNew(new MethodId(invokedMethod).toString(),1,"","aContextId");
    }

    @Test(expected = IllegalSignatureException.class)
    public void testSignatureCheck_shouldNotAllowParamsThatAreNotEvents() throws Throwable {
        workflowInterceptor.invoke(dummyInvocation(SimpleWorkflowForTest.class.getDeclaredMethod("badWorkflowWithNonEventParams", String.class)));
    }

    @Test(expected = IllegalSignatureException.class)
    public void testSignatureCheck_shouldNotAllowCollections() throws Throwable {
        //TODO This is temporary. We should ideally accept collections of events
        workflowInterceptor.invoke(dummyInvocation(SimpleWorkflowForTest.class.getDeclaredMethod("badWorkflowWithCollectionOfEvents", Collection.class)));
    }

    @Test
    public void testSignatureCheck_shouldAllowVarArgMethods() throws Throwable {
        when(localContext.generateEventName(any(Event.class))).thenReturn("someName");
        final StringEvent wfParam1 = new StringEvent("foobar");
        final StringEvent wfParam2 = new StringEvent("foobar2");
        final MethodInvocation invocation = dummyInvocation(SimpleWorkflowForTest.class.getDeclaredMethod("simpleDummyWorkflow", StringEvent[].class), new Object[]{new StringEvent[]{wfParam1,wfParam2}});
        workflowInterceptor.invoke(invocation);
        verify(localContext,times(1)).registerNew(new MethodId(invocation.getMethod()).toString(), 1l, "",null);
        final EventData expectedData1 = new EventData("someName" /*cuz were using mock localContext */, "com.flipkart.flux.client.intercept.SimpleWorkflowForTest$StringEvent", objectMapper.writeValueAsString(wfParam1), WorkflowInterceptor.CLIENT);
        final EventData expectedData2 = new EventData("someName", "com.flipkart.flux.client.intercept.SimpleWorkflowForTest$StringEvent", objectMapper.writeValueAsString(wfParam2), WorkflowInterceptor.CLIENT);
        verify(localContext,times(1)).addEvents(expectedData1,expectedData2);
    }
}