package com.flipkart.flux.client.intercept;

import com.flipkart.flux.api.EventDefinition;
import com.flipkart.flux.api.StateMachineDefinition;
import com.flipkart.flux.client.guice.annotation.IsolatedEnv;
import com.flipkart.flux.client.registry.ExecutableRegistry;
import com.flipkart.flux.client.runner.GuiceJunit4Runner;
import com.flipkart.flux.client.runtime.FluxRuntimeConnector;
import com.flipkart.flux.client.runtime.LocalContext;
import com.flipkart.flux.client.utils.TestHttpServer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import javax.inject.Inject;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;

/***
 * Checks for end to end flow of a workflow with replay event
 */
@RunWith(GuiceJunit4Runner.class)
public class E2EWorkflowReplayTest {

    @Inject
    SimpleWorkflowForReplayTest simpleWorkflowForReplayTest;

    @Inject
    LocalContext localContext;

    @Inject
    FluxRuntimeConnector fluxRuntimeConnector;

    @Inject
    @Rule
    public TestHttpServer testHttpServer;

    @Inject
    public DummyFluxRuntimeResource dummyFluxRuntimeResource;

    @Inject @IsolatedEnv
    ExecutableRegistry executableRegistry;


    @Test
    public void testSubmissionOfWorkflowWithReplayEvents() throws Exception {
        simpleWorkflowForReplayTest.simpleDummyWorkflowWithReplayEvent(new SimpleWorkflowForReplayTest.IntegerEvent(2));
        final StateMachineDefinition submittedDefinition = dummyFluxRuntimeResource.smToCountMap.keySet().stream().findFirst().get();
        assertThat(submittedDefinition.getStates()).hasSize(7);
        final Stream<String> eventDefNames = submittedDefinition.getStates().stream().
                flatMap(stateDefinition -> stateDefinition.getDependencies().stream()).map(EventDefinition::getName);
        assertThat(eventDefNames.distinct().toArray()).containsOnlyOnce(SimpleWorkflowForReplayTest.INTEGER_REPLAY_EVENT_NAME + "0",
                "someReplayEvent", "com.flipkart.flux.client.intercept.SimpleWorkflowForReplayTest$StringEvent1",
                "com.flipkart.flux.client.intercept.SimpleWorkflowForReplayTest$StringEvent2",
                "com.flipkart.flux.client.intercept.SimpleWorkflowForReplayTest$IntegerEvent3",
                "com.flipkart.flux.client.intercept.SimpleWorkflowForReplayTest$StringEvent4");
    }
}