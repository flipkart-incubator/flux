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

import com.flipkart.flux.domain.Event;

import java.util.List;
import java.util.Set;

/**
 * <code>EventsDAO</code> interface provides methods to perform CR operations on {@link Event}
 * @author shyam.akirala
 */
public interface EventsDAO {

    /** Creates Event in the db, and returns the saved object*/
    Event create(Event event);

    /** Updates the event */
    void update(Event event);

    /** Retrieves all the events which belongs to a particular state machine instance*/
    List<Event> findBySMInstanceId(Long stateMachineInstanceId);

    /** Retrieves Event by it's unique identifier*/
    Event findById(Long id);

    /** Retrieves Event by state machine instance id and event name */
    Event findBySMIdAndName(Long stateMachineInstanceId, String eventName);

    /** Retrieves list of event names which are in triggered state and belongs to provided state machine */
    public List<String> findTriggeredEventsNamesBySMId(Long stateMachineInstanceId);

    /** Retrieves list of events by their names and state machine id */
    public List<Event> findByEventNamesAndSMId(Set<String> eventNames, Long stateMachineInstanceId);
}