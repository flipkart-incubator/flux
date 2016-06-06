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
 */

package com.flipkart.flux;

import akka.actor.UntypedActor;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class MockActorRef extends UntypedActor {
    private Map<Object,MutableInt> receivedMessages = new HashMap<>();

    @Override
    public void onReceive(Object message) throws Exception {
        if (!receivedMessages.containsKey(message)) {
            receivedMessages.put(message,new MutableInt(0));
        }
        receivedMessages.get(message).increment();
    }
    public void assertMessageReceived(Object message, int expectedCount) {
        assertThat(receivedMessages.get(message)).isNotNull();
        assertThat(receivedMessages.get(message).intValue()).isEqualTo(expectedCount);
    }
}
