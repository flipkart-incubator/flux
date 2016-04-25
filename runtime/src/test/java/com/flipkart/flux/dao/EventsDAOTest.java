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

import org.junit.Before;
import org.junit.Test;

import com.flipkart.flux.dao.iface.EventsDAO;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.guice.module.ConfigModule;
import com.flipkart.flux.guice.module.HibernateModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import junit.framework.Assert;

/**
 * <code>EventsDAOTest</code> class tests the functionality of {@link EventsDAO} using JUnit tests.
 * @author shyam.akirala
 * @author kartik.bommepally
 */
public class EventsDAOTest {

    private Injector injector;

    @Before
    public void setup() {
        injector = Guice.createInjector(new ConfigModule(), new HibernateModule());
    }

    @Test
    public void createEventTest() {
        EventsDAO eventsDAO = injector.getInstance(EventsDAO.class);
        DummyEventData data = new DummyEventData("event_dat");
        Event<DummyEventData> event = new Event("test_name","test_type", Event.EventStatus.pending,1L, data,"internal_event");
        Long eventId = eventsDAO.create(event).getId();

        Event event1 = eventsDAO.findById(eventId);
        Assert.assertEquals(event, event1);
    }

}