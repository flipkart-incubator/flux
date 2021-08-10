/*
 * Copyright 2012-2018, the original author or authors.
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

package com.flipkart.flux.examples.eventupdate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flipkart.flux.client.model.*;

/**
 * This class is an example to test Event Data Update based on a boolean flag passed to a task.
 * By default task2 will fail with failDependentTask set to true,
 * event update of event paramEvent1 via /{machineId}/context/eventupdate api should be used to set failDependentTask
 * to false.
 * Created by akif.khan on 31/08/2018.
 */
public class EventUpdateWorkflow {

    @Workflow(version = 1)
    public void create(StartEvent startEvent) {
        ParamEvent paramEvent1 = task1();
        ParamEvent paramEvent2 = task2(paramEvent1);
        ParamEvent paramEvent3 = task3(paramEvent2);
        ParamEvent paramEvent4 = task4(paramEvent1);
        ParamEvent paramEvent5 = task5(paramEvent4);
        ParamEvent paramEvent6 = task6(paramEvent3, paramEvent5);
    }

    @Task(version = 1, retries = 0, timeout = 1000L)
    public ParamEvent task1() {
        return new ParamEvent("task1", true);
    }

    @Task(version = 1, retries = 0, timeout = 400L)
    public ParamEvent task2(ParamEvent event) {
        if (event.failDependentTask) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println("interrupted");
                e.printStackTrace();
            }
        }
        return new ParamEvent("task2", false);
    }

    @Task(version = 1, retries = 0, timeout = 1000L)
    public ParamEvent task3(ParamEvent event) {
        return new ParamEvent("task3", false);
    }

    @Task(version = 1, retries = 0, timeout = 400L)
    public ParamEvent task4(ParamEvent event) {
        if (event.failDependentTask) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println("interrupted");
                e.printStackTrace();
            }
        }
        return new ParamEvent("task4", false);
    }

    @Task(version = 1, retries = 0, timeout = 1000L)
    public ParamEvent task5(ParamEvent event) {
        return new ParamEvent("task5", false);
    }

    @Task(version = 1, retries = 0, timeout = 1000L)
    public ParamEvent task6(ParamEvent event, ParamEvent event1) {
        return new ParamEvent("task6", false);
    }
}

class ParamEvent implements Event {

    @JsonProperty
    String data;

    @JsonProperty
    Boolean failDependentTask;

    public ParamEvent() {
    }

    public ParamEvent(String data, Boolean failDependentTask) {
        this.data = data;
        this.failDependentTask = failDependentTask;
    }
}

class StartEvent implements Event {

    @CorrelationId
    @JsonProperty
    String id;

    public StartEvent() {
    }

    public StartEvent(String id) {
        this.id = id;
    }
}
