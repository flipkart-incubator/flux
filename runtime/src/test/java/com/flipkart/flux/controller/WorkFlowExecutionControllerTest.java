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

package com.flipkart.flux.controller;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import com.flipkart.flux.api.EventData;
import com.flipkart.flux.dao.iface.EventsDAO;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.impl.message.TaskAndEvents;
import com.flipkart.flux.impl.task.registry.RouterRegistry;
import com.flipkart.flux.MockActorRef;
import com.flipkart.flux.util.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WorkFlowExecutionControllerTest {

    @Mock
    StateMachinesDAO stateMachinesDAO;

    @Mock
    EventsDAO eventsDAO;

    @Mock
    private RouterRegistry routerRegistry;

    TestActorRef<MockActorRef> mockActor;

    private WorkFlowExecutionController workFlowExecutionController;
    private ActorSystem actorSystem;

    @Before
    public void setUp() throws Exception {
        workFlowExecutionController = new WorkFlowExecutionController(eventsDAO, stateMachinesDAO, routerRegistry);
        when(stateMachinesDAO.findById(anyLong())).thenReturn(TestUtils.getStandardTestMachine());
        actorSystem = ActorSystem.create();
        mockActor = TestActorRef.create(actorSystem, Props.create(MockActorRef.class));
        when(routerRegistry.getRouter(anyString())).thenReturn(mockActor);
    }

    @After
    public void tearDown() throws Exception {
        mockActor.stop();
        actorSystem.shutdown();
    }

    @Test
    public void testEventPost_shouldLookupRouterAndSendMessage() throws Exception {
        final EventData testEventData = new EventData("event1", "foo", "someStringData", "runtime");
        when(eventsDAO.findBySMIdAndName(1l, "event1")).thenReturn(new Event("event1", "foo", Event.EventStatus.pending, 1l, null, null));
        Event[] expectedEvents = new Event[]{new Event("event1","someType", Event.EventStatus.triggered,1l,"someStringData","runtime")};
        when(eventsDAO.findByEventNamesAndSMId(Collections.singleton("event1"),1l)).thenReturn(Arrays.asList(expectedEvents));
        workFlowExecutionController.postEvent(testEventData, 1l);

        verify(routerRegistry, times(2)).getRouter("someRouter"); // For 2 unblocked states
        mockActor.underlyingActor().assertMessageReceived(new TaskAndEvents("com.flipkart.flux.dao.TestTask", expectedEvents, 1l), 2);
        verifyNoMoreInteractions(routerRegistry);
    }
}