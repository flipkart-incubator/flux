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

package com.flipkart.flux.impl.message;

import com.flipkart.flux.domain.Status;

import java.io.Serializable;

/**
 * <code>SerializedRedriverTask</code> is a DTO class which is used in fetching required Redriver task details from Flux Runtime.
 * @author shyam.akirala
 */
public class SerializedRedriverTask implements Serializable {

    private TaskAndEvents taskAndEvents;

    private Status taskStatus;

    /** Constructors*/
    public SerializedRedriverTask() {}
    public SerializedRedriverTask(TaskAndEvents taskAndEvents, Status taskStatus) {
        this.taskAndEvents = taskAndEvents;
        this.taskStatus = taskStatus;
    }

    /** Accessor methods*/
    public TaskAndEvents getTaskAndEvents() {
        return taskAndEvents;
    }
    public Status getTaskStatus() {
        return taskStatus;
    }
}
