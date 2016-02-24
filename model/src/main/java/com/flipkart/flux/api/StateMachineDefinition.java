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

package com.flipkart.flux.api;

import com.flipkart.flux.domain.State;

import java.util.List;

/**
 * @understands API model that a user can use to create state machine Instances on flux
 */
public class StateMachineDefinition {
    private String name;
    private String description;
    private List<StateDefinition> states;
    private String stateState;

    public StateMachineDefinition(String description, String name, List<StateDefinition> states, String stateState) {
        this.description = description;
        this.name = name;
        this.states = states;
        this.stateState = stateState;
    }
}
