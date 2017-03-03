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
import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.domain.Status;
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
        final EventData testEventData = new EventData("event0", "java.lang.String", "42", "runtime");
        when(eventsDAO.findBySMIdAndName(1l, "event0")).thenReturn(new Event("event0", "java.lang.String", Event.EventStatus.pending, 1l, null, null));
        EventData[] expectedEvents = new EventData[]{new EventData("event0","java.lang.String","42","runtime")};
        when(eventsDAO.findTriggeredEventsNamesBySMId(1l)).thenReturn(Collections.singletonList("event0"));
        workFlowExecutionController.postEvent(testEventData, 1l, null);

        verify(routerRegistry, times(1)).getRouter("com.flipkart.flux.dao.TestWorkflow_dummyTask"); // For 1 unblocked states
        mockActor.underlyingActor().assertMessageReceived(new TaskAndEvents("dummyTask", "com.flipkart.flux.dao.TestWorkflow_dummyTask_java.lang.Integer_java.lang.String_version1", 4L, expectedEvents, 1l, "test_state_machine", TestUtils.toStr(TestUtils.getOutputEvent("event3", Integer.class)),2), 1);
        verifyNoMoreInteractions(routerRegistry);
    }

    @Test
    public void testEventPost_shouldNotFetchEventDataFromDBIfStateIsDependantOnSingleEvent() throws Exception {
        final EventData testEventData = new EventData("event1", "foo", "someStringData", "runtime");
        when(eventsDAO.findBySMIdAndName(1l, "event1")).thenReturn(new Event("event1", "foo", Event.EventStatus.pending, 1l, null, null));
        when(eventsDAO.findTriggeredEventsNamesBySMId(1l)).thenReturn(Collections.singletonList("event1"));
        workFlowExecutionController.postEvent(testEventData, 1l, null);

        // As states 2 and 3 dependant on single event there should be no more interactions with eventDAO to fetch event data
        verify(eventsDAO, times(0)).findByEventNamesAndSMId(Collections.singletonList("event1"),1l);
    }

    @Test
    public void testEventPost_taskRedriveDelay() throws Exception {
        final EventData testEventData1 = new EventData("event1", "java.lang.String", "42", "runtime");
        when(eventsDAO.findBySMIdAndName(1l, "event1")).thenReturn(new Event("event1", "java.lang.String", Event.EventStatus.pending, 1l, null, null));
        EventData[] expectedEvents1 = new EventData[]{new EventData("event1","java.lang.String","42","runtime")};
        when(eventsDAO.findByEventNamesAndSMId(Collections.singletonList("event1"),1l)).thenReturn(Arrays.asList(expectedEvents1));
        when(eventsDAO.findTriggeredEventsNamesBySMId(1l)).thenReturn(Collections.singletonList("event1"));
        workFlowExecutionController.postEvent(testEventData1, 1l, null);

        final EventData testEventData0 = new EventData("event0", "java.lang.String", "42", "runtime");
        when(eventsDAO.findBySMIdAndName(1l, "event0")).thenReturn(new Event("event0", "java.lang.String", Event.EventStatus.pending, 1l, null, null));
        EventData[] expectedEvents0 = new EventData[]{new EventData("event0","java.lang.String","42","runtime")};
        when(eventsDAO.findByEventNamesAndSMId(Collections.singletonList("event0"),1l)).thenReturn(Arrays.asList(expectedEvents0));
        when(eventsDAO.findTriggeredEventsNamesBySMId(1l)).thenReturn(Collections.singletonList("event0"));
        workFlowExecutionController.postEvent(testEventData0, 1l, null);

        // give time to execute
        Thread.sleep(2000);

        verify(redriverRegistry).registerTask(2L, 32800); //state with id 2 has 3 retries and 100ms timeout
        verify(redriverRegistry).registerTask(4L, 8400); //state with id 4 has 1 retries and 100ms timeout
    }

    @Test
    public void testEventPost_shouldNotExecuteTaskIfItIsAlreadyCompleted() throws Exception {
        final EventData testEventData = new EventData("event0", "java.lang.String", "42", "runtime");
        when(eventsDAO.findBySMIdAndName(1l, "event0")).thenReturn(new Event("event0", "java.lang.String", Event.EventStatus.pending, 1l, null, null));
        EventData[] expectedEvents = new EventData[]{new EventData("event0","java.lang.String","42","runtime")};
        when(eventsDAO.findTriggeredEventsNamesBySMId(1l)).thenReturn(Collections.singletonList("event0"));
        workFlowExecutionController.postEvent(testEventData, 1l, null);
        State state = stateMachinesDAO.findById(1L).getStates().stream().filter((s)->s.getId() == 4L).findFirst().orElse(null);
        state.setStatus(Status.completed);

        //post the event again, this should not send msg to router for execution
        workFlowExecutionController.postEvent(testEventData, 1l, null);

        verify(routerRegistry, times(1)).getRouter("com.flipkart.flux.dao.TestWorkflow_dummyTask"); // the router should receive only one execution request
        mockActor.underlyingActor().assertMessageReceived(new TaskAndEvents("dummyTask", "com.flipkart.flux.dao.TestWorkflow_dummyTask_java.lang.Integer_java.lang.String_version1", 4L, expectedEvents, 1l, "test_state_machine", TestUtils.toStr(TestUtils.getOutputEvent("event3", Integer.class)),2), 1);
        verifyNoMoreInteractions(routerRegistry);
    }

    @Test
    public void testEventPost_shouldExecuteTaskIfItIsNotCompleted() throws Exception {
        final EventData testEventData = new EventData("event0", "java.lang.String", "42", "runtime");
        when(eventsDAO.findBySMIdAndName(1l, "event0")).thenReturn(new Event("event0", "java.lang.String", Event.EventStatus.pending, 1l, null, null));
        EventData[] expectedEvents = new EventData[]{new EventData("event0","java.lang.String","42","runtime")};
        when(eventsDAO.findTriggeredEventsNamesBySMId(1l)).thenReturn(Collections.singletonList("event0"));
        workFlowExecutionController.postEvent(testEventData, 1l, null);
        StateMachine stateMachine = stateMachinesDAO.findById(1L);
        State state = stateMachinesDAO.findById(1L).getStates().stream().filter((s)->s.getId() == 4L).findFirst().orElse(null);
        state.setStatus(Status.errored);

        //post the event again, this should send msg to router again for execution
        workFlowExecutionController.postEvent(testEventData, 1l, null);

        verify(routerRegistry, times(2)).getRouter("com.flipkart.flux.dao.TestWorkflow_dummyTask"); // the router should receive two execution requests
        mockActor.underlyingActor().assertMessageReceived(new TaskAndEvents("dummyTask", "com.flipkart.flux.dao.TestWorkflow_dummyTask_java.lang.Integer_java.lang.String_version1", 4L, expectedEvents, 1l, "test_state_machine", TestUtils.toStr(TestUtils.getOutputEvent("event3", Integer.class)),2), 2);
        verifyNoMoreInteractions(routerRegistry);
    }
}