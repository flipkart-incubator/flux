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
package com.flipkart.flux.api.core;

import com.flipkart.flux.impl.message.TaskAndEvents;

/**
 * TaskExecutionMessage is the entity which encapsulate executionData and the Akka Router name to which the message
 * should be dispatched for execution.
 */
public class TaskExecutionMessage {
    //name of the Akka task router to which this Object belongs
    private String routerName;
    private TaskAndEvents akkaMessage;

    public TaskExecutionMessage(String routerName, TaskAndEvents akkaMessage) {
        this.routerName = routerName;
        this.akkaMessage = akkaMessage;
    }

    /*default constructor */
    public TaskExecutionMessage() {
    }

    public String getRouterName() {
        return routerName;
    }

    public void setRouterName(String routerName) {
        this.routerName = routerName;
    }

    public TaskAndEvents getAkkaMessage() {
        return akkaMessage;
    }

    public void setAkkaMessage(TaskAndEvents akkaMessage) {
        this.akkaMessage = akkaMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TaskExecutionMessage)) {
            return false;
        }

        TaskExecutionMessage that = (TaskExecutionMessage) o;

        if (!routerName.equals(that.routerName)) {
            return false;
        }
        return akkaMessage.equals(that.akkaMessage);
    }

    @Override
    public int hashCode() {
        int result = routerName.hashCode();
        result = 31 * result + akkaMessage.hashCode();
        return result;
    }
}
