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

import com.flipkart.flux.api.EventData;
import com.flipkart.flux.dao.iface.EventsDAO;
import com.flipkart.flux.domain.Event;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * <code>EventsDAOImpl</code> is an implementation of {@link EventsDAO} which uses Hibernate to perform operations.
 * @author shyam.akirala
 */
public class EventsDAOImpl extends AbstractDAO<Event> implements EventsDAO {

    @Inject
    public EventsDAOImpl(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @Override
    @Transactional
    public Event create(Event event) {
        return super.save(event);
    }

    @Override
    @Transactional
    public void updateEvent(Event event) {
        super.update(event);
    }

    @Override
    @Transactional
    public List<Event> findBySMInstanceId(Long stateMachineInstanceId) {
        return currentSession().createCriteria(Event.class).add(Restrictions.eq("stateMachineInstanceId", stateMachineInstanceId)).list();
    }

    @Override
    @Transactional
    public Event findById(Long id) {
        return super.findById(Event.class, id);
    }

    @Override
    @Transactional
    public Event findBySMIdAndName(Long stateMachineInstanceId, String eventName) {
        Criteria criteria = currentSession().createCriteria(Event.class).add(Restrictions.eq("stateMachineInstanceId", stateMachineInstanceId))
                .add(Restrictions.eq("name", eventName));
        return (Event) criteria.uniqueResult();
    }

    @Override
    @Transactional
    public List<String> findTriggeredEventsNamesBySMId(Long stateMachineInstanceId) {
        Criteria criteria = currentSession().createCriteria(Event.class).add(Restrictions.eq("stateMachineInstanceId", stateMachineInstanceId))
                .add(Restrictions.eq("status", Event.EventStatus.triggered))
                .setProjection(Projections.property("name"));
        return criteria.list();
    }

    @Override
    @Transactional
    public List<Event> findTriggeredEventsBySMId(Long stateMachineInstanceId) {
        Criteria criteria = currentSession().createCriteria(Event.class).add(Restrictions.eq("stateMachineInstanceId", stateMachineInstanceId))
                .add(Restrictions.eq("status", Event.EventStatus.triggered));
        return criteria.list();
    }

    @Override
    @Transactional
    public List<EventData> findByEventNamesAndSMId(Set<String> eventNames, Long stateMachineInstanceId) {
        if (eventNames.isEmpty()) {
            return new ArrayList<>();
        }
        Criteria criteria = currentSession().createCriteria(Event.class).add(Restrictions.eq("stateMachineInstanceId", stateMachineInstanceId))
                .add(Restrictions.in("name", eventNames));
        List<Event> readEvents = criteria.list();
        LinkedList<EventData> readEventsDTOs = new LinkedList<EventData>();
        for (Event event : readEvents) {
        	readEventsDTOs.add(new EventData(event.getName(), event.getType(),event.getEventData(), event.getEventSource()));
        }
        return readEventsDTOs;
    }

}
