package com.flipkart.flux.examples.replayevents.validateStateWithMultipleReplayEvent;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flipkart.flux.client.model.CorrelationId;
import com.flipkart.flux.client.model.Event;
import com.flipkart.flux.client.model.ReplayEvent;
import com.flipkart.flux.client.model.Task;
import com.flipkart.flux.client.model.Workflow;

public class SampleReplayEventOnStateWithMultipleReplayEvent {

    @SuppressWarnings("unused")
	@Workflow(version = 1)
    public void create(StartEvent startEvent) {
        ParamEvent paramEvent1 = task1();
        ParamEvent paramEvent2 = task2(null, null, paramEvent1);
        ParamEvent paramEvent3 = task3(null, paramEvent1);
        ParamEvent paramEvent4 = task4(paramEvent2, paramEvent3);
        ParamEvent paramEvent5 = task5(paramEvent4);
    }

    @Task(version = 1, retries = 0, timeout = 1000L)
    public ParamEvent task1() {
        return new ParamEvent("task1", true);
    }

    @Task(version = 1, retries = 0, timeout = 400L, isReplayable = true, replayRetries = 3)
    public ParamEvent task2(@ReplayEvent("someReplayEvent1") ParamEvent event,
                            @ReplayEvent("someReplayEvent2") ParamEvent event1, ParamEvent event2) {
        return new ParamEvent("task2", false);
    }

    @Task(version = 1, retries = 0, timeout = 1000L, isReplayable = true, replayRetries = 10)
    public ParamEvent task3(@ReplayEvent("someReplayEvent3") ParamEvent event, ParamEvent event1) {
        return new ParamEvent("task3", false);
    }

    @Task(version = 1, retries = 0, timeout = 400L)
    public ParamEvent task4(ParamEvent event1, ParamEvent event2) {
        if (event1.failDependentTask) {
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
