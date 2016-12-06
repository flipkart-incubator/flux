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
import com.flipkart.flux.api.EventData;
import com.flipkart.flux.client.FluxClientInterceptorModule;
import com.flipkart.flux.dao.iface.EventsDAO;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.guice.module.AkkaModule;
import com.flipkart.flux.guice.module.ContainerModule;
import com.flipkart.flux.guice.module.HibernateModule;
import com.flipkart.flux.impl.boot.TaskModule;
import com.flipkart.flux.integration.StringEvent;
import com.flipkart.flux.module.DeploymentUnitTestModule;
import com.flipkart.flux.module.RuntimeTestModule;
import com.flipkart.flux.rule.DbClearWithTestSMRule;
import com.flipkart.flux.runner.GuiceJunit4Runner;
import com.flipkart.flux.runner.Modules;
import com.flipkart.flux.util.TestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Collections;
import java.util.LinkedList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <code>EventsDAOTest</code> class tests the functionality of {@link EventsDAO} using JUnit tests.
 * @author shyam.akirala
 * @author kartik.bommepally
 */
@RunWith(GuiceJunit4Runner.class)
@Modules({DeploymentUnitTestModule.class,HibernateModule.class,RuntimeTestModule.class,ContainerModule.class,AkkaModule.class,TaskModule.class,FluxClientInterceptorModule.class})
public class EventsDAOTest {

    @Inject
    EventsDAO eventsDAO;

    @Inject
    @Rule
    public DbClearWithTestSMRule dbClearWithTestSMRule;

    @Inject
    StateMachinesDAO stateMachinesDAO;

    ObjectMapper objectMapper;

    @Before
    public void setup() {
        objectMapper = new ObjectMapper();
    }

    @Test
    public void createEventTest() throws JsonProcessingException {
        StateMachine stateMachine = dbClearWithTestSMRule.getStateMachine();
        StringEvent data = new StringEvent("event_dat");
        Event event = new Event("test_event_name","Internal", Event.EventStatus.pending,stateMachine.getId(), objectMapper.writeValueAsString(data),"state1");
        Long eventId = eventsDAO.create(event).getId();

        Event event1 = eventsDAO.findById(eventId);
        assertThat(event1).isEqualTo(event);
    }

    @Test
    public void testRetrieveByEventNamesAndSmId() throws Exception {
        final StateMachine standardTestMachine = TestUtils.getStandardTestMachine();
        stateMachinesDAO.create(standardTestMachine);
        final Event event1 = new Event("event1", "someType", Event.EventStatus.pending, standardTestMachine.getId(), null, null);
        final EventData eventData1 = new EventData(event1.getName(),event1.getType(), event1.getEventData(), event1.getEventSource());
        eventsDAO.create(event1);
        final Event event3 = new Event("event3", "someType", Event.EventStatus.pending, standardTestMachine.getId(), null, null);
        final EventData eventData3 = new EventData(event3.getName(),event3.getType(), event3.getEventData(), event3.getEventSource());
        eventsDAO.create(event3);

        assertThat(eventsDAO.findByEventNamesAndSMId(new LinkedList<String>() {{
            add("event1");
            add("event3");
        }}, standardTestMachine.getId())).containsExactly(eventData1, eventData3);
    }

    @Test
    public void testRetrieveByEventNamesAndSmId_forEmptyEventNameSet() throws Exception {
        /* Doesn't matter, but still setting it up */
        final StateMachine standardTestMachine = TestUtils.getStandardTestMachine();
        stateMachinesDAO.create(standardTestMachine);
        final Event event1 = new Event("event1", "someType", Event.EventStatus.pending, standardTestMachine.getId(), null, null);
        eventsDAO.create(event1);
        final Event event3 = new Event("event3", "someType", Event.EventStatus.pending, standardTestMachine.getId(), null, null);
        eventsDAO.create(event3);

        /* Actual test */
        assertThat(eventsDAO.findByEventNamesAndSMId(Collections.<String>emptyList(), standardTestMachine.getId())).isEmpty();
    }
}