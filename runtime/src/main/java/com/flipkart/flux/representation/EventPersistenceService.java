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

import com.flipkart.flux.api.EventMetaDataDefinition;
import com.flipkart.flux.api.EventTransitionDefinition;
import com.flipkart.flux.dao.iface.EventMetaDataDAO;
import com.flipkart.flux.dao.iface.EventTransitionDAO;
import com.flipkart.flux.domain.EventMetaData;
import com.flipkart.flux.domain.EventTransition;
import com.flipkart.flux.domain.Validity;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * <code>EventPersistenceService</code> class converts user provided Event entity definition to domain type object and stores in DB.
 * @author shyam.akirala
 */
@Singleton
public class EventPersistenceService {

    private EventTransitionDAO eventTransitionDAO;
    private EventMetaDataDAO eventMetaDataDAO;

    @Inject
    public EventPersistenceService(EventTransitionDAO eventTransitionDAO, EventMetaDataDAO eventMetaDataDAO) {
        this.eventTransitionDAO = eventTransitionDAO;
        this.eventMetaDataDAO = eventMetaDataDAO;
    }

    public EventMetaData convertEventeMetaDataDefinitionToEventMetaData(
            EventMetaDataDefinition eventMetaDataDefinition) {
        return new EventMetaData(eventMetaDataDefinition.getName(), eventMetaDataDefinition.getType(),
                eventMetaDataDefinition.getSmId(), eventMetaDataDefinition.getDependentStates());
    }

    public EventTransition convertEventTransitionDefinitionToEventTransition(
            EventTransitionDefinition eventTransitionDefinition) {
        return new EventTransition(eventTransitionDefinition.getName(), Validity.yes,
                EventTransition.EventStatus.pending, eventTransitionDefinition.getcorrelationId(),
                eventTransitionDefinition.getExecutionVersion(), eventTransitionDefinition.getEventData(),
                eventTransitionDefinition.getEventSource());
    }

    public EventMetaData persistEventMetaData(EventMetaData eventMetaData) {
        return eventMetaDataDAO.create(eventMetaData.getStateMachineInstanceId(), eventMetaData);
    }

    public EventTransition persistEvent(EventTransition eventTransition) {
        return eventTransitionDAO.create(eventTransition.getStateMachineInstanceId(), eventTransition);
    }

}
