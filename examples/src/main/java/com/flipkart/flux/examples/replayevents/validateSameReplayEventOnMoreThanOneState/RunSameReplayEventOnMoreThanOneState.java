package com.flipkart.flux.examples.replayevents.validateSameReplayEventOnMoreThanOneState;

import com.flipkart.flux.client.FluxClientComponentModule;
import com.flipkart.flux.client.FluxClientInterceptorModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class RunSameReplayEventOnMoreThanOneState {
    public static void main(String args[]){

        /* Initialise _your_ module*/
        final Injector injector = Guice.createInjector(new FluxClientComponentModule(), new FluxClientInterceptorModule());
        SameReplayEventOnMoreThanOneState SameReplayEventOnMoreThanOneState = injector.getInstance(SameReplayEventOnMoreThanOneState.class);
        SameReplayEventOnMoreThanOneState.create(new StartEvent("example_replay_event_workflow_on_more_than_one_state"));

    }
}