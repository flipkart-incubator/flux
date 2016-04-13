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


import org.apache.commons.lang3.mutable.MutableObject;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LocalContextTest {
    LocalContext localContext;
    private ThreadLocal<String> stringThreadLocal;

    @Before
    public void setUp() throws Exception {
        stringThreadLocal = new ThreadLocal<>();
        localContext = new LocalContext(stringThreadLocal);
    }

    @Test
    public void testRegisterNew_shouldCreateALocalRegistration() throws Exception {
        localContext.registerNew("fooBar");
        assertThat(stringThreadLocal.get()).isEqualTo("fooBar");
    }

    @Test(expected = IllegalStateException.class)
    public void shouldDisallowDuplicateRegistrations() throws Exception {
        localContext.registerNew("fooBar");
        localContext.registerNew("fooBar");
    }

    @Test
    public void shouldAllowSameMethodRegistrationFromDifferentThreads() throws Exception {

        final MutableObject<String> settableString1 = new MutableObject<>(null);
        final MutableObject<String> settableString2 = new MutableObject<>(null);

        final Thread thread1 = new Thread(() -> {
            localContext.registerNew("foobar");
            settableString1.setValue(stringThreadLocal.get());
        });
        final Thread thread2 = new Thread(() -> {
            localContext.registerNew("foobar");
            settableString2.setValue(stringThreadLocal.get());
        });
        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        assertThat(settableString1.getValue()).isNotNull().isEqualTo(settableString2.getValue()).isEqualTo("foobar");
    }
}