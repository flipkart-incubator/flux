package com.flipkart.flux.examples.replayevents;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flipkart.flux.client.model.*;


/**
 * This class is an example to test Replay Event based on a boolean flag passed to a task an Replay Event annotation passed to event.
 * Created by vartika.bhatia on 15/06/2019.
 */
public class ReplayEventWorkflow {
	
	/**
	 * Logger instance for this class
	 */
	private static final Logger logger = LogManager.getLogger(ReplayEventWorkflow.class);
	

    @SuppressWarnings("unused")
	@Workflow(version = 1)
    public void create(StartEvent startEvent) {
        ParamEvent paramEvent1 = task1(startEvent);
        ParamEvent paramEvent2 = task2(null, paramEvent1);
        ParamEvent paramEvent3 = task3(paramEvent2);
        ParamEvent paramEvent4 = task4(paramEvent2, paramEvent3);
        ParamEvent paramEvent5 = task5(paramEvent4);
        ParamEvent paramEvent6 = task6(null, paramEvent1);
        ParamEvent paramEvent7 = task7(paramEvent4, paramEvent6);
    }

    @Task(version = 1, retries = 0, timeout = 1000L)
    public ParamEvent task1(StartEvent startEvent) { return new ParamEvent("task1", false); }

    @Task(version = 1, retries = 0, timeout = 400L, isReplayable = true, replayRetries = 3)
    public ParamEvent task2( @ReplayEvent("someReplayEvent1") ParamEvent event, ParamEvent event1) {
		logger.info("Executing replayable task task2 with Param Event : {}", event);    	
        return new ParamEvent("task2", false);
    }

    @Task(version = 1, retries = 0, timeout = 1000L)
    public ParamEvent task3(ParamEvent event) {
        return new ParamEvent("task3", false);
    }

    @Task(version = 1, retries = 0, timeout = 400L)
    public ParamEvent task4(ParamEvent event1, ParamEvent event2) {
        return new ParamEvent("task4", false);
    }

    @Task(version = 1, retries = 0, timeout = 1000L)
    public ParamEvent task5(ParamEvent event) {
        return new ParamEvent("task5", false);
    }

    @Task(version = 1, retries = 0, timeout = 1000L, isReplayable = true, replayRetries = 3)
    public ParamEvent task6( @ReplayEvent("someReplayEvent2") ParamEvent event, ParamEvent event1) {
		logger.info("Executing replayable task task6 with Param Event : {}", event);    	
        return new ParamEvent("task6", false);
    }

    @Task(version = 1, retries = 0, timeout = 1000L)
    public ParamEvent task7(ParamEvent event, ParamEvent event1) {
        return new ParamEvent("task7", false);
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