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
import com.flipkart.flux.domain.EventMetaData;

import java.util.List;
import java.util.Map;

/**
 * <code>EventMetaDataDAO</code> interface provides methods to perform CR operations on {@link EventMetaData}
 * @author akif.khan
 */
public interface EventMetaDataDAO {

    /** Creates EventMetaData in the db, and returns the saved object*/
    EventMetaData create(String stateMachineInstanceId, EventMetaData eventMetaData);

    /** Retrieves all the eventMetaData which belongs to a particular state machine instance*/
    List<EventMetaData> findBySMInstanceId(String stateMachineInstanceId);

    /** Retrieves EventMetaData by state machine instance id and event name */
    EventMetaData findBySMIdAndName(String stateMachineInstanceId, String eventName);
}