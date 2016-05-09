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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MethodIdTest {
    @Test
    public void testCreationFromGivenIdentifier() throws Exception {
        final MethodId generated = MethodId.fromIdentifier("com.flipkart.flux.client.intercept.SimpleWorkflowForTest_simpleStringModifyingTask_java.lang.String_java.lang.String");
        final MethodId expected = new MethodId(new SimpleWorkflowForTest().getClass().getDeclaredMethod("simpleStringModifyingTask", String.class));
        assertThat(generated).isEqualTo(expected);
    }

    @Test(expected = MalformedIdentifierException.class)
    public void testCreationFromIdentifier_shouldBombOnMalformedIdentifiers() throws Exception {
        MethodId.fromIdentifier("badIdentifier_withOnlyOneUnderscore");

    }
}