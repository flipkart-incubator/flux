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

package com.flipkart.flux.persistence.dao.iface;

import com.flipkart.flux.api.VersionedEventData;
import com.flipkart.flux.domain.Event;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.hibernate.Session;

/**
 * <code>EventsDAO</code> interface provides methods to perform CR operations on {@link Event}
 * @author shyam.akirala
 */
public interface EventsDAO {

    /**
     * Creates Event in the db, and returns the saved object
     */
    Event create(String stateMachineInstanceId, Event event);

    /**
     * Updates the event
     */
    void updateEvent(String stateMachineInstanceId, Event event);

    /**
     * Retrieves all the events which belongs to a particular state machine instance
     */
    List<Event> findBySMInstanceId(String stateMachineInstanceId);

    /** Retrieves valid[pending/triggered/cancelled] Event by state machine instance id and event name */
    Event findValidEventBySMIdAndName(String stateMachineInstanceId, String eventName);

    /**
     * Retrieves all the events with the given name irrespective of its status
     * @param stateMachineInstanceId
     * @param eventName
     * @return
     */
    List<Event> findAllBySMIdAndName(String stateMachineInstanceId, String eventName);

    /** Retrieves Event by state machine instance id, event execution version and event name */
    Event findValidEventsByStateMachineIdAndExecutionVersionAndName(String stateMachineInstanceId, String eventName, Long executionVersion);

    /**
     *
     * @param stateMachineInstanceId
     * @param eventNames
     * @param executionVersion
     * @return all valid events for the list of names
     */
    List<Event> findAllValidEventsByStateMachineIdAndExecutionVersionAndName(String stateMachineInstanceId, List<String> eventNames, Long executionVersion);

    /**
     * Retrieves list of events which are in triggered/cancelled state and belongs to provided state machine
     */
    List<String> findTriggeredOrCancelledEventsNamesBySMId(String stateMachineInstanceId);

    /** Retrieves list of valid events whose eventSource is <code>RuntimeConstants.REPLAY_EVENT</code>
     *  and belongs to provided state machine
     */
    List<String> findAllValidReplayEventsNamesBySMId(String stateMachineInstanceId);

    /** Retrieves valid event name matching input Event name whose eventSource is <code>RuntimeConstants.REPLAY_EVENT</code>
     *  and belongs to provided state machine.
     */
    Optional<Event> findValidReplayEventBySMIdAndName(String stateMachineInstanceId, String eventName);

    /** Retrieves list of events which are in triggered state and belongs to provided state machine */
    /**
     * Retrieves list of events which are in triggered state and belongs to provided state machine
     */
    List<Event> findTriggeredEventsBySMId(String stateMachineInstanceId);

    /**
     * Retrieves event which is in triggered state, event name and belongs to provided state machine
     */
    Event findTriggeredEventBySMIdAndName(String stateMachineInstanceId, String eventName);


    /** Retrieves list of events by their names and state machine id */
    List<VersionedEventData> findByEventNamesAndSMId(String stateMachineInstanceId, List<String> eventNames );

    /**
     * Deletes the list of invalid events
     */
    void deleteInvalidEvents(String stateMachineInstanceId, List<String> eventNames);

    /**
     * Retrieves all the events names and statuses. Selects for update if forUpdate is true
     */
    Map<String, Event.EventStatus> getAllEventsNameAndStatus(String stateMachineInstanceId, boolean forUpdate);

    /**
     * Marks an event as cancelled
     */
    void markEventAsCancelled(String stateMachineInstanceId, String eventName);

    /**
     * Creates Event using the parameter session and returns the saved object
     */
    Event create_NonTransactional(Event event, Session session);

    /**
     * Mark given event as invalid using passed parameter session object
     */
    void markEventAsInvalid_NonTransactional(String stateMachineInstanceId, String eventName, Session session);

    /**
     * Retrieves the Event
     * @param stateMachineInstanceId
     * @param eventName
     * @param executionVersion
     * @return
     */
    Event findBySmIdAndNameAndVersion(String stateMachineInstanceId,String eventName, Long executionVersion);
}