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

package com.flipkart.flux.client.registry;

import com.flipkart.flux.client.intercept.SimpleWorkflowForTest;
import com.flipkart.flux.client.intercept.UnknownIdentifierException;
import com.google.inject.Injector;
import com.google.inject.Provider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Method;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LocalExecutableRegistryImplTest {

    private LocalExecutableRegistryImpl localExecutableRegistry;
    private SimpleWorkflowForTest simpleWorkflowForTest;

    @Mock
    Injector injector;

    @Mock
    Provider<SimpleWorkflowForTest> simpleWorkflowForTestProvider;

    private HashMap<String, Method> identifierToMethodMap;

    @Before
    public void setUp() throws Exception {
        simpleWorkflowForTest = new SimpleWorkflowForTest();
        identifierToMethodMap = new HashMap<>();
        localExecutableRegistry = new LocalExecutableRegistryImpl(identifierToMethodMap,injector);
    }

    @Test
    public void testRegisterNewTask_shouldStoreMethodObjectInCache() throws Exception {
        localExecutableRegistry.registerTask("fooBar", simpleWorkflowForTest.getClass().getDeclaredMethod("simpleStringModifyingTask", String.class));
        assertThat(identifierToMethodMap.containsKey("fooBar")).isTrue();
        assertThat(identifierToMethodMap.get("fooBar").getName()).contains("simpleStringModifyingTask");
    }

    @Test
    public void testRegisterNewTask_gracefullyHandleDuplicates() throws Exception {
        localExecutableRegistry.registerTask("fooBar", simpleWorkflowForTest.getClass().getDeclaredMethod("simpleStringModifyingTask", String.class));
        localExecutableRegistry.registerTask("fooBar", simpleWorkflowForTest.getClass().getDeclaredMethod("simpleStringModifyingTask", String.class));
        assertThat(identifierToMethodMap.containsKey("fooBar")).isTrue();
        assertThat(identifierToMethodMap.get("fooBar").getName()).contains("simpleStringModifyingTask");
    }

    @Test
    public void testTaskRetrieval_shouldRetrieveRegisteredMethods() throws Exception {
        final Method givenMethod = simpleWorkflowForTest.getClass().getDeclaredMethod("simpleStringModifyingTask", String.class);
        localExecutableRegistry.registerTask("fooBar", givenMethod);
        assertThat(localExecutableRegistry.getTask("fooBar")).isEqualTo(givenMethod);
    }

    @Test
    public void testTaskRetrieval_shouldTryToRetrieveNonRegisteredMethods() throws Exception {
        /* Test Setup */
        final SimpleWorkflowForTest mockSimpleWorkflow = new SimpleWorkflowForTest();
        when(injector.getInstance(any(Class.class))).thenReturn(mockSimpleWorkflow);

        /* actual test */
        final Method expectedMethod = simpleWorkflowForTest.getClass().getDeclaredMethod("simpleStringModifyingTask", String.class);
        assertThat(
            localExecutableRegistry.getTask("com.flipkart.flux.client.intercept.SimpleWorkflowForTest_simpleStringModifyingTask_java.lang.String_java.lang.String"))
        .isEqualTo(expectedMethod);
        org.mockito.Mockito.verify(injector,times(1)).getInstance(Class.forName("com.flipkart.flux.client.intercept.SimpleWorkflowForTest"));
    }

    @Test(expected = UnknownIdentifierException.class)
    public void testTaskRetrieval_shouldBombOnUnknownClasses() throws Exception {
        localExecutableRegistry.getTask("com.Foo.NonExistent.Class_someMethodName_com.Foo.NonExistant.SomeReturnType");
    }

    @Test(expected = UnknownIdentifierException.class)
    public void testTaskRetrieval_shouldBombOnIncorrectSignatures() throws Exception {
        /* Test Setup */
        final SimpleWorkflowForTest mockSimpleWorkflow = new SimpleWorkflowForTest();
        when(injector.getInstance(any(Class.class))).thenReturn(mockSimpleWorkflow);
        // We try retrieving a method signature that doesn't exist in the class
        localExecutableRegistry.getTask("com.flipkart.flux.client.intercept.SimpleWorkflowForTest_simpleStringModifyingTask_java.lang.String_java.lang.String_java.lang.String");
    }

    @Test(expected = UnknownIdentifierException.class)
    public void testTaskRetrieval_shouldBombOnIncorrectMethodNames() throws Exception {
        final SimpleWorkflowForTest mockSimpleWorkflow = new SimpleWorkflowForTest();
        when(injector.getInstance(any(Class.class))).thenReturn(mockSimpleWorkflow);
        // We try retrieving a method signature that doesn't exist in the class
        localExecutableRegistry.getTask("com.flipkart.flux.client.intercept.SimpleWorkflowForTest_simpleStringModifyingTaskChangedName_java.lang.String_java.lang.String");
    }
}