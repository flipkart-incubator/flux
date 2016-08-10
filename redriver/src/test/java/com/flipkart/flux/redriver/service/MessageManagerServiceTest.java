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

package com.flipkart.flux.redriver.service;

import com.codahale.metrics.InstrumentedScheduledExecutorService;
import com.flipkart.flux.redriver.dao.MessageDao;
import com.flipkart.flux.redriver.model.ScheduledMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class MessageManagerServiceTest {

    @Mock
    MessageDao messageDao;
    private MessageManagerService messageManagerService;

    @Before
    public void setup() {

    }

    @Test
    public void testRemoval_shouldDeferRemoval() throws Exception {
        messageManagerService = new MessageManagerService(messageDao,500l,10);
        messageManagerService.initialize(); // Will be called by polyguice in the production env

        messageManagerService.scheduleForRemoval(new ScheduledMessage("testMessage1", "fooBar", 1l, "url1"));
        messageManagerService.scheduleForRemoval(new ScheduledMessage("testMessage2", "fooBar", 1l, "url1"));
        messageManagerService.scheduleForRemoval(new ScheduledMessage("testMessage3", "fooBar", 1l, "url1"));

        verifyZeroInteractions(messageDao);

        Thread.sleep(700l);
        verify(messageDao,times(1)).deleteInBatch(anyListOf(String.class));

    }

    @Test
    public void testRemoval_shouldDeleteInBatches() throws Exception {
        messageManagerService = new MessageManagerService(messageDao,500l,2);
        messageManagerService.initialize(); // Will be called by polyguice in the production env

        messageManagerService.scheduleForRemoval(new ScheduledMessage("testMessage1", "fooBar", 1l, "url1"));
        messageManagerService.scheduleForRemoval(new ScheduledMessage("testMessage2", "fooBar", 1l, "url1"));
        messageManagerService.scheduleForRemoval(new ScheduledMessage("testMessage3", "fooBar", 1l, "url1"));

        verifyZeroInteractions(messageDao);

        Thread.sleep(700l);
        verify(messageDao,times(1)).deleteInBatch(Arrays.asList("testMessage1","testMessage2"));

        Thread.sleep(700l);
        verify(messageDao,times(1)).deleteInBatch(Arrays.asList("testMessage3"));


    }

    @Test
    public void testExecutorPool() throws Exception {
        final ExecutorService executorService = Executors.newFixedThreadPool(1);
        final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);

        final Future<Object> result = executorService.submit(() -> {
            throw new RuntimeException("fooBarBaaz");
        });

        Thread.sleep(200l);

        System.out.println("Done:" + result.isDone());
        final Future<Object> result2 = executorService.submit(() -> {
            System.out.println("Yo man");
            return null;
        });
        Thread.sleep(200l);
        System.out.println("result2.isDone() = " + result2.isDone());
    }
}