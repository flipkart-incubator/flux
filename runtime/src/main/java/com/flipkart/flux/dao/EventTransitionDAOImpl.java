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
import com.flipkart.flux.dao.iface.EventTransitionDAO;
import com.flipkart.flux.domain.EventTransition;
import com.flipkart.flux.persistence.*;
import com.google.inject.name.Named;
import org.hibernate.*;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.*;

/**
 * <code>EventTransitionDAOImpl</code> is an implementation of {@link EventTransitionDAO} which uses Hibernate to perform operations.
 *
 * @author akif.khan
 */
public class EventTransitionDAOImpl extends AbstractDAO<EventTransition> implements EventTransitionDAO {

    @Inject
    public EventTransitionDAOImpl(@Named("fluxSessionFactoriesContext") SessionFactoryContext sessionFactoryContext) {
        super(sessionFactoryContext);
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public EventTransition create(String stateMachineId, EventTransition eventTransition) {
        return super.save(eventTransition);
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public void updateEventTransition(String stateMachineId, EventTransition eventTransition) {
        super.update(eventTransition);
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public List<EventTransition> findBySMInstanceId(String stateMachineId) {
        return currentSession().createCriteria(EventTransition.class).add(Restrictions.eq("stateMachineId", stateMachineId)).list();
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public EventTransition findBySMIdAndName(String stateMachineId, String eventName) {
        Criteria criteria = currentSession().createCriteria(EventTransition.class).add(Restrictions.eq("stateMachineId", stateMachineId))
                .add(Restrictions.eq("name", eventName));
        return (EventTransition) criteria.uniqueResult();
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public List<String> findTriggeredOrCancelledEventsNamesBySMId(String stateMachineId) {

        Criteria criteria = currentSession().createCriteria(EventTransition.class).add(Restrictions.eq(
                "stateMachineId", stateMachineId))
                .add(Restrictions.or(
                        Restrictions.eq("status", EventTransition.EventStatus.triggered),
                        Restrictions.eq("status", EventTransition.EventStatus.cancelled)))
                .setProjection(Projections.property("name"));
        return criteria.list();
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public List<EventTransition> findTriggeredEventsBySMId(String stateMachineId) {
        Criteria criteria = currentSession().createCriteria(EventTransition.class).add(Restrictions.eq(
                "stateMachineId", stateMachineId))
                .add(Restrictions.eq("status", EventTransition.EventStatus.triggered));
        return criteria.list();
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public List<EventData> findByEventNamesAndSMId(String stateMachineId, List<String> eventNames) {
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
        Query hqlQuery = currentSession().createQuery(
                "from EventTransition where stateMachineId = :SMID and name in (" + eventNamesString.toString()
                + ") order by field(name, " + eventNamesString.toString() + ")").setParameter("SMID", stateMachineId);
        List<EventTransition> readEvents = hqlQuery.list();
        LinkedList<EventData> readEventsDTOs = new LinkedList<EventData>();
        // temporarily passing event type as 'internal' for POC run. It shouldbe retrieved from EventMetaData
        for (EventTransition event : readEvents) {
            readEventsDTOs.add(new EventData(event.getName(), "internal", event.getEventData(), event.getEventSource(),
                    event.getExecutionVersion()));
        }
        return readEventsDTOs;
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public Map<String, EventTransition.EventStatus> getAllEventsNameAndStatus(String stateMachineId, boolean forUpdate) {
        SQLQuery sqlQuery = currentSession().createSQLQuery(
                "Select name, status from EventTransition where stateMachineId ='" + stateMachineId + (forUpdate ? "' for update" : "''"));

        List<Object[]> eventRows = sqlQuery.list();
        Map<String, EventTransition.EventStatus> eventStatusMap = new HashMap<>();

        for(Object[] eventRow : eventRows) {
            eventStatusMap.put((String)eventRow[0], EventTransition.EventStatus.valueOf((String)eventRow[1]));
        }

        return eventStatusMap;
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public void markEventAsCancelled(String stateMachineId, String eventName) {
        Query query = currentSession().createQuery("update EventTransition set status = :status where stateMachineId = :stateMachineId and name = :eventName");
        query.setString("status", EventTransition.EventStatus.cancelled.toString());
        query.setString("stateMachineId", stateMachineId);
        query.setString("eventName", eventName);
        query.executeUpdate();
    }

}
