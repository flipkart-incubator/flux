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

package com.flipkart.flux.representation;

import com.flipkart.flux.api.EventDefinition;
import com.flipkart.flux.dao.iface.EventsDAO;
import com.flipkart.flux.domain.Event;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * <code>EventPersistenceService</code> class converts user provided Event entity definition to domain type object and stores in DB.
 * @author shyam.akirala
 */
@Singleton
public class EventPersistenceService {

    private EventsDAO eventsDAO;

    @Inject
    public EventPersistenceService(EventsDAO eventsDAO) {
        this.eventsDAO = eventsDAO;
    }

    /**
     * Converts {@link EventDefinition} to domain object {@link Event}
     * @param eventDefinition
     * @return
     */
    public Event convertEventDefinitionToEvent(EventDefinition eventDefinition) {
        return new Event(eventDefinition.getName(), eventDefinition.getType(), Event.EventStatus.pending, null, null, null);
    }

    /**
     * Persists the event in the DB.
     * @param event
     * @return created event
     */
    public Event persistEvent(Event event) {
        return eventsDAO.create(event);
    }

}
