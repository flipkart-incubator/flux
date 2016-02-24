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
 * @understands Used to track the current situation of the StateMachine.
 * A state, in our implementation, is a task that needs to executed.
 * This class manages the metadata around the status of execution of that task.
 */
public class State {

    /* Defined by the User */
    private Long version;
    private String name;
    private String description;
    private Hook onEntryHook;
    private Task task;
    private Hook onExitHook;
    private Long retryCount;
    private Long timeout;

    /* Maintained by the execution engine */
    private List<FluxError> errors;
    private Status status;
    private Status rollbackStatus;
    private Long numRetries;

    public void enter(Context context) {
        // 1. Begin execution of the task
        // 2. Set next state
        // The return value of the task can either be returned from here, or if we go truly async then
        // the worker executing the task can "Post" it back to the WF engine.
    }
}
