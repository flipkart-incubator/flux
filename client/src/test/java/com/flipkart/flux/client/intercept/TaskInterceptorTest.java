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

import com.flipkart.flux.api.EventDefinition;
import com.flipkart.flux.client.intercept.SimpleWorkflowForTest.IntegerEvent;
import com.flipkart.flux.client.intercept.SimpleWorkflowForTest.StringEvent;
import com.flipkart.flux.client.model.Event;
import com.flipkart.flux.client.registry.Executable;
import com.flipkart.flux.client.registry.ExecutableImpl;
import com.flipkart.flux.client.registry.ExecutableRegistry;
import com.flipkart.flux.client.runtime.LocalContext;
import com.flipkart.flux.client.utils.TestUtil;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.flipkart.flux.client.constant.ClientConstants._VERSION;
import static com.flipkart.flux.client.intercept.SimpleWorkflowForTest.INTEGER_EVENT_NAME;
import static com.flipkart.flux.client.intercept.SimpleWorkflowForTest.STRING_EVENT_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TaskInterceptorTest {
    @Mock
    LocalContext localContext;

    @Mock
    ExecutableRegistry executableRegistry;

    private SimpleWorkflowForTest simpleWorkflowForTest;
    private TaskInterceptor taskInterceptor;

    @Before
    public void setUp() throws Exception {
//        localContext = Mockito.spy(new LocalContext());
        taskInterceptor = new TaskInterceptor(localContext,executableRegistry);
        simpleWorkflowForTest = new SimpleWorkflowForTest();
        when(localContext.isWorkflowInterception()).thenReturn(true);
    }

    @Test
    public void testInterception_shouldSubmitNewState_methodWithOneParam() throws Throwable {
        setupMockLocalContext();

        final Method invokedMethod = simpleWorkflowForTest.getClass().getDeclaredMethod("simpleStringModifyingTask", StringEvent.class);
        taskInterceptor.invoke(TestUtil.dummyInvocation(invokedMethod, new Object[]{new StringEvent("someEvent")}));

        final List<EventDefinition> expectedDependency =
            Collections.singletonList(new EventDefinition(STRING_EVENT_NAME+"0","com.flipkart.flux.client.intercept.SimpleWorkflowForTest$StringEvent"));
        final EventDefinition expectedOutput = new EventDefinition("com.flipkart.flux.client.intercept.SimpleWorkflowForTest$StringEvent1","com.flipkart.flux.client.intercept.SimpleWorkflowForTest$StringEvent");
        verify(localContext, times(1)).
            registerNewState(1l, "simpleStringModifyingTask", null, null,
                new MethodId(invokedMethod).toString()+_VERSION+"1", 2l, 2000l, expectedDependency, expectedOutput);
    }

    @Test
    public void testInterception_shouldSubmitNewState_methodWithTwoParam() throws Throwable {
        setupMockLocalContext();

        final Method invokedMethod = simpleWorkflowForTest.getClass().getDeclaredMethod("someTaskWithIntegerAndString", StringEvent.class, IntegerEvent.class);
        /* Third task intercepted */
        taskInterceptor.invoke(TestUtil.dummyInvocation(invokedMethod, new Object[]{new StringEvent("someEvent"), new IntegerEvent(1)}));
        /* Verifications */
        final List<EventDefinition> expectedDependencies = new LinkedList<EventDefinition>() {{
            add(new EventDefinition(STRING_EVENT_NAME+"0", "com.flipkart.flux.client.intercept.SimpleWorkflowForTest$StringEvent"));
            add(new EventDefinition(INTEGER_EVENT_NAME+"1", "com.flipkart.flux.client.intercept.SimpleWorkflowForTest$IntegerEvent"));
        }};
        EventDefinition expectedOutput = null; // Since the method returns void
        verify(localContext, times(1)).
            registerNewState(1l, "someTaskWithIntegerAndString", null, null,
                new MethodId(invokedMethod).toString()+_VERSION+"1", 0l, 1000l, expectedDependencies, expectedOutput);

    }

    @Test
    public void testInterception_shouldReturnSubclassOfGivenReturnType() throws Throwable {
        when(localContext.generateEventName(any(Event.class))).thenReturn("generatedEventName");
        final Method invokedMethod = simpleWorkflowForTest.getClass().getDeclaredMethod("simpleStringModifyingTask", StringEvent.class);
        final Object returnedObject = taskInterceptor.invoke(TestUtil.dummyInvocation(invokedMethod));
        assertThat(returnedObject).isNotNull();
        assertThat(StringEvent.class.isAssignableFrom(returnedObject.getClass())).isTrue();
        assertThat(((Event) returnedObject).name()).isEqualTo("generatedEventName");
    }

    @Test
    public void testInterception_returnedObjectShouldImplementInterceptedInterface() throws Throwable {
        when(localContext.generateEventName(any(Event.class))).thenReturn("generatedEventName");
        final Method invokedMethod = simpleWorkflowForTest.getClass().getDeclaredMethod("simpleStringModifyingTask", StringEvent.class);
        final Object returnedObject = taskInterceptor.invoke(TestUtil.dummyInvocation(invokedMethod));
        assertThat(returnedObject).isNotNull();
        assertThat(Intercepted.class.isAssignableFrom(returnedObject.getClass())).isTrue();
    }

    @Test
    public void testInterception_shouldReturnNothingIfMethodReturnsVoid() throws Throwable {
        final Method invokedMethod = simpleWorkflowForTest.getClass().getDeclaredMethod("someTaskWithIntegerAndString", StringEvent.class, IntegerEvent.class);
        final Object invoke = taskInterceptor.invoke(TestUtil.dummyInvocation(invokedMethod));
        assertThat(invoke).isNull();
    }

    @Test
    @Ignore
    public void shouldNotAllowVarArgMethods() throws Exception {
         // TODO still need to figure out if we should allow var arg methods or no.
    }

    @Test
    public void shouldRegisterTaskMethodsWithRegistry() throws Throwable {
        final Method invokedMethod = simpleWorkflowForTest.getClass().getDeclaredMethod("simpleStringModifyingTask", StringEvent.class);
        taskInterceptor.invoke(TestUtil.dummyInvocation(invokedMethod, simpleWorkflowForTest));
        final Executable expectedExecutable = new ExecutableImpl(simpleWorkflowForTest, invokedMethod, 2000l);
        verify(executableRegistry, times(1)).registerTask(new MethodId(invokedMethod).toString()+_VERSION+"1", expectedExecutable);
    }

    @Test
    public void shouldPassThroughIfItsNotPartOfWorkflowInterception() throws Throwable {
        when(localContext.isWorkflowInterception()).thenReturn(false);
        final MethodInvocation dummyInvocation = TestUtil.dummyInvocation(simpleWorkflowForTest.getClass().getDeclaredMethod("simpleStringModifyingTask", StringEvent.class), simpleWorkflowForTest);
        taskInterceptor.invoke(dummyInvocation);
        verify(localContext,times(1)).isWorkflowInterception();
        verifyNoMoreInteractions(localContext);
        verifyZeroInteractions(executableRegistry);
        assertThat(((MutableInt) ReflectionTestUtils.getField(dummyInvocation, "numProceedInvoctions")).getValue()).isEqualTo(1);
    }

    @Test(expected = IllegalSignatureException.class)
    public void testTaskInterception_BarkIfParametersDontExtendEvent() throws Throwable {
        final MethodInvocation dummyInvocation = TestUtil.dummyInvocation(simpleWorkflowForTest.getClass().getDeclaredMethod("badWorkflowWithNonEventParams", String.class), simpleWorkflowForTest);
        taskInterceptor.invoke(dummyInvocation);
    }

    @Test
    public void testRegisterExternalEventsWithTheirGivenName() throws Throwable {
        /* setup */
        setupMockLocalContext();
        final Method invokedMethod = simpleWorkflowForTest.getClass().getDeclaredMethod("waitForExternalEvent", StringEvent.class,IntegerEvent.class);

        /* invocation */
        taskInterceptor.invoke(TestUtil.dummyInvocation(invokedMethod,new Object[]{null,new IntegerEvent(1)}));

        /* verification */
        final List<EventDefinition> expectedDependency = new LinkedList<EventDefinition>() {{
            add(new EventDefinition("someExternalEvent", "com.flipkart.flux.client.intercept.SimpleWorkflowForTest$StringEvent"));
            add(new EventDefinition(INTEGER_EVENT_NAME + "0", "com.flipkart.flux.client.intercept.SimpleWorkflowForTest$IntegerEvent"));
            }};
        final EventDefinition expectedOutput = new EventDefinition("com.flipkart.flux.client.intercept.SimpleWorkflowForTest$StringEvent1","com.flipkart.flux.client.intercept.SimpleWorkflowForTest$StringEvent");
        verify(localContext, times(1)).
            registerNewState(1l, "waitForExternalEvent", null, null,
                new MethodId(invokedMethod).toString()+_VERSION+"1", 2l, 2000l, expectedDependency, expectedOutput);
    }

    @Test(expected = IllegalInvocationException.class)
    public void testExternalEvents_externalEventAnnotatedArgsShouldBeNull() throws Throwable {
        /* setup */
        setupMockLocalContext();
        final Method invokedMethod = simpleWorkflowForTest.getClass().getDeclaredMethod("waitForExternalEvent", StringEvent.class,IntegerEvent.class);

        /* invocation */
        taskInterceptor.invoke(TestUtil.dummyInvocation(invokedMethod, new Object[]{new StringEvent("foo"), new IntegerEvent(1)}));

    }

    @Test
    public void testExternalEvents_checkWithLocalContextForExistingDefs() throws Throwable {
        /* setup */
        setupMockLocalContext();
        /* Invocation 1 */
        final Method method1 = simpleWorkflowForTest.getClass().getDeclaredMethod("waitForExternalEvent", StringEvent.class, IntegerEvent.class);
        taskInterceptor.invoke(TestUtil.dummyInvocation(method1,new Object[]{null,new IntegerEvent(1)}));

        /* Invocation 2 - the method is annotated with externalEvent with same name and same type, so we're good */
        final Method method2 = simpleWorkflowForTest.getClass().getDeclaredMethod("waitForExternalEvent", StringEvent.class);
        taskInterceptor.invoke(TestUtil.dummyInvocation(method2,new Object[]{null}));

        /* Invocation 3 - the method is annotated with externalEvent with same name but different type, so it will fail if passed to the real context */
        final Method method3 = simpleWorkflowForTest.getClass().getDeclaredMethod("waitForExternalEvent", IntegerEvent.class);
        taskInterceptor.invoke(TestUtil.dummyInvocation(method3, new Object[]{null}));
        verify(localContext,times(2)).checkExistingDefinition(new EventDefinition("someExternalEvent", "com.flipkart.flux.client.intercept.SimpleWorkflowForTest$StringEvent"));
        verify(localContext,times(1)).checkExistingDefinition(new EventDefinition("someExternalEvent","com.flipkart.flux.client.intercept.SimpleWorkflowForTest$IntegerEvent"));
    }

    private void setupMockLocalContext() {
        final AtomicInteger eventCounter = new AtomicInteger(0);
        doAnswer(invocation -> (((Event)invocation.getArguments()[0]).name())+eventCounter.getAndIncrement()).when(localContext).generateEventName(any(Event.class));
    }

}