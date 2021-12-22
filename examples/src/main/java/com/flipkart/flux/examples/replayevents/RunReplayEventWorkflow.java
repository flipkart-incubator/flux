package com.flipkart.flux.examples.replayevents;

import com.flipkart.flux.client.FluxClientComponentModule;
import com.flipkart.flux.client.FluxClientInterceptorModule;
import com.flipkart.flux.client.runtime.FluxRuntimeConnector;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * This class can be used to run and observe the <code>ReplayEventWorkflow</code>
 * This or a similar class like this is _not_ required to be present in your actual production jar
 *
 * @author vartika.bhatia on 10/06/2019
 * @author akif.khan
 */
public class RunReplayEventWorkflow {

    public static void main(String args[]) throws InterruptedException {

        /* Initialise _your_ module*/
        final Injector injector = Guice.createInjector(new FluxClientComponentModule(), new FluxClientInterceptorModule());
        ReplayEventWorkflow replayEventWorkflow = injector.getInstance(ReplayEventWorkflow.class);
        FluxRuntimeConnector fluxHttpConnector = injector.getInstance(FluxRuntimeConnector.class);
        String correlation_id_1 = "example_replay_event_workflow_100";
        replayEventWorkflow.create(new StartEvent(correlation_id_1));

        System.out.println("[Main] Sleeping for 10 seconds before posting replay event to flux"
            + " runtime so that replayable state is completed");
        Thread.sleep(10000l); // Just a 10 second wait to ensure that the replayable state is completed
        fluxHttpConnector.submitReplayEvent("someReplayEvent1",
            new ParamEvent("task44", false), correlation_id_1, null);
        System.out.println("[Main] Posted replay event to flux runtime, the workflow should have continued");

        ReplayEventWorkflow2 replayEventWorkflow2 = injector.getInstance(ReplayEventWorkflow2.class);
        String correlation_id_2 = "example_replay_event_workflow_71";
        replayEventWorkflow2.create(new StartEvent(correlation_id_2));

        for (int i = 0; i < 6; i++) {
            System.out.println(
                "[Main] Sleeping for 10 seconds before posting replay event to flux runtime so that "
                    + "replayable state is completed");
            Thread.sleep(
                10000l); // Just a 10 second wait to ensure that the replayable state is completed
            if(i%2 == 0) {
            	fluxHttpConnector.submitReplayEvent("RE1",
                new IntegerEvent(5), correlation_id_2, "my-replay-event");
            }
            else {
                fluxHttpConnector.submitReplayEvent("RE2",
                    new IntegerEvent(5), correlation_id_2, "example");
            }
            System.out.println(
                "[Main] Posted replay event 2 to flux runtime iteration:," + i
                    + " the workflow should have continued");
        }
    }
}