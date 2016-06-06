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
 *
 */

package com.flipkart.flux.impl.message;

import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.Task;
import com.flipkart.flux.impl.task.AkkaTask;

/**
 * <code>TaskAndEvents</code> is a message that composes a {@link Task} identifier and the {@link Event}S that it processes.
 * Used in invoking the {@link AkkaTask} Actor in akka.
 *
 * @author yogesh.nachnani
 *
 */
public class TaskAndEvents {
    /* The string that uniquely identifies a client task to be executed */
    private String taskIdentifier;
    /* The set of events that have presently unblocked the task and whose data will be utilised during task execution */
    private Event[] events;

    public TaskAndEvents(String taskIdentifier, Event[] events) {
        this.taskIdentifier = taskIdentifier;
        this.events = events;
    }

    public String getTaskIdentifier() {
        return taskIdentifier;
    }

    public Event[] getEvents() {
        return this.events;
    }
}
