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
 *
 */

package com.flipkart.flux.integration;

import static com.flipkart.flux.resource.StateMachineResourceTest.STATE_MACHINE_RESOURCE_URL;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.flipkart.flux.FluxRuntimeRole;
import com.flipkart.flux.InjectFromRole;
import com.flipkart.flux.api.Status;
import com.flipkart.flux.client.FluxClientComponentModule;
import com.flipkart.flux.client.FluxClientInterceptorModule;
import com.flipkart.flux.client.registry.Executable;
import com.flipkart.flux.constant.RuntimeConstants;
import com.flipkart.flux.dao.ParallelScatterGatherQueryHelper;
import com.flipkart.flux.dao.iface.EventsDAO;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.dao.iface.StatesDAO;
import com.flipkart.flux.deploymentunit.iface.DeploymentUnitsManager;
import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.guice.module.AkkaModule;
import com.flipkart.flux.guice.module.ContainerModule;
import com.flipkart.flux.guice.module.ExecutionContainerModule;
import com.flipkart.flux.guice.module.ExecutionTaskModule;
import com.flipkart.flux.guice.module.OrchestrationTaskModule;
import com.flipkart.flux.guice.module.OrchestratorContainerModule;
import com.flipkart.flux.guice.module.ShardModule;
import com.flipkart.flux.initializer.ExecutionOrderedComponentBooter;
import com.flipkart.flux.initializer.OrchestrationOrderedComponentBooter;
import com.flipkart.flux.module.DeploymentUnitTestModule;
import com.flipkart.flux.module.RuntimeTestModule;
import com.flipkart.flux.redriver.dao.MessageDao;
import com.flipkart.flux.registry.TaskExecutableImpl;
import com.flipkart.flux.registry.TaskExecutableRegistryImpl;
import com.flipkart.flux.rule.DbClearRule;
import com.flipkart.flux.runner.GuiceJunit4Runner;
import com.flipkart.flux.runner.Modules;
import com.flipkart.flux.task.redriver.RedriverRegistry;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

@RunWith(GuiceJunit4Runner.class)
@Modules(orchestrationModules = {FluxClientComponentModule.class, ShardModule.class,
    RuntimeTestModule.class, ContainerModule.class,
    OrchestrationTaskModule.class, OrchestratorContainerModule.class, FluxClientInterceptorModule.class},
    executionModules = {FluxClientComponentModule.class, DeploymentUnitTestModule.class,
        AkkaModule.class, ExecutionTaskModule.class, ExecutionContainerModule.class,ContainerModule.class,
        FluxClientInterceptorModule.class})
public class E2ETest {

  @Rule
  @InjectFromRole(value = FluxRuntimeRole.ORCHESTRATION)
  public DbClearRule dbClearRule;
  @InjectFromRole(value = FluxRuntimeRole.ORCHESTRATION)
  StateMachinesDAO stateMachinesDAO;
  @InjectFromRole(value = FluxRuntimeRole.ORCHESTRATION)
  ParallelScatterGatherQueryHelper parallelScatterGatherQueryHelper;
  @InjectFromRole(value = FluxRuntimeRole.ORCHESTRATION)
  EventsDAO eventsDAO;
  @InjectFromRole(value = FluxRuntimeRole.ORCHESTRATION)
  StatesDAO statesDAO;
  @InjectFromRole(value = FluxRuntimeRole.ORCHESTRATION)
  SimpleWorkflow simpleWorkflow;
  @InjectFromRole(value = FluxRuntimeRole.ORCHESTRATION)
  OrchestrationOrderedComponentBooter orchestrationOrderedComponentBooter;
  @InjectFromRole(value = FluxRuntimeRole.ORCHESTRATION)
  TestCancelPathWorkflow testCancelPathWorkflow;
  @InjectFromRole(value = FluxRuntimeRole.ORCHESTRATION)
  TestReplayEventTriggerWorkflow testReplayEventTriggerWorkflow;
  /**
   * Needed to populate deployment units before beginning the test
   */
  @InjectFromRole(value = FluxRuntimeRole.EXECUTION)
  DeploymentUnitsManager deploymentUnitManager;
  @InjectFromRole(value = FluxRuntimeRole.EXECUTION)
  TaskExecutableRegistryImpl registry;
  @InjectFromRole(value = FluxRuntimeRole.EXECUTION)
  ExecutionOrderedComponentBooter executionOrderedComponentBooter;
  @InjectFromRole(value = FluxRuntimeRole.ORCHESTRATION)
  RedriverRegistry redriverRegistry;
  @InjectFromRole(value = FluxRuntimeRole.ORCHESTRATION)
  MessageDao messageDao;

  @Before
  public void setUp() {
    try {
      Unirest.post("http://localhost:9998/api/client-elb/create")
          .queryString("clientId", "defaultElbId").queryString("clientElbUrl",
          "http://localhost:9997").asString();
    } catch (UnirestException e) {
      e.printStackTrace();
    }
  }

  @After
  public void tearDown() {
    try {
      Unirest.post("http://localhost:9998/api/client-elb/delete")
          .queryString("clientId", "defaultElbId").asString();
    } catch (UnirestException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testSimpleWorkflowE2E() throws Exception {
    /* Invocation */
    simpleWorkflow.simpleDummyWorkflow(new StringEvent("startingEvent"));
    // sleep for a while to let things complete and then eval results and shutdown
    Thread.sleep(2000L);

    /* Asserts*/
    final Set<StateMachine> smInDb = parallelScatterGatherQueryHelper
        .findStateMachinesByNameAndVersion(
            "com.flipkart.flux.integration.SimpleWorkflow_simpleDummyWorkflow_void_com.flipkart.flux.integration.StringEvent_version1",
            1l);
    final String smId = smInDb.stream().findFirst().get().getId();
    assertThat(smInDb).hasSize(1);
    assertThat(eventsDAO.findBySMInstanceId(smId)).hasSize(3);

    /** All the events should be in triggered state after execution*/
    assertThat(eventsDAO.findTriggeredOrCancelledEventsNamesBySMId(smId)).hasSize(3);
  }

  @Test
  public void testCancelPathWorkflowE2E() throws Exception {
    /* Invocation */
    testCancelPathWorkflow.create(new StartEvent("test_cancel_path"));
    // sleep for a while to let things complete and then eval results and shutdown
    Thread.sleep(6000L);

    /* Asserts*/
    final Set<StateMachine> smInDb = parallelScatterGatherQueryHelper
        .findStateMachinesByNameAndVersion(
            "com.flipkart.flux.integration.TestCancelPathWorkflow_create_void_com.flipkart.flux.integration.StartEvent_version1",
            1l);
    final String smId = smInDb.stream().findFirst().get().getId();
    assertThat(smInDb).hasSize(1);
    assertThat(eventsDAO.findBySMInstanceId(smId)).hasSize(9);

    /* Tests the propagation of FluxCancelPathException via event ParamEvent2 */
    String eventName = "com.flipkart.flux.integration.ParamEvent2";
    assertThat(
        eventsDAO.findValidEventsByStateMachineIdAndExecutionVersionAndName(smId, eventName, 0L)
            .getStatus().toString().equalsIgnoreCase("cancelled"));

    /* Triggered events coming from States which do not throw FluxCancelPathException */
    assertThat(eventsDAO.findTriggeredEventsBySMId(smId)).hasSize(3);

    /** All the events should be in triggered or cancelled state after execution*/
    assertThat(eventsDAO.findTriggeredOrCancelledEventsNamesBySMId(smId)).hasSize(9);
  }

  @Test
  public void testExecConcurrencyValueOfTask() {
    Executable executable = registry.getTask(
        "com.flipkart.flux.integration.SimpleWorkflow_simpleStringReturningTask_com.flipkart.flux.integration.StringEvent_com.flipkart.flux.integration.StringEvent_version1");
    assertThat(executable).isInstanceOf(TaskExecutableImpl.class);
    TaskExecutableImpl taskExecutable = (TaskExecutableImpl) executable;
    assertThat(taskExecutable.getExecutionConcurrency()).isEqualTo(5);
  }

  @SuppressWarnings("unused")
  @Test
  public void verifyRedriverPolling() {
    dbClearRule.explicitClearTables();
    for (int i = 0; i < 100; i++) {
      redriverRegistry.registerTask(i * 1L, "smId", 0, 0l);
    }
    for (int i = 1; i <= 100; i++) {
      redriverRegistry.registerTask(1000L + i, "smId", (i * 10000000L), 0l);
    }
    Long total = messageDao.redriverCount();
    try {
      Thread.sleep(12000);
    } catch (InterruptedException ex) {
    }
    assertThat(messageDao.redriverCount()).isEqualTo(100L);
    dbClearRule.explicitClearTables();
  }

  @Test
  public void testReplayEventTriggerWorkflowE2E() throws Exception {
    /* Invocation */
    String smId = "test_replay_event_trigger";
    testReplayEventTriggerWorkflow.create(new StartEvent(smId));
    // sleep for a while to let things complete and then eval results and shutdown
    Thread.sleep(2000L);

    StateMachine stateMachine = stateMachinesDAO.findById(smId);


    /* Assert for default values and ReplayEvent entries in Datastore */
    assertThat(stateMachine.getExecutionVersion()).isEqualTo(0);
    assertThat(eventsDAO.findBySMInstanceId(smId)).hasSize(11);
    assertThat(eventsDAO.findValidReplayEventBySMIdAndName(smId, "RE1").isPresent()).isTrue();
    assertThat(eventsDAO.findValidReplayEventBySMIdAndName(smId, "RE2").isPresent()).isTrue();

    // Trigger ReplayEvent RE1
    String replayEventJson = IOUtils
        .toString(this.getClass().getClassLoader().getResourceAsStream("replay_event_data.json"), "UTF-8");
    Unirest.post(
        STATE_MACHINE_RESOURCE_URL + "/" + smId + "/context/replayevent")
        .header("Content-Type", "application/json").body(replayEventJson).asString();

    /* Wait for redriver to pick replayable state and then let the things complete with that execution version.
     * Replayable state are redrived by redriver, thread sleep in between
     * is set to 15 secs for each RE trigger because redriver batch read
     * interval is 2.5 secs and further sleep of 12.5 secs is for all states
     * to get completed (7 states, each with timeout of 0.5 secs).
     */
    Thread.sleep(15000L);

    stateMachine = stateMachinesDAO.findById(smId);
    /* Assertions */
    assertThat(stateMachine.getExecutionVersion()).isEqualTo(1);
    assertThat(eventsDAO.findBySMInstanceId(smId)).hasSize(11);
    assertThat(eventsDAO.findValidReplayEventBySMIdAndName(smId, "RE1").get().getEventData())
        .isEqualTo("42");
    assertThat(eventsDAO.findValidReplayEventBySMIdAndName(smId, "RE1").get().getEventSource())
        .contains(RuntimeConstants.REPLAY_EVENT);

    // Assertions for execution Version of all events
    assertThat(eventsDAO.findValidEventBySMIdAndName(
        smId, "com.flipkart.flux.integration.StartEvent0").getExecutionVersion()).isEqualTo(0);
    assertThat(eventsDAO.findValidEventBySMIdAndName(
        smId, "com.flipkart.flux.integration.IntegerEvent1").getExecutionVersion()).isEqualTo(0);
    assertThat(eventsDAO.findValidEventBySMIdAndName(
        smId, "com.flipkart.flux.integration.IntegerEvent2").getExecutionVersion()).isEqualTo(0);
    assertThat(eventsDAO.findValidEventBySMIdAndName(
        smId, "com.flipkart.flux.integration.IntegerEvent3").getExecutionVersion()).isEqualTo(1);
    assertThat(eventsDAO.findValidEventBySMIdAndName(
        smId, "com.flipkart.flux.integration.IntegerEvent4").getExecutionVersion()).isEqualTo(1);
    assertThat(eventsDAO.findValidEventBySMIdAndName(
        smId, "com.flipkart.flux.integration.IntegerEvent5").getExecutionVersion()).isEqualTo(1);
    assertThat(eventsDAO.findValidEventBySMIdAndName(
        smId, "com.flipkart.flux.integration.IntegerEvent6").getExecutionVersion()).isEqualTo(1);
    assertThat(eventsDAO.findValidEventBySMIdAndName(
        smId, "com.flipkart.flux.integration.IntegerEvent7").getExecutionVersion()).isEqualTo(0);
    assertThat(eventsDAO.findValidEventBySMIdAndName(
        smId, "com.flipkart.flux.integration.IntegerEvent8").getExecutionVersion()).isEqualTo(1);
    assertThat(eventsDAO.findValidEventBySMIdAndName(
        smId, "RE1").getExecutionVersion()).isEqualTo(1);
    assertThat(eventsDAO.findValidEventBySMIdAndName(
        smId, "RE2").getExecutionVersion()).isEqualTo(0);

    /* Assert for executionVersion of all state after RE1 replayEvent is triggered. Only states in it's
     * traversal path should have executionVersion '1'. Also all states should be completed */
    for (State state : stateMachine.getStates()) {
      switch (state.getName()) {
        case "t1":
          assertThat(state.getExecutionVersion()).isEqualTo(0);
          assertThat(state.getStatus().toString()).isEqualTo(Status.completed.toString());
          break;
        case "t2":
          assertThat(state.getExecutionVersion()).isEqualTo(0);
          assertThat(state.getStatus().toString()).isEqualTo(Status.completed.toString());
          break;
        case "t3":
          assertThat(state.getExecutionVersion()).isEqualTo(1);
          assertThat(state.getStatus().toString()).isEqualTo(Status.completed.toString());
          break;
        case "t4":
          assertThat(state.getExecutionVersion()).isEqualTo(1);
          assertThat(state.getStatus().toString()).isEqualTo(Status.completed.toString());
          break;
        case "t5":
          assertThat(state.getExecutionVersion()).isEqualTo(1);
          assertThat(state.getStatus().toString()).isEqualTo(Status.completed.toString());
          break;
        case "t6":
          assertThat(state.getExecutionVersion()).isEqualTo(1);
          assertThat(state.getStatus().toString()).isEqualTo(Status.completed.toString());
          break;
        case "t7":
          assertThat(state.getExecutionVersion()).isEqualTo(1);
          assertThat(state.getStatus().toString()).isEqualTo(Status.completed.toString());
          break;
        case "t8":
          assertThat(state.getExecutionVersion()).isEqualTo(0);
          assertThat(state.getStatus().toString()).isEqualTo(Status.completed.toString());
          break;
        case "t9":
          assertThat(state.getExecutionVersion()).isEqualTo(1);
          assertThat(state.getStatus().toString()).isEqualTo(Status.completed.toString());
          break;
        case "t10":
          assertThat(state.getExecutionVersion()).isEqualTo(1);
          assertThat(state.getStatus().toString()).isEqualTo(Status.completed.toString());
          break;
      }
    }

    // Trigger ReplayEvent RE2
    String replayEvent2_Json = IOUtils
        .toString(this.getClass().getClassLoader().getResourceAsStream("replay_event_data_2.json"), "UTF-8");
    Unirest.post(
        STATE_MACHINE_RESOURCE_URL + "/" + smId + "/context/replayevent")
        .header("Content-Type", "application/json").body(replayEvent2_Json).asString();

    /* Wait for redriver to pick replayable state and then let the things complete with that execution version. */
    Thread.sleep(12000L);

    stateMachine = stateMachinesDAO.findById(smId);
    /* Assertions */
    assertThat(stateMachine.getExecutionVersion()).isEqualTo(2);
    assertThat(eventsDAO.findBySMInstanceId(smId)).hasSize(11);
    assertThat(eventsDAO.findValidReplayEventBySMIdAndName(smId, "RE2").get().getEventData())
        .isEqualTo("50");
    assertThat(eventsDAO.findValidReplayEventBySMIdAndName(smId, "RE2").get().getEventSource())
        .contains(RuntimeConstants.REPLAY_EVENT);

    // Assertions for execution Version of all events
    assertThat(eventsDAO.findValidEventBySMIdAndName(
        smId, "com.flipkart.flux.integration.StartEvent0").getExecutionVersion()).isEqualTo(0);
    assertThat(eventsDAO.findValidEventBySMIdAndName(
        smId, "com.flipkart.flux.integration.IntegerEvent1").getExecutionVersion()).isEqualTo(0);
    assertThat(eventsDAO.findValidEventBySMIdAndName(
        smId, "com.flipkart.flux.integration.IntegerEvent2").getExecutionVersion()).isEqualTo(0);
    assertThat(eventsDAO.findValidEventBySMIdAndName(
        smId, "com.flipkart.flux.integration.IntegerEvent3").getExecutionVersion()).isEqualTo(1);
    assertThat(eventsDAO.findValidEventBySMIdAndName(
        smId, "com.flipkart.flux.integration.IntegerEvent4").getExecutionVersion()).isEqualTo(1);
    assertThat(eventsDAO.findValidEventBySMIdAndName(
        smId, "com.flipkart.flux.integration.IntegerEvent5").getExecutionVersion()).isEqualTo(1);
    assertThat(eventsDAO.findValidEventBySMIdAndName(
        smId, "com.flipkart.flux.integration.IntegerEvent6").getExecutionVersion()).isEqualTo(1);
    assertThat(eventsDAO.findValidEventBySMIdAndName(
        smId, "com.flipkart.flux.integration.IntegerEvent7").getExecutionVersion()).isEqualTo(0);
    assertThat(eventsDAO.findValidEventBySMIdAndName(
        smId, "com.flipkart.flux.integration.IntegerEvent8").getExecutionVersion()).isEqualTo(2);
    assertThat(eventsDAO.findValidEventBySMIdAndName(
        smId, "RE1").getExecutionVersion()).isEqualTo(1);
    assertThat(eventsDAO.findValidEventBySMIdAndName(
        smId, "RE2").getExecutionVersion()).isEqualTo(2);

    /* Assert for executionVersion of all state after RE2 replayEvent is triggered. Only states in it's
     * traversal path should have executionVersion '2'. Also all states should be completed */
    for (State state : stateMachine.getStates()) {
      switch (state.getName()) {
        case "t1":
          assertThat(state.getExecutionVersion()).isEqualTo(0);
          assertThat(state.getStatus().toString()).isEqualTo(Status.completed.toString());
          break;
        case "t2":
          assertThat(state.getExecutionVersion()).isEqualTo(0);
          assertThat(state.getStatus().toString()).isEqualTo(Status.completed.toString());
          break;
        case "t3":
          assertThat(state.getExecutionVersion()).isEqualTo(1);
          assertThat(state.getStatus().toString()).isEqualTo(Status.completed.toString());
          break;
        case "t4":
          assertThat(state.getExecutionVersion()).isEqualTo(1);
          assertThat(state.getStatus().toString()).isEqualTo(Status.completed.toString());
          break;
        case "t5":
          assertThat(state.getExecutionVersion()).isEqualTo(1);
          assertThat(state.getStatus().toString()).isEqualTo(Status.completed.toString());
          break;
        case "t6":
          assertThat(state.getExecutionVersion()).isEqualTo(1);
          assertThat(state.getStatus().toString()).isEqualTo(Status.completed.toString());
          break;
        case "t7":
          assertThat(state.getExecutionVersion()).isEqualTo(1);
          assertThat(state.getStatus().toString()).isEqualTo(Status.completed.toString());
          break;
        case "t8":
          assertThat(state.getExecutionVersion()).isEqualTo(0);
          assertThat(state.getStatus().toString()).isEqualTo(Status.completed.toString());
          break;
        case "t9":
          assertThat(state.getExecutionVersion()).isEqualTo(2);
          assertThat(state.getStatus().toString()).isEqualTo(Status.completed.toString());
          break;
        case "t10":
          assertThat(state.getExecutionVersion()).isEqualTo(2);
          assertThat(state.getStatus().toString()).isEqualTo(Status.completed.toString());
          break;
      }
    }
  }
}