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
import com.flipkart.flux.persistence.*;
import com.google.inject.name.Named;
import org.hibernate.*;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.*;

/**
 * <code>EventsDAOImpl</code> is an implementation of {@link EventsDAO} which uses Hibernate to perform operations.
 *
 * @author shyam.akirala
 */
// TODO: For all queries, added restriction not to retrieve invalid statuses events. Need to add test cases for it.
public class EventsDAOImpl extends AbstractDAO<Event> implements EventsDAO {

    @Inject
    public EventsDAOImpl(@Named("fluxSessionFactoriesContext") SessionFactoryContext sessionFactoryContext) {
        super(sessionFactoryContext);
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public Event create(String stateMachineInstanceId, Event event) {
        return super.save(event);
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public void updateEvent(String stateMachineInstanceId, Event event) {
        super.update(event);
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public List<Event> findBySMInstanceId(String stateMachineInstanceId) {
        return currentSession().createCriteria(Event.class)
                .add(Restrictions.eq("stateMachineInstanceId", stateMachineInstanceId))
                .add(Restrictions.ne("status", Event.EventStatus.invalid))
                .list();
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public List<Event> findBySMIdAndName(String stateMachineInstanceId, String eventName) {
        Criteria criteria = currentSession().createCriteria(Event.class)
                .add(Restrictions.eq("stateMachineInstanceId", stateMachineInstanceId))
                .add(Restrictions.eq("name", eventName))
                .add(Restrictions.ne("status", Event.EventStatus.invalid));
        return (List<Event>) criteria.list();
    }

    // TODO : Modify query not to send invalid event. Need to add test cases around it.
    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public Event findBySMIdExecutionVersionAndName(String stateMachineInstanceId, String eventName,
                                                   Long executionVersion) {
        Criteria criteria = currentSession().createCriteria(Event.class)
                .add(Restrictions.eq("stateMachineInstanceId", stateMachineInstanceId))
                .add(Restrictions.eq("name", eventName))
                .add(Restrictions.eq("executionVersion", executionVersion));
        return (Event) criteria.uniqueResult();
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public List<String> findTriggeredOrCancelledEventsNamesBySMId(String stateMachineInstanceId) {

        Criteria criteria = currentSession().createCriteria(Event.class).add(Restrictions.eq(
                "stateMachineInstanceId", stateMachineInstanceId))
                .add(Restrictions.or(
                        Restrictions.eq("status", Event.EventStatus.triggered),
                        Restrictions.eq("status", Event.EventStatus.cancelled)))
                        .setProjection(Projections.property("name"));
        return criteria.list();
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public List<String> findReplayEventsNamesBySMId(String stateMachineInstanceId) {

        Criteria criteria = currentSession().createCriteria(Event.class)
                .add(Restrictions.eq("stateMachineInstanceId", stateMachineInstanceId))
                .add(Restrictions.eq("eventSource", "replay"))
                .add(Restrictions.ne("status", Event.EventStatus.invalid))
                .setProjection(Projections.property("name"));
        return criteria.list();
    }


    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public List<Event> findTriggeredEventsBySMId(String stateMachineInstanceId) {
        Criteria criteria = currentSession().createCriteria(Event.class).add(Restrictions.eq(
                "stateMachineInstanceId", stateMachineInstanceId))
                .add(Restrictions.eq("status", Event.EventStatus.triggered));
        return criteria.list();
    }

    // TODO: Add tests for this.
    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public Event findTriggeredEventBySMIdAndName(String stateMachineInstanceId, String eventName) {
        Criteria criteria = currentSession().createCriteria(Event.class)
                .add(Restrictions.eq("stateMachineInstanceId", stateMachineInstanceId))
                .add(Restrictions.eq("name", eventName))
                .add(Restrictions.eq("status", Event.EventStatus.triggered));
        return (Event) criteria.uniqueResult();
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public List<EventData> findByEventNamesAndSMId(String stateMachineInstanceId, List<String> eventNames) {
        if (eventNames.isEmpty()) {
            return new ArrayList<>();
        }
        StringBuilder eventNamesString = new StringBuilder();
        for (int i = 0; i < eventNames.size(); i++) {
            eventNamesString.append("\'" + eventNames.get(i) + "\'");
            if (i != eventNames.size() - 1)
                eventNamesString.append(", ");
        }
        //retrieves and returns the events in the order of eventNames
        Query hqlQuery = currentSession().createQuery("from Event where stateMachineInstanceId = :SMID " +
                "and status != 'invalid' and name in (" + eventNamesString.toString()
                + ") order by field(name, " + eventNamesString.toString() + ")").setParameter("SMID", stateMachineInstanceId);
        List<Event> readEvents = hqlQuery.list();
        LinkedList<EventData> readEventsDTOs = new LinkedList<EventData>();
        for (Event event : readEvents) {
            // Skip replay event source, since it's an optional event.
            if(event.getEventSource() != "replay") {
                readEventsDTOs.add(new EventData(event.getName(), event.getType(), event.getEventData(), event.getEventSource()));
            }
        }
        return readEventsDTOs;
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public Map<String, Event.EventStatus> getAllEventsNameAndStatus(String stateMachineInstanceId, boolean forUpdate) {
        SQLQuery sqlQuery = currentSession().createSQLQuery(
                "Select name, status from Events where  status != 'invalid' and stateMachineInstanceId ='"
                        + stateMachineInstanceId + (forUpdate ? "' for update" : "''"));

        List<Object[]> eventRows = sqlQuery.list();
        Map<String, Event.EventStatus> eventStatusMap = new HashMap<>();

        for(Object[] eventRow : eventRows) {
            eventStatusMap.put((String)eventRow[0], Event.EventStatus.valueOf((String)eventRow[1]));
        }

        return eventStatusMap;
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public void markEventAsCancelled(String stateMachineInstanceId, String eventName) {
        Query query = currentSession().createQuery("update Event set status = :status where" +
                " status != :invalidStatus and stateMachineInstanceId = :stateMachineInstanceId and name = :eventName");
        query.setString("status", Event.EventStatus.cancelled.toString());
        query.setString("invalidStatus", Event.EventStatus.invalid.toString());
        query.setString("stateMachineInstanceId", stateMachineInstanceId);
        query.setString("eventName", eventName);
        query.executeUpdate();
    }

    // TODO: Add test case for this update query.
    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public void markEventsAsInvalid(String stateMachineInstanceId, List<String> eventNames) {
        if (!eventNames.isEmpty()) {
            StringBuilder eventNamesString = new StringBuilder();
            for (int i = 0; i < eventNames.size(); i++) {
                eventNamesString.append("\'" + eventNames.get(i) + "\'");
                if (i != eventNames.size() - 1)
                    eventNamesString.append(", ");
            }
            Query query = currentSession().createQuery("update Event set status = :status where" +
                    " stateMachineInstanceId = :stateMachineInstanceId and name in (" + eventNamesString.toString() + ")");
            query.setString("status", Event.EventStatus.invalid.toString());
            query.setString("stateMachineInstanceId", stateMachineInstanceId);
            query.executeUpdate();
        }
    }


}