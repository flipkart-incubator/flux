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

package com.flipkart.flux.examples.cancelpath;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flipkart.flux.client.exception.FluxCancelPathException;
import com.flipkart.flux.client.model.*;

/**
 * This class shows how to use {@link FluxCancelPathException} to cancel a path in state machine.
 *
 * Created by shyam.akirala on 29/08/17.
 */
public class CancelPathWorkflow {

    @Workflow(version = 1)
    public void create(StartEvent startEvent) {
        ParamEvent paramEvent1 = task1();
        ParamEvent paramEvent2 = task2(paramEvent1);
        ParamEvent paramEvent3 = task3(paramEvent1);
        ParamEvent paramEvent4 = task4(paramEvent2, paramEvent3);
        ParamEvent paramEvent5 = task5(paramEvent2);
        ParamEvent paramEvent6 = task6(paramEvent5);
        ParamEvent paramEvent7 = task7(paramEvent5);
        ParamEvent paramEvent8 = task8(paramEvent5);
        ParamEvent paramEvent9 = task9(paramEvent6, paramEvent7, paramEvent8);
        task10(paramEvent4, paramEvent9);
    }

    @Task(version = 1, retries = 0, timeout = 1000L)
    public ParamEvent task1() {
        return new ParamEvent("task1");
    }

    @Task(version = 1, retries = 0, timeout = 1000L)
    public ParamEvent task2(ParamEvent event) {
        // logic which decides whether to cancel the path
        if (event.data.length() == 5) {
            throw new FluxCancelPathException();
        }
        return new ParamEvent(event.data + "_task2");
    }

    @Task(version = 1, retries = 0, timeout = 1000L)
    public ParamEvent task3(ParamEvent event) {
        // logic which decides whether to cancel the path
        if (event.data.length() < 5) {
            throw new FluxCancelPathException();
        }
        return new ParamEvent(event.data + "_task3");
    }

    @Task(version = 1, retries = 0, timeout = 1000L)
    public ParamEvent task4(ParamEvent event, ParamEvent event1) {
        // This task is dependant on output of task2 and task3. But only one of the paths is executed, and other gets cancelled due to the above if conditions
        // The event from cancelled path would be null, so do a null check
        return new ParamEvent((event != null ? event.data : "") + (event1 != null ? event1.data : "") + "_task4");
    }

    @Task(version = 1, retries = 0, timeout = 1000L)
    public ParamEvent task5(ParamEvent event) {
        return new ParamEvent(event.data + "_task5");
    }

    @Task(version = 1, retries = 0, timeout = 1000L)
    public ParamEvent task6(ParamEvent event) {
        return new ParamEvent(event.data + "_task6");
    }

    @Task(version = 1, retries = 0, timeout = 1000L)
    public ParamEvent task7(ParamEvent event) {
        return new ParamEvent(event.data + "_task7");
    }

    @Task(version = 1, retries = 0, timeout = 1000L)
    public ParamEvent task8(ParamEvent event) {
        return new ParamEvent(event.data + "_task8");
    }

    @Task(version = 1, retries = 0, timeout = 1000L)
    public ParamEvent task9(ParamEvent event, ParamEvent event1, ParamEvent event2) {
        return new ParamEvent(event.data + "_" + event1.data + "_" + event2.data + "_task9");
    }

    @Task(version = 1, retries = 0, timeout = 1000L)
    public void task10(ParamEvent event, ParamEvent event1) {
        System.out.println("Executing task10. Event data " + (event != null ? event.data : "") + " " + (event1 != null ? event1.data : ""));
    }
}

class ParamEvent implements Event {

    @JsonProperty
    String data;

    public ParamEvent() {
    }

    public ParamEvent(String data) {
        this.data = data;
    }
}

class StartEvent implements Event {

    @CorrelationId @JsonProperty
    String id;

    public StartEvent() {
    }

    public StartEvent(String id) {
        this.id = id;
    }
}
