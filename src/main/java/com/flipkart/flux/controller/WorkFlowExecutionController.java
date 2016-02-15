/*
 * Copyright 2012-2015, the original author or authors.
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
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.StateMachine;

import java.util.List;

/**
 * @understands Controls the execution flow of a given state machine
 */
public interface WorkFlowExecutionController {
    List<State> init(StateMachine stateMachine); // List of initial states
    List<State> postEvent(Context stateMachineContext, String eventFqn,String eventData); // Accepts an event against a given instance of a state machine and returns list of states that can now be active
}
