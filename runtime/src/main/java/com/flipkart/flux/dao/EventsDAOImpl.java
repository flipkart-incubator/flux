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

import com.flipkart.flux.api.VersionedEventData;
import com.flipkart.flux.constant.RuntimeConstants;
import com.flipkart.flux.dao.iface.EventsDAO;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.Event.EventStatus;
import com.flipkart.flux.persistence.DataSourceType;
import com.flipkart.flux.persistence.SelectDataSource;
import com.flipkart.flux.persistence.SessionFactoryContext;
import com.flipkart.flux.persistence.Storage;
import com.google.inject.name.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

/**
 * <code>EventsDAOImpl</code> is an implementation of {@link EventsDAO} which uses Hibernate to perform operations.
 *
 * @author shyam.akirala
 */
// TODO: For all queries, added restriction not to retrieve invalid statuses events. Need to add test cases for it.
public class EventsDAOImpl extends AbstractDAO<Event> implements EventsDAO {

    private static final String TABLE_NAME = "Event";
    private static final String COLUMN_STATE_MACHINE_INSTANCE_ID = "stateMachineInstanceId";
    private static final String COLUMN_STATUS = "status";
    private static final String COLUMN_NAME = "name";

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

    /**
     * Returns the list of events which are not marked as invalid
     * @param stateMachineInstanceId Identifier for the state machine
     * @return
     */
    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public List<Event> findBySMInstanceId(String stateMachineInstanceId) {
        return currentSession().createCriteria(Event.class)
                .add(Restrictions.eq("stateMachineInstanceId", stateMachineInstanceId))
                .add(Restrictions.ne("status", Event.EventStatus.invalid))
                .list();
    }

    /**
     * Returns the list of events that are not marked invalid
     * @param stateMachineInstanceId State Machine Identifier
     * @param eventName Name of the event
     * @return
     */
    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public Event findValidEventBySMIdAndName(String stateMachineInstanceId, String eventName) {
        Criteria criteria = currentSession().createCriteria(Event.class)
                .add(Restrictions.eq("stateMachineInstanceId", stateMachineInstanceId))
                .add(Restrictions.eq("name", eventName))
                .add(Restrictions.ne("status", Event.EventStatus.invalid));
        return (Event) criteria.uniqueResult();
    }

    /**
     * Retrieves all the events with the given name irrespective of its status
     * @param stateMachineInstanceId
     * @param eventName
     */
    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE,storage = Storage.SHARDED)
    public List<Event> findAllBySMIdAndName(String stateMachineInstanceId, String eventName) {
        Criteria criteria = currentSession().createCriteria(Event.class)
            .add(Restrictions.eq("stateMachineInstanceId", stateMachineInstanceId))
            .add(Restrictions.eq("name", eventName));
        return criteria.list();
    }

    // TODO : This may throw NULL results. Check and remove invalid event check safely or handle null results.
    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public Event findValidEventsByStateMachineIdAndExecutionVersionAndName(String stateMachineInstanceId, String eventName,
                                                   Long executionVersion) {
        Criteria criteria = currentSession().createCriteria(Event.class)
                .add(Restrictions.eq("stateMachineInstanceId", stateMachineInstanceId))
                .add(Restrictions.eq("name", eventName))
                .add(Restrictions.ne("status", EventStatus.invalid))
                .add(Restrictions.eq("executionVersion", executionVersion));
        return (Event) criteria.uniqueResult();
    }

    /**
     * @return all valid events for the list of names
     */
    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public List<Event> findAllValidEventsByStateMachineIdAndExecutionVersionAndName(
        String stateMachineInstanceId, List<String> eventNames, Long executionVersion) {
        Criteria criteria = currentSession().createCriteria(Event.class)
            .add(Restrictions.eq("stateMachineInstanceId", stateMachineInstanceId))
            .add(Restrictions.in("name", eventNames))
            .add(Restrictions.ne("status", EventStatus.invalid))
            .add(Restrictions.eq("executionVersion", executionVersion));
        return criteria.list();
    }

    /**
     * Returns the List of all event names that are either triggered or cancelled
     * @param stateMachineInstanceId State Machine Identifier
     * @return
     */
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

    /**
     * @param stateMachineInstanceId State Machine Identifier
     * @return Returns the list of replay event names
     */
    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public List<String> findAllValidReplayEventsNamesBySMId(String stateMachineInstanceId) {

        Criteria criteria = currentSession().createCriteria(Event.class)
                .add(Restrictions.eq("stateMachineInstanceId", stateMachineInstanceId))
                .add(Restrictions.ilike("eventSource", RuntimeConstants.REPLAY_EVENT, MatchMode.ANYWHERE))
                .add(Restrictions.ne("status", EventStatus.invalid))
                .setProjection(Projections.property("name"));
        return criteria.list();
    }

    /**
     *
     * @param stateMachineInstanceId
     * @param eventName
     * @return
     */
    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public Optional<Event> findValidReplayEventBySMIdAndName(String stateMachineInstanceId, String eventName) {
        Criteria criteria = currentSession().createCriteria(Event.class)
                .add(Restrictions.eq("stateMachineInstanceId", stateMachineInstanceId))
                .add(Restrictions.eq("name", eventName))
                .add(Restrictions.ilike("eventSource", RuntimeConstants.REPLAY_EVENT, MatchMode.ANYWHERE))
                .add(Restrictions.ne("status", EventStatus.invalid));

        Object object = criteria.uniqueResult();
        Event castedObject = null;
        if(object != null)
            castedObject = (Event) object;
        return Optional.ofNullable(castedObject);
    }

    /**
     * Returns the list of the events that are in triggered state
     * @param stateMachineInstanceId State achine identifier
     * @return
     */
    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public List<Event> findTriggeredEventsBySMId(String stateMachineInstanceId) {
        Criteria criteria = currentSession().createCriteria(Event.class).add(Restrictions.eq(
                "stateMachineInstanceId", stateMachineInstanceId))
                .add(Restrictions.eq("status", Event.EventStatus.triggered));
        return criteria.list();
    }

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
    public List<VersionedEventData> findByEventNamesAndSMId(String stateMachineInstanceId, List<String> eventNames) {
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
        LinkedList<VersionedEventData> readEventsDTOs = new LinkedList<>();
        //TODO: Check again
        for (Event event : readEvents) {
            readEventsDTOs.add(new VersionedEventData(event.getName(), event.getType(), event.getEventData(),
                    event.getEventSource(), event.getExecutionVersion()));
        }
        return readEventsDTOs;
    }

    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public Map<String, Event.EventStatus> getAllEventsNameAndStatus(String stateMachineInstanceId, boolean forUpdate) {
        SQLQuery sqlQuery = currentSession().createSQLQuery(
                "Select name, status from Events where  status != 'invalid' and stateMachineInstanceId ='"
                        + stateMachineInstanceId + (forUpdate ? "' for update" : "'"));

        List<Object[]> eventRows = sqlQuery.list();
        Map<String, Event.EventStatus> eventStatusMap = new HashMap<>();

        for (Object[] eventRow : eventRows) {
            eventStatusMap.put((String) eventRow[0], Event.EventStatus.valueOf((String) eventRow[1]));
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

    @Override
    public Event create_NonTransactional(Event event, Session session) {
        session.save(event);
        return event;
    }

    @Override
    public void markEventAsInvalid_NonTransactional(String stateMachineInstanceId, String eventName, Session session) {
        Query query = session.createQuery("update Event set status = :status where" +
                " stateMachineInstanceId = :stateMachineInstanceId and name = :eventName");
        query.setString("status", EventStatus.invalid.toString());
        query.setString("stateMachineInstanceId", stateMachineInstanceId);
        query.setString("eventName", eventName);
        query.executeUpdate();
    }

    //TODO: Check and validate query + Test cases
    @Override
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public void deleteInvalidEvents(String stateMachineInstanceId, List<String> eventNames) {
        if (!eventNames.isEmpty()) {
            StringBuilder eventNamesString = new StringBuilder();
            for (int i = 0; i < eventNames.size(); i++) {
                eventNamesString.append("\'" + eventNames.get(i) + "\'");
                if (i != eventNames.size() - 1)
                    eventNamesString.append(", ");
            }
            Query query = currentSession().createQuery("delete from " + TABLE_NAME + " where "
                    + COLUMN_STATE_MACHINE_INSTANCE_ID + " = :stateMachineInstanceId and " + COLUMN_NAME + " in (" + eventNamesString.toString() + ")"
                    + " and " + COLUMN_STATUS + " = :status");
            query.setString("status", Event.EventStatus.invalid.toString());
            query.setString("stateMachineInstanceId", stateMachineInstanceId);
            query.executeUpdate();
        }
    }

}