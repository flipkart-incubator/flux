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

package com.flipkart.flux.controller;

import com.flipkart.flux.domain.Context;
import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.StateMachine;

import java.util.List;

/**
 * @understands Controls the execution flow of a given state machine
 */
public interface WorkFlowExecutionController {
    /**
     * Perform init operations on a state machine.
     * This can include creating the Context for the first time and storing it.
     * Figuring out and returning the list of task that are independent and can begin execution
     * @param stateMachine
     * @return List of states that do not have any event dependencies on them
     */
    List<State> init(StateMachine stateMachine);

    /**
     * Attaches the event to the stateMachineContext.
     * The stateMachineContext can then be used to retrieve the states that have now been enabled and ready for execution
     * @param stateMachineContext
     * @param eventFqn
     * @param eventData
     * @return List of states unblocked by the given event
     */
    List<State> postEvent(Context stateMachineContext, String eventFqn,String eventData); // Accepts an event against a given instance of a state machine and returns list of states that can now be active
}
