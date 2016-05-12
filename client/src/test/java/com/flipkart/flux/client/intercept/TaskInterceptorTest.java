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

import com.flipkart.flux.client.runtime.LocalContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.fail;

@RunWith(MockitoJUnitRunner.class)
public class TaskInterceptorTest {
    @Mock
    LocalContext localContext;

    private SimpleWorkflowForTest simpleWorkflowForTest;
    private TaskInterceptor taskInterceptor;

    @Before
    public void setUp() throws Exception {
        taskInterceptor = new TaskInterceptor(localContext);
        simpleWorkflowForTest = new SimpleWorkflowForTest();
    }

//    @Test
//    public void testInterception_shouldSubmitNewState_methodWithOneParam() throws Throwable {
//        taskInterceptor.invoke(TestUtil.dummyInvocation(simpleWorkflowForTest.getClass().getDeclaredMethod("simpleStringModifyingTask", String.class)));
//
//        final Set<EventDefinition> expectedEventDef =
//            Collections.singleton(new EventDefinition("com.flipkart.flux.client.intercept.SimpleWorkflowForTest_simpleStringModifyingTask_java.lang.String_arg0"));
//        org.mockito.Mockito.verify(localContext, times(1)).
//            registerNewState(2l, "simpleStringModifyingTask", null, null,
//                "com.flipkart.flux.client.intercept.SimpleWorkflowForTest_simpleStringModifyingTask_java.lang.String_java.lang.String", 2l, 2000l, expectedEventDef);
//
//    }
//
//    @Test
//    public void testInterception_shouldSubmitNewState_methodWithTwoParam() throws Throwable {
//        taskInterceptor.invoke(TestUtil.dummyInvocation(simpleWorkflowForTest.getClass().getDeclaredMethod("someTaskWithIntegerAndString", String.class,Integer.class)));
//        /* Third task intercepted */
//        final Set<EventDefinition> expectedEventDefs = new HashSet<>();
//        expectedEventDefs.add(new EventDefinition("com.flipkart.flux.client.intercept.SimpleWorkflowForTest_someTaskWithIntegerAndString_java.lang.String_arg0"));
//        expectedEventDefs.add(new EventDefinition("com.flipkart.flux.client.intercept.SimpleWorkflowForTest_someTaskWithIntegerAndString_java.lang.Integer_arg1"));
//        org.mockito.Mockito.verify(localContext, times(1)).
//            registerNewState(3l, "someTaskWithIntegerAndString", null, null,
//                "com.flipkart.flux.client.intercept.SimpleWorkflowForTest_someTaskWithIntegerAndString_void_java.lang.String_java.lang.Integer", 0l, 1000l, expectedEventDefs);
//
//    }

    @Test
    public void shouldNotAllowVarArgMethods() throws Exception {
        fail("todo"); // TODO still need to figure out if we should allow var arg methods or no.

    }
}