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


import com.flipkart.flux.api.StateMachineDefinition;
import org.apache.commons.lang3.mutable.MutableObject;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LocalContextTest {
    LocalContext localContext;
    private ThreadLocal<StateMachineDefinition> threadLocal;

    @Before
    public void setUp() throws Exception {
        threadLocal = new ThreadLocal<>();
        localContext = new LocalContext(threadLocal);
    }

    @Test
    public void testRegisterNew_shouldCreateALocalRegistration() throws Exception {
        localContext.registerNew("fooBar", 1, "someDescription");
        assertThat(threadLocal.get()).isEqualTo(new StateMachineDefinition("someDescription","fooBar",1l));
    }

    @Test
    public void testReset() throws Exception {
        localContext.registerNew("fooBar", 1, "someDescription");
        localContext.reset();
        assertThat(threadLocal.get()).isNull();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldDisallowDuplicateRegistrations() throws Exception {
        localContext.registerNew("fooBar", 1, "someDescription");
        localContext.registerNew("fooBar", 1, "someOtherDescription");
    }

    @Test
    public void shouldAllowSameMethodRegistrationFromDifferentThreads() throws Exception {

        final MutableObject<StateMachineDefinition> definitionOne = new MutableObject<>(null);
        final MutableObject<StateMachineDefinition> definitionTwo = new MutableObject<>(null);

        final Thread thread1 = new Thread(() -> {
            localContext.registerNew("fooBar", 1, "someDescription");
            definitionOne.setValue(threadLocal.get());
        });
        final Thread thread2 = new Thread(() -> {
            localContext.registerNew("fooBar", 1, "someDescription");
            definitionTwo.setValue(threadLocal.get());
        });
        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        assertThat(definitionOne.getValue()).isNotNull().isEqualTo(definitionTwo.getValue()).isEqualTo(new StateMachineDefinition("someDescription","fooBar",1l));
    }

}