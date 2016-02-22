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

package com.flipkart.flux.domain;

import java.util.List;

/**
 * @understands Represents a state machine submitted for execution
 * This class maintains the meta data that can be used to store & show information about the current state of execution of a state machine
 */
public class StateMachine {

    private Long version;

    /* provided */
    private String name;
    private String description;
    private List<State> states;
    private State startState;

    /* maintained */
    private Status status;
    private Status rollbackStatus;
    private Context context;

}
