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
import com.flipkart.flux.api.EventDefinition;
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
import com.flipkart.flux.metrics.iface.MetricsClient;
import com.flipkart.flux.task.redriver.RedriverRegistry;
import com.flipkart.flux.util.TestUtils;
import com.typesafe.config.ConfigFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Mock
    private MetricsClient metricsClient;
    TestActorRef<MockActorRef> mockActor;

    private WorkFlowExecutionController workFlowExecutionController;
    private ActorSystem actorSystem;
    private ObjectMapper objectMapper;


    @Before
    public void setUp() throws Exception {
        Thread.sleep(1000);
        workFlowExecutionController = new WorkFlowExecutionController(eventsDAO, stateMachinesDAO, statesDAO, auditDAO, routerRegistry, redriverRegistry, metricsClient);
        when(stateMachinesDAO.findById(anyLong())).thenReturn(TestUtils.getStandardTestMachineWithId());
        actorSystem = ActorSystem.create("default", ConfigFactory.load("application2.conf"));
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
        when(eventsDAO.findTriggeredOrCancelledEventsNamesBySMId(1l)).thenReturn(Collections.singletonList("event0"));
        workFlowExecutionController.postEvent(testEventData, TestUtils.getStandardTestMachineWithId());

        verify(routerRegistry, times(1)).getRouter("com.flipkart.flux.dao.TestWorkflow_dummyTask"); // For 1 unblocked states
        mockActor.underlyingActor().assertMessageReceived(new TaskAndEvents("dummyTask", "com.flipkart.flux.dao.TestWorkflow_dummyTask_java.lang.Integer_java.lang.String_version1", 4L, expectedEvents, 1l, "test_state_machine", TestUtils.toStr(TestUtils.getOutputEvent("event3", Integer.class)),2), 1);
        verifyNoMoreInteractions(routerRegistry);
    }

    @Test
    public void testEventPost_shouldNotFetchEventDataFromDBIfStateIsDependantOnSingleEvent() throws Exception {
        final EventData testEventData = new EventData("event1", "foo", "someStringData", "runtime");
        when(eventsDAO.findBySMIdAndName(1l, "event1")).thenReturn(new Event("event1", "foo", Event.EventStatus.pending, 1l, null, null));
        when(eventsDAO.findTriggeredOrCancelledEventsNamesBySMId(1l)).thenReturn(Collections.singletonList("event1"));
        workFlowExecutionController.postEvent(testEventData, TestUtils.getStandardTestMachineWithId());

        // As states 2 and 3 dependant on single event there should be no more interactions with eventDAO to fetch event data
        verify(eventsDAO, times(0)).findByEventNamesAndSMId(Collections.singletonList("event1"),1l);
    }

    @Test
    public void testEventPost_taskRedriveDelay() throws Exception {
        final EventData testEventData1 = new EventData("event1", "java.lang.String", "42", "runtime");
        when(eventsDAO.findBySMIdAndName(1l, "event1")).thenReturn(new Event("event1", "java.lang.String", Event.EventStatus.pending, 1l, null, null));
        EventData[] expectedEvents1 = new EventData[]{new EventData("event1","java.lang.String","42","runtime")};
        when(eventsDAO.findByEventNamesAndSMId(Collections.singletonList("event1"),1l)).thenReturn(Arrays.asList(expectedEvents1));
        when(eventsDAO.findTriggeredOrCancelledEventsNamesBySMId(1l)).thenReturn(Collections.singletonList("event1"));
        workFlowExecutionController.postEvent(testEventData1, TestUtils.getStandardTestMachineWithId());

        final EventData testEventData0 = new EventData("event0", "java.lang.String", "42", "runtime");
        when(eventsDAO.findBySMIdAndName(1l, "event0")).thenReturn(new Event("event0", "java.lang.String", Event.EventStatus.pending, 1l, null, null));
        EventData[] expectedEvents0 = new EventData[]{new EventData("event0","java.lang.String","42","runtime")};
        when(eventsDAO.findByEventNamesAndSMId(Collections.singletonList("event0"),1l)).thenReturn(Arrays.asList(expectedEvents0));
        when(eventsDAO.findTriggeredOrCancelledEventsNamesBySMId(1l)).thenReturn(Collections.singletonList("event0"));
        workFlowExecutionController.postEvent(testEventData0, TestUtils.getStandardTestMachineWithId());

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
        when(eventsDAO.findTriggeredOrCancelledEventsNamesBySMId(1l)).thenReturn(Collections.singletonList("event0"));
        workFlowExecutionController.postEvent(testEventData, TestUtils.getStandardTestMachineWithId());
        StateMachine stateMachine = stateMachinesDAO.findById(1L);
        State state = stateMachine.getStates().stream().filter((s)->s.getId() == 4L).findFirst().orElse(null);
        state.setStatus(Status.completed);

        //post the event again, this should not send msg to router for execution
        workFlowExecutionController.postEvent(testEventData, stateMachine);

        verify(routerRegistry, times(1)).getRouter("com.flipkart.flux.dao.TestWorkflow_dummyTask"); // the router should receive only one execution request
        mockActor.underlyingActor().assertMessageReceived(new TaskAndEvents("dummyTask", "com.flipkart.flux.dao.TestWorkflow_dummyTask_java.lang.Integer_java.lang.String_version1", 4L, expectedEvents, 1l, "test_state_machine", TestUtils.toStr(TestUtils.getOutputEvent("event3", Integer.class)),2), 1);
        verifyNoMoreInteractions(routerRegistry);
    }

    @Test
    public void testEventPost_shouldExecuteTaskIfItIsNotCompleted() throws Exception {
        final EventData testEventData = new EventData("event0", "java.lang.String", "42", "runtime");
        when(eventsDAO.findBySMIdAndName(1l, "event0")).thenReturn(new Event("event0", "java.lang.String", Event.EventStatus.pending, 1l, null, null));
        EventData[] expectedEvents = new EventData[]{new EventData("event0","java.lang.String","42","runtime")};
        when(eventsDAO.findTriggeredOrCancelledEventsNamesBySMId(1l)).thenReturn(Collections.singletonList("event0"));
        workFlowExecutionController.postEvent(testEventData, TestUtils.getStandardTestMachineWithId());
        StateMachine stateMachine = stateMachinesDAO.findById(1L);
        State state = stateMachine.getStates().stream().filter((s)->s.getId() == 4L).findFirst().orElse(null);
        state.setStatus(Status.errored);

        //post the event again, this should send msg to router again for execution
        workFlowExecutionController.postEvent(testEventData, stateMachine);

        verify(routerRegistry, times(2)).getRouter("com.flipkart.flux.dao.TestWorkflow_dummyTask"); // the router should receive two execution requests
        mockActor.underlyingActor().assertMessageReceived(new TaskAndEvents("dummyTask", "com.flipkart.flux.dao.TestWorkflow_dummyTask_java.lang.Integer_java.lang.String_version1", 4L, expectedEvents, 1l, "test_state_machine", TestUtils.toStr(TestUtils.getOutputEvent("event3", Integer.class)),2), 2);
        verifyNoMoreInteractions(routerRegistry);
    }

    @Test
    public void testEventPost_shouldNotExecuteTaskIfItIsCancelled() throws Exception {
        final EventData testEventData = new EventData("event0", "java.lang.String", "42", "runtime");
        when(eventsDAO.findBySMIdAndName(1l, "event0")).thenReturn(new Event("event0", "java.lang.String", Event.EventStatus.pending, 1l, null, null));
        EventData[] expectedEvents = new EventData[]{new EventData("event0","java.lang.String","42","runtime")};
        when(eventsDAO.findTriggeredOrCancelledEventsNamesBySMId(1l)).thenReturn(Collections.singletonList("event0"));
        workFlowExecutionController.postEvent(testEventData, TestUtils.getStandardTestMachineWithId());
        StateMachine stateMachine = stateMachinesDAO.findById(1L);
        State state = stateMachine.getStates().stream().filter((s)->s.getId() == 4L).findFirst().orElse(null);
        state.setStatus(Status.cancelled);

        //post the event again, this should not send msg to router for execution
        workFlowExecutionController.postEvent(testEventData, stateMachine);

        verify(routerRegistry, times(1)).getRouter("com.flipkart.flux.dao.TestWorkflow_dummyTask"); // the router should receive only one execution request
        mockActor.underlyingActor().assertMessageReceived(new TaskAndEvents("dummyTask", "com.flipkart.flux.dao.TestWorkflow_dummyTask_java.lang.Integer_java.lang.String_version1", 4L, expectedEvents, 1l, "test_state_machine", TestUtils.toStr(TestUtils.getOutputEvent("event3", Integer.class)),2), 1);
        verifyNoMoreInteractions(routerRegistry);
    }

    @Test
    public void testCancelPath_shouldCancelPathTillJoinNode() throws Exception {

        //setup the below state machine, and do cancel with event3, that should cancel till state3
        //
        //   state1 --------(event1)---------> state2 --------(event2)--------------------> state3
        //    |                                                                               ^
        //    |                                                                               |
        //    |                           ---(event3)--> state5 --(event4)---              (event6)
        //    |                          |                                   |                |
        //    |----(event1)---> state4 --                                    |---> state7 ----
        //                               |---(event3)--> state6 --(event5)---|
        //
        HashMap<String, Event.EventStatus> eventStatusHashMap = new HashMap<String, Event.EventStatus>() {{
            put("event1", Event.EventStatus.triggered);
            put("event2", Event.EventStatus.triggered);
            put("event3", Event.EventStatus.pending);
            put("event4", Event.EventStatus.pending);
            put("event5", Event.EventStatus.pending);
        }};

        String outputEvent1 = null;
        String outputEvent2 = null;
        String outputEvent3 = null;
        String outputEvent4 = null;
        String outputEvent5 = null;
        String outputEvent6 = null;
        try {
            outputEvent1 = objectMapper.writeValueAsString(new EventDefinition("event1", "SomeEvent.class"));
            outputEvent2 = objectMapper.writeValueAsString(new EventDefinition("event2", "SomeEvent.class"));
            outputEvent3 = objectMapper.writeValueAsString(new EventDefinition("event3", "SomeEvent.class"));
            outputEvent4 = objectMapper.writeValueAsString(new EventDefinition("event4", "SomeEvent.class"));
            outputEvent5 = objectMapper.writeValueAsString(new EventDefinition("event5", "SomeEvent.class"));
            outputEvent6 = objectMapper.writeValueAsString(new EventDefinition("event6", "SomeEvent.class"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        State state1 = new State(1L, "state1", null, null, null, null, new ArrayList<>(), 0L, 1000L, outputEvent1, Status.initialized, null, 0L);
        TestUtils.setProperty(state1, "id", 1L); state1.setStateMachineId(1L);
        State state2 = new State(1L, "state2", null, null, null, null, new ArrayList<String>(){{add("event1");}}, 0L, 1000L, outputEvent2, Status.initialized, null, 0L);
        TestUtils.setProperty(state2, "id", 2L); state2.setStateMachineId(1L);
        State state3 = new State(1L, "state3", null, null, null, null, new ArrayList<String>(){{add("event2");add("event6");}}, 0L, 1000L, null, Status.initialized, null, 0L);
        TestUtils.setProperty(state3, "id", 3L); state3.setStateMachineId(1L);
        State state4 = new State(1L, "state4", null, null, null, null, new ArrayList<String>(){{add("event1");}}, 0L, 1000L, outputEvent3, Status.initialized, null, 0L);
        TestUtils.setProperty(state4, "id", 4L); state4.setStateMachineId(1L);
        State state5 = new State(1L, "state5", null, null, null, null, new ArrayList<String>(){{add("event3");}}, 0L, 1000L, outputEvent4, Status.initialized, null, 0L);
        TestUtils.setProperty(state5, "id", 5L); state5.setStateMachineId(1L);
        State state6 = new State(1L, "state6", null, null, null, null, new ArrayList<String>(){{add("event3");}}, 0L, 1000L, outputEvent5, Status.initialized, null, 0L);
        TestUtils.setProperty(state6, "id", 6L); state6.setStateMachineId(1L);
        State state7 = new State(1L, "state7", null, null, null, null, new ArrayList<String>(){{add("event4");add("event5");}}, 0L, 1000L, outputEvent6, Status.initialized, null, 0L);
        TestUtils.setProperty(state7, "id", 7L); state7.setStateMachineId(1L);

        Set<State> states = new HashSet<State>(){{add(state1);add(state2);add(state3);add(state4);add(state5);add(state6);add(state7);}};
        StateMachine stateMachine = new StateMachine(1L, "state_machine_1", null, states, "magic_number_1");
        TestUtils.setProperty(stateMachine, "id", 1L);
        EventData testEventData = new EventData("event3", null, null, "runtime", true);
        when(eventsDAO.getAllEventsNameAndStatus(1L, true)).thenReturn(eventStatusHashMap);

        // invoke cancel
        Set<State> executableStates = workFlowExecutionController.cancelPath(stateMachine, testEventData);

        assertThat(executableStates.size()).isEqualTo(1);
        assertThat(executableStates.contains("state3"));
        verify(eventsDAO).markEventAsCancelled(1L, "event3");
        verify(eventsDAO).markEventAsCancelled(1L, "event4");
        verify(eventsDAO).markEventAsCancelled(1L, "event5");
        verify(eventsDAO).markEventAsCancelled(1L, "event6");

        verify(statesDAO).updateStatus(5L, 1L, Status.cancelled);
        verify(statesDAO).updateStatus(6L, 1L, Status.cancelled);
        verify(statesDAO).updateStatus(7L, 1L, Status.cancelled);


    }
}