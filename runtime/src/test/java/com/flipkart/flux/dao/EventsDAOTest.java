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

import com.flipkart.flux.guice.modules.HibernateModule;
import com.flipkart.flux.dao.iface.EventsDAO;
import com.flipkart.flux.domain.Event;
import com.google.inject.Guice;
import com.google.inject.Injector;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;

/**
 * @author shyam.akirala
 */
public class EventsDAOTest {

    private Injector injector;

    @Before
    public void setup() {
        injector = Guice.createInjector(new HibernateModule());
    }

    @Test
    public void createEventTest() {
        EventsDAO eventsDAO = injector.getInstance(EventsDAO.class);
        EventData data = new EventData("external_event", "test_event_info");
        Event<EventData> event = new Event("test_name","test_type", Event.EventStatus.pending,"test_state_machine_instance_id", data,"internal_event");
        Long eventId = eventsDAO.create(event).getId();

        Event event1 = eventsDAO.findById(eventId);
        Assert.assertNotNull(event1);
    }

}

class EventData implements Serializable{
    String eventType;
    String eventInfo;

    public EventData(String eventType, String eventInfo) {
        this.eventType = eventType;
        this.eventInfo = eventInfo;
    }
    public String getEventType() {
        return eventType;
    }
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    public String getEventInfo() {
        return eventInfo;
    }
    public void setEventInfo(String eventInfo) {
        this.eventInfo = eventInfo;
    }
}