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
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
        taskInterceptor = new TaskInterceptor(localContext,executableRegistry);
        simpleWorkflowForTest = new SimpleWorkflowForTest();
        when(localContext.isWorkflowInterception()).thenReturn(true);
    }

    @Test
    public void testInterception_shouldSubmitNewState_methodWithOneParam() throws Throwable {
        taskInterceptor.invoke(TestUtil.dummyInvocation(simpleWorkflowForTest.getClass().getDeclaredMethod("simpleStringModifyingTask", String.class)));

        final Set<EventDefinition> expectedEventDef =
            Collections.singleton(new EventDefinition("com.flipkart.flux.client.intercept.SimpleWorkflowForTest_simpleStringModifyingTask_java.lang.String_arg0","")); //TODO: CHANGE IT
        verify(localContext, times(1)).
            registerNewState(2l, "simpleStringModifyingTask", null, null,
                "com.flipkart.flux.client.intercept.SimpleWorkflowForTest_simpleStringModifyingTask_java.lang.String_java.lang.String", 2l, 2000l, expectedEventDef);

    }

    @Test
    public void testInterception_shouldSubmitNewState_methodWithTwoParam() throws Throwable {
        taskInterceptor.invoke(TestUtil.dummyInvocation(simpleWorkflowForTest.getClass().getDeclaredMethod("someTaskWithIntegerAndString", String.class,Integer.class)));
        /* Third task intercepted */
        final Set<EventDefinition> expectedEventDefs = new HashSet<>();
        expectedEventDefs.add(new EventDefinition("com.flipkart.flux.client.intercept.SimpleWorkflowForTest_someTaskWithIntegerAndString_java.lang.String_arg0","")); //TODO: CHANGE IT
        expectedEventDefs.add(new EventDefinition("com.flipkart.flux.client.intercept.SimpleWorkflowForTest_someTaskWithIntegerAndString_java.lang.Integer_arg1","")); //TODO: CHANGE IT
        verify(localContext, times(1)).
            registerNewState(3l, "someTaskWithIntegerAndString", null, null,
                "com.flipkart.flux.client.intercept.SimpleWorkflowForTest_someTaskWithIntegerAndString_void_java.lang.String_java.lang.Integer", 0l, 1000l, expectedEventDefs);

    }

    @Test
    @Ignore
    public void shouldNotAllowVarArgMethods() throws Exception {
         // TODO still need to figure out if we should allow var arg methods or no.
    }

    @Test
    public void shouldRegisterTaskMethodsWithRegistry() throws Throwable {
        taskInterceptor.invoke(TestUtil.dummyInvocation(simpleWorkflowForTest.getClass().getDeclaredMethod("simpleStringModifyingTask", String.class), simpleWorkflowForTest));
        final Executable expectedExecutable = new ExecutableImpl(simpleWorkflowForTest,simpleWorkflowForTest.getClass().getDeclaredMethod("simpleStringModifyingTask", String.class), 2000l);
        verify(executableRegistry,times(1)).registerTask("com.flipkart.flux.client.intercept.SimpleWorkflowForTest_simpleStringModifyingTask_java.lang.String_java.lang.String",expectedExecutable);
    }

    @Test
    public void shouldPassThroughIfItsNotPartOfWorkflowInterception() throws Throwable {
        when(localContext.isWorkflowInterception()).thenReturn(false);
        final MethodInvocation dummyInvocation = TestUtil.dummyInvocation(simpleWorkflowForTest.getClass().getDeclaredMethod("simpleStringModifyingTask", String.class), simpleWorkflowForTest);
        taskInterceptor.invoke(dummyInvocation);
        verify(localContext,times(1)).isWorkflowInterception();
        verifyNoMoreInteractions(localContext);
        verifyZeroInteractions(executableRegistry);
        assertThat(((MutableInt) ReflectionTestUtils.getField(dummyInvocation, "numProceedInvoctions")).getValue()).isEqualTo(1);
    }
}