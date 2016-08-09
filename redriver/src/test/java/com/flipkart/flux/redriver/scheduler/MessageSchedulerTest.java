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

package com.flipkart.flux.redriver.scheduler;

import com.flipkart.flux.redriver.model.ScheduledMessage;
import com.flipkart.flux.redriver.model.ScheduledMessageComparator;
import com.flipkart.flux.redriver.service.MessageManagerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.PriorityQueue;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MessageSchedulerTest {
    @Mock
    MessageManagerService messageManagerService;

    @Mock
    PriorityQueue<ScheduledMessage> messages;

    private MessageScheduler messageScheduler;

    @Before
    public void setUp() throws Exception {
        messageScheduler = new MessageScheduler(messageManagerService, messages);
    }

    @Test
    public void testAddMessage_shouldAddToDbAndPriorityQueue() throws Exception {
        final ScheduledMessage testMessage = new ScheduledMessage("1234", "someData", 200l, "someUrl");
        messageScheduler.addMessage(testMessage);

        verify(messageManagerService, times(1)).saveMessage(testMessage);
        verifyNoMoreInteractions(messageManagerService);

        verify(messages, times(1)).add(testMessage);
    }

    @Test
    public void testRemoveMessage_shouldRemoveFromPriorityQueueAndScheduleForRemoval() throws Exception {
        final ScheduledMessage testMessage = new ScheduledMessage("1234", "someData", 200l, "someUrl");
        PriorityQueue<ScheduledMessage> localMessageQueue = new PriorityQueue<>();
        localMessageQueue.add(testMessage);
        messageScheduler = new MessageScheduler(messageManagerService, localMessageQueue);
        messageScheduler.removeMessage("1234");

        assertTrue(localMessageQueue.isEmpty());

        verify(messageManagerService,times(1)).scheduleForRemoval(testMessage);
        verifyNoMoreInteractions(messageManagerService);
    }

    @Test
    public void testRemoveMessage_shouldBeGracefullInFaceOfUnknownMessageId() throws Exception {
        final PriorityQueue<ScheduledMessage> localMessageQueue = new PriorityQueue<>();
        messageScheduler = new MessageScheduler(messageManagerService, localMessageQueue);
        messageScheduler.removeMessage("1234");
        assertTrue(localMessageQueue.isEmpty());
        verifyZeroInteractions(messageManagerService);
    }
}