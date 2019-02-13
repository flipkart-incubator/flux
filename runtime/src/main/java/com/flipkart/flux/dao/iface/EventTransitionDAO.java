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

package com.flipkart.flux.dao.iface;

import com.flipkart.flux.api.EventData;
import com.flipkart.flux.domain.EventTransition;

import java.util.List;
import java.util.Map;

/**
 * <code>EventTransitionDAO</code> interface provides methods to perform CR operations on {@link EventTransition}
 * @author akif.khan
 */
public interface EventTransitionDAO {

    /** Creates EventTransition in the db, and returns the saved object*/
    EventTransition create(String stateMachineInstanceId, EventTransition eventTransition);

    /** Updates the eventTransition */
    void updateEventTransition(String stateMachineInstanceId, EventTransition eventTransition);

    /** Retrieves all the eventTransition which belongs to a particular state machine instance*/
    List<EventTransition> findBySMInstanceId(String stateMachineInstanceId);


    /** Retrieves EventTransition by state machine instance id and event name */
    EventTransition findBySMIdAndName(String stateMachineInstanceId, String eventName);

    /** Retrieves list of event names which are in triggered state and belongs to provided state machine */
    List<String> findTriggeredOrCancelledEventsNamesBySMId(String stateMachineInstanceId);

    /** Retrieves list of eventTransition which are in triggered state and belongs to provided state machine */
    List<EventTransition> findTriggeredEventsBySMId(String stateMachineInstanceId);

    /** Retrieves list of eventTransition by their names and state machine id */
    List<EventTransition> findByEventNamesAndSMId(String stateMachineInstanceId, List<String> eventNames );


    /** Retrieves all the events names and statuses. Selects for update if forUpdate is true */
    Map<String, EventTransition.EventStatus> getAllEventsNameAndStatus(String stateMachineInstanceId, boolean forUpdate);

    /** Marks an event as cancelled */
    void markEventAsCancelled(String stateMachineInstanceId, String eventName);
}