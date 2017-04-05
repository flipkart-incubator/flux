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

package com.flipkart.flux.eventscheduler.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.eventscheduler.dao.EventSchedulerDao;
import com.flipkart.flux.eventscheduler.model.ScheduledEvent;
import com.flipkart.flux.task.eventscheduler.EventSchedulerRegistry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Mockito.*;

/**
 * @author shyam.akirala
 */
@RunWith(MockitoJUnitRunner.class)
public class EventSchedulerServiceTest {

    @Mock
    EventSchedulerDao eventSchedulerDao;

    @Mock
    EventSchedulerRegistry eventSchedulerRegistry;

    private EventSchedulerService eventSchedulerService;

    private final int batchSize = 2;

    @Before
    public void setUp() throws Exception {
        eventSchedulerService = new EventSchedulerService(eventSchedulerDao, eventSchedulerRegistry, 500, batchSize, new ObjectMapper());
        eventSchedulerService.setInitialDelay(0L);
    }

    @Test
    public void testIdle() throws Exception {
        Thread.sleep(200);
        Assert.assertFalse(eventSchedulerService.isRunning());
        verifyZeroInteractions(eventSchedulerRegistry);
    }

    @Test
    public void testTriggerEvent_shouldTriggerEventWhenOldEventFound() throws Exception {
        long now = System.currentTimeMillis()/1000;
        when(eventSchedulerDao.retrieveOldest(0, batchSize)).
                thenReturn(Arrays.asList(new ScheduledEvent("smCorId", "event_name1", now-2, "{\"name\":\"event_name1\",\"type\":\"some_type\",\"data\":\"data\",\"eventSource\":\"internal\"}"),
                        new ScheduledEvent("smCorId", "event_name2", now + 10000, "{\"name\":\"event_name2\",\"type\":\"some_type\",\"data\":\"data\",\"eventSource\":\"internal\"}"))).
                thenReturn(Arrays.asList(new ScheduledEvent("smCorId", "event_name2", now + 10000, "{\"name\":\"event_name2\",\"type\":\"some_type\",\"data\":\"data\",\"eventSource\":\"internal\"}")));

        eventSchedulerService.start();

        Thread.sleep(300);
        verify(eventSchedulerRegistry).triggerEvent("event_name1", "data", "smCorId", "internal");
        verify(eventSchedulerRegistry).deregisterEvent("smCorId", "event_name1");
        Thread.sleep(500);
        verifyNoMoreInteractions(eventSchedulerRegistry);
    }

    @Test
    public void testNoTriggerEvents_shouldNotTriggerEventsWithScheduledTimeOfFuture() throws Exception {
        long now = System.currentTimeMillis()/1000;
        when(eventSchedulerDao.retrieveOldest(0, batchSize)).
                thenReturn(Arrays.asList(new ScheduledEvent("smCorId", "event_name1", now + 9, "{\"name\":\"event_name1\",\"type\":\"some_type\",\"data\":\"data\",\"eventSource\":\"internal\"}"),
                        new ScheduledEvent("smCorId", "event_name2", now + 10, "{\"name\":\"event_name2\",\"type\":\"some_type\",\"data\":\"data\",\"eventSource\":\"internal\"}")));

        eventSchedulerService.start();

        Thread.sleep(1000);
        verifyZeroInteractions(eventSchedulerRegistry);
    }

    @Test
    public void testStartStopCycle() throws Exception {
        long now = System.currentTimeMillis()/1000;
        when(eventSchedulerDao.retrieveOldest(0, batchSize)).
                thenReturn(Arrays.asList(new ScheduledEvent("smCorId", "event_name1", now, "{\"name\":\"event_name1\",\"type\":\"some_type\",\"data\":\"data\",\"eventSource\":\"internal\"}"))).
                thenReturn(Arrays.asList(new ScheduledEvent("smCorId", "event_name2", now+2, "{\"name\":\"event_name2\",\"type\":\"some_type\",\"data\":\"data\",\"eventSource\":\"internal\"}")));

        eventSchedulerService.start();
        Thread.sleep(100);
        Assert.assertTrue(eventSchedulerService.isRunning());
        verify(eventSchedulerRegistry).triggerEvent("event_name1", "data", "smCorId", "internal");
        verify(eventSchedulerRegistry).deregisterEvent("smCorId", "event_name1");

        eventSchedulerService.stop();
        Thread.sleep(2000);
        Assert.assertFalse(eventSchedulerService.isRunning());
        verifyNoMoreInteractions(eventSchedulerRegistry);

        eventSchedulerService.start();
        Thread.sleep(100);

        verify(eventSchedulerRegistry).triggerEvent("event_name2", "data", "smCorId", "internal");
        verify(eventSchedulerRegistry).deregisterEvent("smCorId", "event_name2");
        verifyNoMoreInteractions(eventSchedulerRegistry);
    }
}
