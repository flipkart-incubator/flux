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
import com.flipkart.flux.client.intercept.SimpleWorkflowForTest.StringEvent;
import com.flipkart.flux.client.intercept.UnknownIdentifierException;
import com.google.inject.Injector;
import com.google.inject.Provider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LocalExecutableRegistryImplTest {

    private LocalExecutableRegistryImpl localExecutableRegistry;
    private SimpleWorkflowForTest simpleWorkflowForTest;

    @Mock
    Injector injector;

    @Mock
    Provider<SimpleWorkflowForTest> simpleWorkflowForTestProvider;

    private HashMap<String, Executable> identifierToMethodMap;

    @Before
    public void setUp() throws Exception {
        simpleWorkflowForTest = new SimpleWorkflowForTest();
        identifierToMethodMap = new HashMap<>();
        localExecutableRegistry = new LocalExecutableRegistryImpl(identifierToMethodMap,injector);
    }

    @Test
    public void testRegisterNewTask_shouldStoreExecutableInCache() throws Exception {
        localExecutableRegistry.registerTask("fooBar", new ExecutableImpl(simpleWorkflowForTest,simpleWorkflowForTest.getClass().getDeclaredMethod("simpleStringModifyingTask", StringEvent.class), 2000l));
        assertThat(identifierToMethodMap.containsKey("fooBar")).isTrue();
        assertThat(identifierToMethodMap.get("fooBar")).isEqualTo(new ExecutableImpl(simpleWorkflowForTest, simpleWorkflowForTest.getClass().getDeclaredMethod("simpleStringModifyingTask", StringEvent.class), 2000l));
    }

    @Test
    public void testRegisterNewTask_gracefullyHandleDuplicates() throws Exception {
        localExecutableRegistry.registerTask("fooBar",new ExecutableImpl(simpleWorkflowForTest,simpleWorkflowForTest.getClass().getDeclaredMethod("simpleStringModifyingTask", StringEvent.class), 2000l));
        localExecutableRegistry.registerTask("fooBar",new ExecutableImpl(simpleWorkflowForTest,simpleWorkflowForTest.getClass().getDeclaredMethod("simpleStringModifyingTask", StringEvent.class), 2000l));
        assertThat(identifierToMethodMap.containsKey("fooBar")).isTrue();
        assertThat(identifierToMethodMap.get("fooBar")).isEqualTo(new ExecutableImpl(simpleWorkflowForTest,simpleWorkflowForTest.getClass().getDeclaredMethod("simpleStringModifyingTask", StringEvent.class), 2000l));
    }

    @Test
    public void testTaskRetrieval_shouldRetrieveRegisteredExecutables() throws Exception {
        final Executable givenExecutable = new ExecutableImpl(simpleWorkflowForTest, simpleWorkflowForTest.getClass().getDeclaredMethod("simpleStringModifyingTask", StringEvent.class), 2000l);
        localExecutableRegistry.registerTask("fooBar", givenExecutable);
        assertThat(localExecutableRegistry.getTask("fooBar")).isEqualTo(givenExecutable);
    }

    @Test
    public void testTaskRetrieval_shouldTryToRetrieveNonRegisteredMethods() throws Exception {
        /* Test Setup */
        when(injector.getInstance(any(Class.class))).thenReturn(simpleWorkflowForTest);

        /* actual test */
        final Executable expectedExecutable = new ExecutableImpl(simpleWorkflowForTest, simpleWorkflowForTest.getClass().getDeclaredMethod("simpleStringModifyingTask", StringEvent.class), 2000l);
        assertThat(
            localExecutableRegistry.getTask("com.flipkart.flux.client.intercept.SimpleWorkflowForTest_simpleStringModifyingTask_com.flipkart.flux.client.intercept.SimpleWorkflowForTest$StringEvent_com.flipkart.flux.client.intercept.SimpleWorkflowForTest$StringEvent_version1"))
        .isEqualTo(expectedExecutable);
        org.mockito.Mockito.verify(injector,times(1)).getInstance(Class.forName("com.flipkart.flux.client.intercept.SimpleWorkflowForTest"));
    }

    @Test(expected = UnknownIdentifierException.class)
    public void testTaskRetrieval_shouldBombOnUnknownClasses() throws Exception {
        localExecutableRegistry.getTask("com.Foo.NonExistent.Class_someMethodName_com.Foo.NonExistant.SomeReturnType_version1");
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