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

package com.flipkart.flux.impl.task;

import com.flipkart.flux.client.intercept.UnknownIdentifierException;
import com.flipkart.flux.client.registry.Executable;
import com.flipkart.flux.client.registry.ExecutableImpl;
import com.flipkart.flux.client.registry.ExecutableRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TaskRegistryTest {

    @Mock
    ExecutableRegistry executableRegistry;

    private TaskRegistry taskRegistry;

    @Before
    public void setUp() throws Exception {
        taskRegistry = new TaskRegistry(executableRegistry);
    }

    @Test
    public void testRetrieve_shouldReturnTaskGivenIdentifier() throws Exception {
        final Object someObject = new Object();
        final Executable givenExecutable = new ExecutableImpl(someObject, someObject.getClass().getDeclaredMethod("toString"), 10);
        when(executableRegistry.getTask(anyString())).thenReturn(givenExecutable);
        assertThat(taskRegistry.retrieveTask("com.flipkart.flux.impl.SomeWorkflow_simpleStringModifyingTask_java.lang.String_java.lang.String")).isEqualTo(new LocalJvmTask(givenExecutable));
        verify(executableRegistry, times(1)).getTask("com.flipkart.flux.impl.SomeWorkflow_simpleStringModifyingTask_java.lang.String_java.lang.String");
    }

    @Test(expected = UnknownIdentifierException.class)
    public void testRetrieve_shouldBombIfTaskCannotBeFound() throws Exception {
        when(executableRegistry.getTask(anyString())).thenThrow(new UnknownIdentifierException("blah"));
        taskRegistry.retrieveTask("com.flipkart.flux.impl.SomeWorkflow_simpleStringModifyingTask_java.lang.String_java.lang.String");
    }
}