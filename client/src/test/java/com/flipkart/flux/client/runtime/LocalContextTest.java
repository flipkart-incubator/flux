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

package com.flipkart.flux.client.runtime;


import com.flipkart.flux.api.EventDefinition;
import com.flipkart.flux.api.StateDefinition;
import com.flipkart.flux.api.StateMachineDefinition;
import com.flipkart.flux.client.intercept.SimpleWorkflowForTest;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

public class LocalContextTest {
    LocalContext localContext;
    private ThreadLocal<StateMachineDefinition> tlStateMachineDef;
    private ThreadLocal<MutableInt> tlMutableInteger;

    @Before
    public void setUp() throws Exception {
        tlStateMachineDef = new ThreadLocal<>();
        tlMutableInteger = new ThreadLocal<>();
        localContext = new LocalContext(tlStateMachineDef, tlMutableInteger, new ThreadLocal<>());
    }

    @Test
    public void testRegisterNew_shouldCreateALocalRegistration() throws Exception {
        localContext.registerNew("fooBar", 1, "someDescription","someContextId");
        assertThat(tlStateMachineDef.get()).isEqualTo(new StateMachineDefinition("someDescription", "fooBar", 1l, new HashSet<>(), new HashSet<>(), "someContextId"));
        assertThat(tlMutableInteger.get().intValue()).isEqualTo(0);
    }

    @Test
    public void testReset() throws Exception {
        localContext.registerNew("fooBar", 1, "someDescription","someContextId");
        localContext.reset();
        assertThat(tlStateMachineDef.get()).isNull();
        assertThat(tlMutableInteger.get()).isNull();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldDisallowDuplicateRegistrations() throws Exception {
        localContext.registerNew("fooBar", 1, "someDescription","someContextId");
        localContext.registerNew("fooBar", 1, "someOtherDescription","someOtherContextId");
    }

    @Test
    public void shouldAllowSameMethodRegistrationFromDifferentThreads() throws Exception {

        final MutableObject<StateMachineDefinition> definitionOne = new MutableObject<>(null);
        final MutableObject<StateMachineDefinition> definitionTwo = new MutableObject<>(null);

        final Thread thread1 = new Thread(() -> {
            localContext.registerNew("fooBar", 1, "someDescription","someContext");
            definitionOne.setValue(tlStateMachineDef.get());
        });
        final Thread thread2 = new Thread(() -> {
            localContext.registerNew("fooBar", 1, "someDescription","someContext");
            definitionTwo.setValue(tlStateMachineDef.get());
        });
        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        assertThat(definitionOne.getValue()).isNotNull().isEqualTo(definitionTwo.getValue()).isEqualTo(new StateMachineDefinition("someDescription", "fooBar", 1l, new HashSet<>(), new HashSet<>(), "someContext"));
    }

    @Test
    public void testRegisterNewState() throws Exception {
        localContext.registerNew("fooBar", 1, "someDescription",null);
        EventDefinition dummyDependency = new EventDefinition("someName","com.blah.some.Event");
        EventDefinition dummyOutput = new EventDefinition("someOutput","com.blah.some.Event");
        localContext.registerNewState(1l, "someState", null, "com.blah.some.Hook", "com.blah.some.Task", 1l, 1000l, Collections.singletonList(dummyDependency), dummyOutput);
        StateDefinition expectedStateDefinition = new StateDefinition(1l,"someState",null,
            "com.blah.some.Hook", "com.blah.some.Task", "com.blah.some.Hook",
            1l, 1000l, Collections.singletonList(dummyDependency), dummyOutput);
        assertThat(tlStateMachineDef.get()).isEqualTo(new StateMachineDefinition("someDescription", "fooBar", 1l, Collections.singleton(expectedStateDefinition), new HashSet<>(), null));
    }

    @Test
    public void testGet_shouldReturnStateMachineDefinition() throws Exception {
        localContext.registerNew("fooBar", 1, "someDescription","someCtx");
        assertThat(localContext.getStateMachineDef()).isEqualTo(new StateMachineDefinition("someDescription", "fooBar", 1l, new HashSet<>(), new HashSet<>(), "someCtx"));
    }

    @Test
    public void testEventName_ReturnEventNameAppendedWithIncreasingCount() throws Exception {
        localContext.registerNew("fooBar", 1, "someDescription",null);
        final SimpleWorkflowForTest.StringEvent event1 = new SimpleWorkflowForTest.StringEvent("foo");
        final SimpleWorkflowForTest.StringEvent event2 = new SimpleWorkflowForTest.StringEvent("foo");
        assertThat(event1).isEqualTo(event2); // Basically, we have object-equality but referential inequality
        assertThat(localContext.generateEventName(event1)).isEqualTo(SimpleWorkflowForTest.STRING_EVENT_NAME+"0");
        assertThat(localContext.generateEventName(event2)).isEqualTo(SimpleWorkflowForTest.STRING_EVENT_NAME+"1");
    }

    @Test
    public void testEventName_ReturnSameEventNameForKnownObject() throws Exception {
        localContext.registerNew("fooBar", 1, "someDescription",null);
        final SimpleWorkflowForTest.StringEvent event1 = new SimpleWorkflowForTest.StringEvent("foo");
        assertThat(localContext.generateEventName(event1)).isEqualTo(SimpleWorkflowForTest.STRING_EVENT_NAME+"0");
        assertThat(localContext.generateEventName(event1)).isEqualTo(SimpleWorkflowForTest.STRING_EVENT_NAME+"0");
    }
}