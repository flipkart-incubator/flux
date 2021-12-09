package com.flipkart.flux.examples.replayevents.validateStateWithMultipleReplayEvent;

import com.flipkart.flux.client.FluxClientComponentModule;
import com.flipkart.flux.client.FluxClientInterceptorModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class RunSampleReplayEventOnStateWithMultipleReplayEvent {

    public static void main(String args[]){

        /* Initialise _your_ module*/
        final Injector injector = Guice.createInjector(new FluxClientComponentModule(), new FluxClientInterceptorModule());
        SampleReplayEventOnStateWithMultipleReplayEvent SampleReplayEventOnStateWithMultipleReplayEvent = injector.getInstance(SampleReplayEventOnStateWithMultipleReplayEvent.class);
        SampleReplayEventOnStateWithMultipleReplayEvent.create(new StartEvent("example_multiple_replay_event_workflow_on_one_state"));

    }
}