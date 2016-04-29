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

import com.flipkart.flux.dao.iface.EventsDAO;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.rule.DbClearWithTestSMRule;
import com.flipkart.flux.runner.GuiceJunit4Runner;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

/**
 * <code>EventsDAOTest</code> class tests the functionality of {@link EventsDAO} using JUnit tests.
 * @author shyam.akirala
 * @author kartik.bommepally
 */
@RunWith(GuiceJunit4Runner.class)
public class EventsDAOTest {

    @Inject
    EventsDAO eventsDAO;

    @Inject
    @Rule
    public DbClearWithTestSMRule dbClearWithTestSMRule;

    @Before
    public void setup() {}

    @Test
    public void createEventTest() {
        StateMachine stateMachine = dbClearWithTestSMRule.getStateMachine();
        DummyEventData data = new DummyEventData("event_dat");
        Event<DummyEventData> event = new Event<DummyEventData>("test_event_name","Internal", Event.EventStatus.pending,stateMachine.getId(), data,"state1");
        Long eventId = eventsDAO.create(event).getId();

        Event event1 = eventsDAO.findById(eventId);
        Assert.assertEquals(event, event1);
    }
}