/*
 * Copyright 2012-2019, the original author or authors.
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

import com.flipkart.flux.domain.StateTraversalPath;

import java.util.List;
import java.util.Optional;

/**
 * <code>StateTraversalPathDAO</code> interface provides methods to perform CR operations on {@link StateTraversalPath}
 *
 * @author akif.khan
 */
public interface StateTraversalPathDAO {

    /**
     * Creates state traversal path and returns saved object
     */
    StateTraversalPath create(String StateMachineInstanceId, StateTraversalPath stateTraversalPath);

    /**
     * Retrieves state traversal path for given stateMachineId and stateId
     */
    Optional<StateTraversalPath> findById(String stateMachineId, Long stateId);

    /**
     * This query is not used anywhere in application's functionality as of now.
     * Retrieves list of states with their respective traversal path for given stateMachineId
     */
    List<StateTraversalPath> findByStateMachineId(String stateMachineId);
}