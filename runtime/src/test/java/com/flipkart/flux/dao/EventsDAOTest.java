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

package com.flipkart.flux.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.InjectFromRole;
import com.flipkart.flux.api.EventData;
import com.flipkart.flux.api.VersionedEventData;
import com.flipkart.flux.client.FluxClientComponentModule;
import com.flipkart.flux.client.FluxClientInterceptorModule;
import com.flipkart.flux.dao.iface.EventsDAO;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.Event.EventStatus;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.guice.module.ContainerModule;
import com.flipkart.flux.guice.module.OrchestrationTaskModule;
import com.flipkart.flux.guice.module.ShardModule;
import com.flipkart.flux.integration.StringEvent;
import com.flipkart.flux.module.RuntimeTestModule;
import com.flipkart.flux.rule.DbClearWithTestSMRule;
import com.flipkart.flux.runner.GuiceJunit4Runner;
import com.flipkart.flux.runner.Modules;
import com.flipkart.flux.util.TestUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <code>EventsDAOTest</code> class tests the functionality of {@link EventsDAO} using JUnit tests.
 *
 * @author shyam.akirala
 * @author kartik.bommepally
 */
@RunWith(GuiceJunit4Runner.class)
@Modules(orchestrationModules = {FluxClientComponentModule.class, ShardModule.class, RuntimeTestModule.class, ContainerModule.class,
        OrchestrationTaskModule.class, FluxClientInterceptorModule.class})
public class EventsDAOTest {

    @InjectFromRole
    private
    EventsDAO eventsDAO;

    @InjectFromRole
    @Rule
    public DbClearWithTestSMRule dbClearWithTestSMRule;

    @InjectFromRole
    private
    StateMachinesDAO stateMachinesDAO;

    private ObjectMapper objectMapper;

    @Before
    public void setup() {
        objectMapper = new ObjectMapper();
    }

    @Test
    public void createEventTest() throws JsonProcessingException {
        StateMachine stateMachine = dbClearWithTestSMRule.getStateMachine();
        StringEvent data = new StringEvent("event_dat");
        Event event = new Event("test_event_name", "Internal", Event.EventStatus.pending, stateMachine.getId(),
                objectMapper.writeValueAsString(data), "state1");
        eventsDAO.create(event.getStateMachineInstanceId(), event);

        Event event1 = eventsDAO.findValidEventsByStateMachineIdAndExecutionVersionAndName(event.getStateMachineInstanceId(), event.getName(),
                0L);
        assertThat(event1).isEqualTo(event);
    }

    @Test
    public void testRetrieveByEventNamesAndSmId() throws Exception {
        final StateMachine standardTestMachine = TestUtils.getStandardTestMachine();
        stateMachinesDAO.create(standardTestMachine.getId(), standardTestMachine);
        final Event event1 = new Event("event1", "someType", Event.EventStatus.pending, standardTestMachine.getId(), null, null);
        final VersionedEventData eventData1 = new VersionedEventData(event1.getName(), event1.getType(),
                event1.getEventData(), event1.getEventSource(), event1.getExecutionVersion());
        eventsDAO.create(event1.getStateMachineInstanceId(), event1);
        final Event event3 = new Event("event3", "someType", Event.EventStatus.pending, standardTestMachine.getId(), null, null);
        final VersionedEventData eventData3 = new VersionedEventData(event3.getName(), event3.getType(),
                event3.getEventData(), event3.getEventSource(), event3.getExecutionVersion());
        eventsDAO.create(event3.getStateMachineInstanceId(), event3);

        assertThat(eventsDAO.findByEventNamesAndSMId(standardTestMachine.getId(), new LinkedList<String>() {{
            add("event1");
            add("event3");
        }})).containsExactly(eventData1, eventData3);
    }

    @Test
    public void testRetrieveByEventNamesAndSmId_forEmptyEventNameSet() throws Exception {
        /* Doesn't matter, but still setting it up */
        final StateMachine standardTestMachine = TestUtils.getStandardTestMachine();
        stateMachinesDAO.create(standardTestMachine.getId(), standardTestMachine);
        final Event event1 = new Event("event1", "someType", Event.EventStatus.pending, standardTestMachine.getId(), null, null);
        eventsDAO.create(event1.getStateMachineInstanceId(), event1);
        final Event event3 = new Event("event3", "someType", Event.EventStatus.pending, standardTestMachine.getId(), null, null);
        eventsDAO.create(event3.getStateMachineInstanceId(), event3);

        /* Actual test */
        assertThat(eventsDAO.findByEventNamesAndSMId(standardTestMachine.getId(), Collections.emptyList())).isEmpty();
    }

    @Test
    public void testFindBySMInstanceId() throws Exception{
        final StateMachine standardTestMachine = TestUtils.getStandardTestMachine();
        stateMachinesDAO.create(standardTestMachine.getId(), standardTestMachine);
        final Event event1 = new Event("event1", "someType", EventStatus.invalid, standardTestMachine.getId(), null, null);
        eventsDAO.create(event1.getStateMachineInstanceId(), event1);
        final Event event2 = new Event("event2", "someType", EventStatus.invalid, standardTestMachine.getId(), null, null);
        eventsDAO.create(event1.getStateMachineInstanceId(), event2);
        final Event event3 = new Event("event3", "someType", Event.EventStatus.pending, standardTestMachine.getId(), null, null);
        eventsDAO.create(event3.getStateMachineInstanceId(), event3);

        /* Actual test should contain only the event that is not marked as invalid*/
        assertThat(eventsDAO.findBySMInstanceId(standardTestMachine.getId())).containsExactly(event3);

    }

    @Test
    public void testFindBySMIdAndName() throws Exception{
        final StateMachine standardTestMachine = TestUtils.getStandardTestMachine();
        stateMachinesDAO.create(standardTestMachine.getId(), standardTestMachine);
        final Event event1 = new Event("event1", "someType", EventStatus.pending, standardTestMachine.getId(), null, null);
        eventsDAO.create(event1.getStateMachineInstanceId(), event1);
        final Event event2 = new Event("event2", "someType", EventStatus.invalid, standardTestMachine.getId(), null, null);
        eventsDAO.create(event1.getStateMachineInstanceId(), event2);

        // Should return event1 as it is not marked invalid
        assertThat(eventsDAO.findValidEventBySMIdAndName(standardTestMachine.getId(),"event1")).isEqualTo(event1);

        // should return empty as invalid events are filtered at query level
        assertThat(eventsDAO.findValidEventBySMIdAndName(standardTestMachine.getId(),"event2")).isNull();
    }


    @Test
    public void testFindTriggeredOrCancelledEventsNamesBySMId() throws Exception{
        final StateMachine standardTestMachine = TestUtils.getStandardTestMachine();
        stateMachinesDAO.create(standardTestMachine.getId(), standardTestMachine);
        final Event event1 = new Event("event1", "someType", EventStatus.pending, standardTestMachine.getId(), null, null);
        eventsDAO.create(event1.getStateMachineInstanceId(), event1);
        final Event event2 = new Event("event2", "someType", EventStatus.invalid, standardTestMachine.getId(), null, null);
        eventsDAO.create(event1.getStateMachineInstanceId(), event2);
        final Event event3 = new Event("event3", "someType", EventStatus.triggered, standardTestMachine.getId(), null, null);
        eventsDAO.create(event1.getStateMachineInstanceId(), event3);

        // Should return event3 as it is not marked triggered
        assertThat(eventsDAO.findTriggeredOrCancelledEventsNamesBySMId(standardTestMachine.getId())).containsExactly(event3.getName());

        final Event event4 = new Event("event4", "someType", EventStatus.cancelled, standardTestMachine.getId(), null, null);
        eventsDAO.create(event1.getStateMachineInstanceId(), event4);

        assertThat(eventsDAO.findTriggeredOrCancelledEventsNamesBySMId(standardTestMachine.getId())).containsOnly("event3","event4");
    }


    @Test
    public void testFindReplayEventsNamesBySMId() throws Exception{
        final StateMachine standardTestMachine = TestUtils.getStandardTestMachine();
        stateMachinesDAO.create(standardTestMachine.getId(), standardTestMachine);
        final Event event1 = new Event("event1", "someType", EventStatus.pending, standardTestMachine.getId(), null, "replay");
        eventsDAO.create(event1.getStateMachineInstanceId(), event1);
        final Event event2 = new Event("event2", "someType", EventStatus.invalid, standardTestMachine.getId(), null, null);
        eventsDAO.create(event2.getStateMachineInstanceId(), event2);
        final Event event3 = new Event("event3", "someType", EventStatus.triggered, standardTestMachine.getId(), null, null);
        eventsDAO.create(event3.getStateMachineInstanceId(), event3);
        final Event event4 = new Event("event4", "someType", EventStatus.triggered, standardTestMachine.getId(), null, "replay");
        eventsDAO.create(event4.getStateMachineInstanceId(), event4);
        final Event event5 = new Event("event5", "someType", EventStatus.cancelled, standardTestMachine.getId(), null, "replay");
        eventsDAO.create(event5.getStateMachineInstanceId(), event5);

        assertThat(eventsDAO.findAllValidReplayEventsNamesBySMId(standardTestMachine.getId())).hasSize(3);
        assertThat(eventsDAO.findAllValidReplayEventsNamesBySMId(standardTestMachine.getId())).containsOnly("event1","event4","event5");
    }

    @Test
    public void testFindTriggeredEventsBySMId() throws Exception{
        final StateMachine standardTestMachine = TestUtils.getStandardTestMachine();
        stateMachinesDAO.create(standardTestMachine.getId(), standardTestMachine);
        final Event event1 = new Event("event1", "someType", EventStatus.pending, standardTestMachine.getId(), null, "replay");
        eventsDAO.create(event1.getStateMachineInstanceId(), event1);
        final Event event2 = new Event("event2", "someType", EventStatus.invalid, standardTestMachine.getId(), null, null);
        eventsDAO.create(event2.getStateMachineInstanceId(), event2);
        final Event event3 = new Event("event3", "someType", EventStatus.triggered, standardTestMachine.getId(), null, null);
        eventsDAO.create(event3.getStateMachineInstanceId(), event3);
        final Event event4 = new Event("event4", "someType", EventStatus.triggered, standardTestMachine.getId(), null, "replay");
        eventsDAO.create(event4.getStateMachineInstanceId(), event4);

        assertThat(eventsDAO.findTriggeredEventsBySMId(standardTestMachine.getId())).containsOnly(event3,event4);
    }

    @Test
    public void testFindTriggeredEventBySMIdAndName() throws Exception{

        final StateMachine standardTestMachine = TestUtils.getStandardTestMachine();
        stateMachinesDAO.create(standardTestMachine.getId(), standardTestMachine);
        final Event event1 = new Event("event1", "someType", EventStatus.pending, standardTestMachine.getId(), null, "replay");
        eventsDAO.create(event1.getStateMachineInstanceId(), event1);
        final Event event2 = new Event("event2", "someType", EventStatus.invalid, standardTestMachine.getId(), null, null);
        eventsDAO.create(event2.getStateMachineInstanceId(), event2);
        final Event event3 = new Event("event3", "someType", EventStatus.triggered, standardTestMachine.getId(), null, null);
        eventsDAO.create(event3.getStateMachineInstanceId(), event3);
        final Event event4 = new Event("event4", "someType", EventStatus.triggered, standardTestMachine.getId(), null, "replay");
        eventsDAO.create(event4.getStateMachineInstanceId(), event4);

        Event retrievedEvent3 = eventsDAO.findTriggeredEventBySMIdAndName(standardTestMachine.getId(),"event3");
        assertThat(retrievedEvent3).isEqualTo(event3);

        Event retrievedEvent4 = eventsDAO.findTriggeredEventBySMIdAndName(standardTestMachine.getId(),"event4");
        assertThat(retrievedEvent4).isEqualTo(event4);

    }

    @Test
    public void testGetAllEventsNameAndStatus() throws Exception{

        final StateMachine standardTestMachine = TestUtils.getStandardTestMachine();
        stateMachinesDAO.create(standardTestMachine.getId(), standardTestMachine);
        final Event event1 = new Event("event1", "someType", EventStatus.pending, standardTestMachine.getId(), null, "replay");
        eventsDAO.create(event1.getStateMachineInstanceId(), event1);
        final Event event2 = new Event("event2", "someType", EventStatus.invalid, standardTestMachine.getId(), null, null);
        eventsDAO.create(event1.getStateMachineInstanceId(), event2);
        final Event event3 = new Event("event3", "someType", EventStatus.triggered, standardTestMachine.getId(), null, null);
        eventsDAO.create(event1.getStateMachineInstanceId(), event3);
        final Event event4 = new Event("event4", "someType", EventStatus.triggered, standardTestMachine.getId(), null, "replay");
        eventsDAO.create(event1.getStateMachineInstanceId(), event4);
        final Event event5 = new Event("event5", "someType", EventStatus.cancelled, standardTestMachine.getId(), null, "replay");
        eventsDAO.create(event1.getStateMachineInstanceId(), event5);

        assertThat(eventsDAO.getAllEventsNameAndStatus(standardTestMachine.getId(),false)).hasSize(4);

        assertThat(eventsDAO.getAllEventsNameAndStatus(standardTestMachine.getId(),true)).hasSize(4);
    }

    @Test
    public void testMarkEventAsCancelled() throws Exception{
        final StateMachine standardTestMachine = TestUtils.getStandardTestMachine();
        stateMachinesDAO.create(standardTestMachine.getId(),standardTestMachine);
        final Event event = new Event("event1","someType",EventStatus.pending,standardTestMachine.getId(),null,null);
        eventsDAO.create(standardTestMachine.getId(),event);
        eventsDAO.markEventAsCancelled(standardTestMachine.getId(),"event1");

        assertThat(eventsDAO.findValidEventBySMIdAndName(standardTestMachine.getId(),"event1").getStatus()).isEqualTo(EventStatus.cancelled);
    }

    @Test
    public void testMarkEventAsInvalid() throws Exception{
        final StateMachine standardTestMachine = TestUtils.getStandardTestMachine();
        stateMachinesDAO.create(standardTestMachine.getId(),standardTestMachine);
        final Event event1 = new Event("event1","someType",EventStatus.pending,standardTestMachine.getId(),null,null);
        eventsDAO.create(standardTestMachine.getId(),event1);
        final Event event2 = new Event("event2", "someType", EventStatus.invalid, standardTestMachine.getId(), null, null);
        eventsDAO.create(event2.getStateMachineInstanceId(), event2);
        final Event event3 = new Event("event3", "someType", EventStatus.triggered, standardTestMachine.getId(), null, null);
        eventsDAO.create(event3.getStateMachineInstanceId(), event3);
        final Event event4 = new Event("event4", "someType", EventStatus.triggered, standardTestMachine.getId(), null, "replay");
        eventsDAO.create(event4.getStateMachineInstanceId(), event4);
        final Event event5 = new Event("event5", "someType", EventStatus.cancelled, standardTestMachine.getId(), null, "replay");
        eventsDAO.create(event5.getStateMachineInstanceId(), event5);
        final Event event6 = new Event("event6", "someType", EventStatus.triggered, standardTestMachine.getId(), null, null);
        eventsDAO.create(event6.getStateMachineInstanceId(), event6);

        List<String> invalidEventsList = new ArrayList<>();
        invalidEventsList.add(event2.getName());
        invalidEventsList.add(event3.getName());
        invalidEventsList.add(event4.getName());

        eventsDAO.markEventsAsInvalid(standardTestMachine.getId(), invalidEventsList);

        assertThat(eventsDAO.findAllBySMIdAndName(standardTestMachine.getId(),"event1").get(0).getStatus()).isEqualTo(EventStatus.pending);
        assertThat(eventsDAO.findAllBySMIdAndName(standardTestMachine.getId(),"event2").get(0).getStatus()).isEqualTo(EventStatus.invalid);
        assertThat(eventsDAO.findAllBySMIdAndName(standardTestMachine.getId(),"event3").get(0).getStatus()).isEqualTo(EventStatus.invalid);
        assertThat(eventsDAO.findAllBySMIdAndName(standardTestMachine.getId(),"event4").get(0).getStatus()).isEqualTo(EventStatus.invalid);
        assertThat(eventsDAO.findAllBySMIdAndName(standardTestMachine.getId(),"event5").get(0).getStatus()).isEqualTo(EventStatus.cancelled);
        assertThat(eventsDAO.findAllBySMIdAndName(standardTestMachine.getId(),"event6").get(0).getStatus()).isEqualTo(EventStatus.triggered);
    }

    @Test
    public void testFindAllValidEventsByStateMachineIdAndExecutionVersionAndName() throws Exception{
        final StateMachine standardTestMachine = TestUtils.getStandardTestMachine();
        stateMachinesDAO.create(standardTestMachine.getId(),standardTestMachine);

        // Store all the different events in DB
        final Event event1 = new Event("event1","someType",EventStatus.pending,standardTestMachine.getId(),null,null,0l);
        eventsDAO.create(standardTestMachine.getId(),event1);
        final Event event2 = new Event("event2", "someType", EventStatus.invalid, standardTestMachine.getId(), null, null,0l);
        eventsDAO.create(event2.getStateMachineInstanceId(), event2);
        final Event event3 = new Event("event3", "someType", EventStatus.triggered, standardTestMachine.getId(), null, null,0l);
        eventsDAO.create(event3.getStateMachineInstanceId(), event3);
        final Event event4 = new Event("event4", "someType", EventStatus.triggered, standardTestMachine.getId(), null, "replay",1l);
        eventsDAO.create(event4.getStateMachineInstanceId(), event4);
        final Event event5 = new Event("event5", "someType", EventStatus.cancelled, standardTestMachine.getId(), null, "replay",0l);
        eventsDAO.create(event5.getStateMachineInstanceId(), event5);
        final Event event6 = new Event("event6", "someType", EventStatus.triggered, standardTestMachine.getId(), null, null,1l);
        eventsDAO.create(event6.getStateMachineInstanceId(), event6);

        // Query for the events listed
        List<String> inputList = new ArrayList<>();
        inputList.add(event1.getName());
        inputList.add(event2.getName());
        inputList.add(event3.getName());
        inputList.add(event4.getName());
        inputList.add(event5.getName());

        List<Event> outputEventList = eventsDAO.findAllValidEventsByStateMachineIdAndExecutionVersionAndName(standardTestMachine.getId(),inputList,0l);

        assertThat(outputEventList).contains(event1,event3,event5);
        assertThat(outputEventList).doesNotContain(event2,event4,event6);

    }
}