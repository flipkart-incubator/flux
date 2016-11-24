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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.MockActorRef;
import com.flipkart.flux.api.EventData;
import com.flipkart.flux.dao.iface.AuditDAO;
import com.flipkart.flux.dao.iface.EventsDAO;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.dao.iface.StatesDAO;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.impl.message.TaskAndEvents;
import com.flipkart.flux.impl.task.registry.RouterRegistry;
import com.flipkart.flux.task.redriver.RedriverRegistry;
import com.flipkart.flux.util.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WorkFlowExecutionControllerTest {

    @Mock
    StateMachinesDAO stateMachinesDAO;

    @Mock
    EventsDAO eventsDAO;
    
    @Mock
    StatesDAO statesDAO;

    @Mock
    AuditDAO auditDAO;

    @Mock
    private RouterRegistry routerRegistry;

    @Mock
    private RedriverRegistry redriverRegistry;
    
    TestActorRef<MockActorRef> mockActor;

    private WorkFlowExecutionController workFlowExecutionController;
    private ActorSystem actorSystem;
    private ObjectMapper objectMapper;


    @Before
    public void setUp() throws Exception {
        Thread.sleep(1000);
        workFlowExecutionController = new WorkFlowExecutionController(eventsDAO, stateMachinesDAO, statesDAO, auditDAO, routerRegistry, redriverRegistry);
        when(stateMachinesDAO.findById(anyLong())).thenReturn(TestUtils.getStandardTestMachineWithId());
        actorSystem = ActorSystem.create();
        mockActor = TestActorRef.create(actorSystem, Props.create(MockActorRef.class));
        when(routerRegistry.getRouter(anyString())).thenReturn(mockActor);
        objectMapper = new ObjectMapper();
    }

    @After
    public void tearDown() throws Exception {
        mockActor.stop();
        actorSystem.terminate();
    }

    @Test
    public void testEventPost_shouldLookupRouterAndSendMessage() throws Exception {
        final EventData testEventData = new EventData("event1", "foo", "someStringData", "runtime");
        when(eventsDAO.findBySMIdAndName(1l, "event1")).thenReturn(new Event("event1", "foo", Event.EventStatus.pending, 1l, null, null));
        EventData[] expectedEvents = new EventData[]{new EventData("event1","someType","someStringData","runtime")};
        when(eventsDAO.findByEventNamesAndSMId(Collections.singletonList("event1"),1l)).thenReturn(Arrays.asList(expectedEvents));
        when(eventsDAO.findTriggeredEventsNamesBySMId(1l)).thenReturn(Collections.singletonList("event1"));
        workFlowExecutionController.postEvent(testEventData, 1l, null);

        verify(routerRegistry, times(2)).getRouter("com.flipkart.flux.dao.TestWorkflow_testTask"); // For 2 unblocked states
        mockActor.underlyingActor().assertMessageReceived(new TaskAndEvents("testTask", "com.flipkart.flux.dao.TestWorkflow_testTask_event1", 2L, expectedEvents, 1l, objectMapper.writeValueAsString(TestUtils.standardStateMachineOutputEvent()),2), 1);
        mockActor.underlyingActor().assertMessageReceived(new TaskAndEvents("testTask", "com.flipkart.flux.dao.TestWorkflow_testTask_event1", 3L, expectedEvents, 1l, null,2), 1);
        verifyNoMoreInteractions(routerRegistry);
    }

    @Test
    public void testEventPost_taskRedriveDelay() throws Exception {
        final EventData testEventData = new EventData("event1", "foo", "someStringData", "runtime");
        when(eventsDAO.findBySMIdAndName(1l, "event1")).thenReturn(new Event("event1", "foo", Event.EventStatus.pending, 1l, null, null));
        EventData[] expectedEvents = new EventData[]{new EventData("event1","someType","someStringData","runtime")};
        when(eventsDAO.findByEventNamesAndSMId(Collections.singletonList("event1"),1l)).thenReturn(Arrays.asList(expectedEvents));
        when(eventsDAO.findTriggeredEventsNamesBySMId(1l)).thenReturn(Collections.singletonList("event1"));
        workFlowExecutionController.postEvent(testEventData, 1l, null);

        verify(redriverRegistry).registerTask(3L, 32800); //state with id 3 has 3 retries and 100ms timeout
        verify(redriverRegistry).registerTask(2L, 16600); //state with id 2 has 2 retries and 100ms timeout
    }
}