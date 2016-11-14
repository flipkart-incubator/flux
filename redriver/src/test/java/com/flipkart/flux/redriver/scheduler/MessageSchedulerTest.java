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
import com.flipkart.flux.redriver.service.MessageManagerService;
import com.flipkart.flux.task.redriver.RedriverRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.PriorityQueue;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MessageSchedulerTest {
    @Mock
    MessageManagerService messageManagerService;

    @Mock
    PriorityQueue<ScheduledMessage> messages;

    @Mock
    RedriverRegistry redriverRegistry;

    private MessageScheduler messageScheduler;

    @Before
    public void setUp() throws Exception {
        messageScheduler = new MessageScheduler(messageManagerService, messages, redriverRegistry, 10);
    }

    @Test
    public void testAddMessage_shouldAddToDbAndPriorityQueue() throws Exception {
        final ScheduledMessage testMessage = new ScheduledMessage(121l, 200l);
        messageScheduler.addMessage(testMessage);

        Thread.sleep(1000);

        verify(messageManagerService, times(1)).saveMessage(testMessage);
        verifyNoMoreInteractions(messageManagerService);

        verify(messages, times(1)).add(testMessage);
    }

    @Test
    public void testRemoveMessage_shouldRemoveFromPriorityQueueAndScheduleForRemoval() throws Exception {
        final ScheduledMessage testMessage = new ScheduledMessage(121l, 200l);
        PriorityQueue<ScheduledMessage> localMessageQueue = new PriorityQueue<>();
        localMessageQueue.add(testMessage);
        messageScheduler = new MessageScheduler(messageManagerService, localMessageQueue, redriverRegistry, 10);
        messageScheduler.removeMessage(121l);

        assertTrue(localMessageQueue.isEmpty());

        verify(messageManagerService,times(1)).scheduleForRemoval(testMessage.getTaskId());
        verifyNoMoreInteractions(messageManagerService);
    }

    @Test
    public void testScheduledExecution() throws Exception {
        messageScheduler =  new MessageScheduler(messageManagerService,redriverRegistry, 10);
        final long currTime = System.currentTimeMillis();
        messageScheduler.addMessage(new ScheduledMessage(121l, currTime+1000l));
        messageScheduler.addMessage(new ScheduledMessage(122l, currTime+1000l));
        messageScheduler.start();
        verifyZeroInteractions(redriverRegistry); // Nothing scheduled yet
        messageScheduler.addMessage(new ScheduledMessage(123l, currTime + 1000l));
        messageScheduler.addMessage(new ScheduledMessage(124l, currTime + 2000l));
        Thread.sleep(1100l);
        verify(redriverRegistry,times(1)).redriveTask(121l);
        verify(redriverRegistry,times(1)).redriveTask(122l);
        verify(redriverRegistry,times(1)).redriveTask(123l);
        verifyNoMoreInteractions(redriverRegistry);
        Thread.sleep(1100l);
        verify(redriverRegistry,times(1)).redriveTask(124l);
        verifyNoMoreInteractions(redriverRegistry);
    }

    @Test
    public void testRemovalAfterExecution() throws Exception {
        messageScheduler =  new MessageScheduler(messageManagerService,redriverRegistry, 10);
        final long currTime = System.currentTimeMillis();
        final ScheduledMessage testMessage1 = new ScheduledMessage(121l, currTime + 800l);
        final ScheduledMessage testMessage2 = new ScheduledMessage(122l, currTime + 800l);
        messageScheduler.addMessage(testMessage1);
        messageScheduler.addMessage(testMessage2);
        messageScheduler.start();
        Thread.sleep(1100l);
        verify(redriverRegistry,times(2)).redriveTask(anyLong());
        verify(messageManagerService,times(1)).scheduleForRemoval(testMessage1.getTaskId());
        verify(messageManagerService,times(1)).scheduleForRemoval(testMessage2.getTaskId());
    }
}