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

import com.flipkart.flux.FluxRuntimeRole;
import com.flipkart.flux.InjectFromRole;
import com.flipkart.flux.client.FluxClientInterceptorModule;
import com.flipkart.flux.client.registry.Executable;
import com.flipkart.flux.dao.ParallelScatterGatherQueryHelper;
import com.flipkart.flux.dao.iface.EventsDAO;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.deploymentunit.iface.DeploymentUnitsManager;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.guice.module.*;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(GuiceJunit4Runner.class)
@Modules(orchestrationModules = {ShardModule.class, RuntimeTestModule.class, ContainerModule.class,
        OrchestrationTaskModule.class, FluxClientInterceptorModule.class},
        executionModules = { DeploymentUnitTestModule.class, AkkaModule.class, ExecutionTaskModule.class, ExecutionContainerModule.class, FluxClientInterceptorModule.class})
public class E2ETest {

    @InjectFromRole(value = FluxRuntimeRole.ORCHESTRATION)
    StateMachinesDAO stateMachinesDAO;

    @InjectFromRole(value = FluxRuntimeRole.ORCHESTRATION)
    ParallelScatterGatherQueryHelper parallelScatterGatherQueryHelper;

    @InjectFromRole(value = FluxRuntimeRole.ORCHESTRATION)
    EventsDAO eventsDAO;

    @Rule
    @InjectFromRole(value = FluxRuntimeRole.ORCHESTRATION)
    public DbClearRule dbClearRule;

    @InjectFromRole(value = FluxRuntimeRole.ORCHESTRATION)
    SimpleWorkflow simpleWorkflow;

    @InjectFromRole(value = FluxRuntimeRole.ORCHESTRATION)
    OrchestrationOrderedComponentBooter orchestrationOrderedComponentBooter;

    @InjectFromRole(value = FluxRuntimeRole.ORCHESTRATION)
    TestCancelPathWorkflow testCancelPathWorkflow;

    /**
     * Needed to populate deployment units before beginning the test
     */
    @InjectFromRole(value = FluxRuntimeRole.EXECUTION)
    DeploymentUnitsManager deploymentUnitManager;

    @InjectFromRole(value = FluxRuntimeRole.EXECUTION)
    TaskExecutableRegistryImpl registry;

    @InjectFromRole(value = FluxRuntimeRole.EXECUTION)
    ExecutionOrderedComponentBooter executionOrderedComponentBooter;

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

    @InjectFromRole(value = FluxRuntimeRole.ORCHESTRATION)
    RedriverRegistry redriverRegistry;

    @InjectFromRole(value = FluxRuntimeRole.ORCHESTRATION)
    MessageDao messageDao;

    @Test
    public void testSimpleWorkflowE2E() throws Exception {
        /* Invocation */
        simpleWorkflow.simpleDummyWorkflow(new StringEvent("startingEvent"));
        // sleep for a while to let things complete and then eval results and shutdown
        Thread.sleep(2000L);

        /* Asserts*/
        final Set<StateMachine> smInDb = parallelScatterGatherQueryHelper.findStateMachinesByNameAndVersion("com.flipkart.flux.integration.SimpleWorkflow_simpleDummyWorkflow_void_com.flipkart.flux.integration.StringEvent_version1", 1l);
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
        Thread.sleep(2000L);

        /* Asserts*/
        final Set<StateMachine> smInDb =  parallelScatterGatherQueryHelper.findStateMachinesByNameAndVersion("com.flipkart.flux.integration.TestCancelPathWorkflow_create_void_com.flipkart.flux.integration.StartEvent_version1", 1l);
        final String smId = smInDb.stream().findFirst().get().getId();
        assertThat(smInDb).hasSize(1);
        assertThat(eventsDAO.findBySMInstanceId(smId)).hasSize(9);

        /* Tests the propagation of FluxCancelPathException via event ParamEvent2 */
        String eventName = "com.flipkart.flux.integration.ParamEvent2";
        assertThat(eventsDAO.findBySMIdAndName(smId, eventName).getStatus().toString().equalsIgnoreCase("cancelled"));

        /* Triggered events coming from States which do not throw FluxCancelPathException */
        assertThat(eventsDAO.findTriggeredEventsBySMId(smId)).hasSize(3);

        /** All the events should be in triggered or cancelled state after execution*/
        assertThat(eventsDAO.findTriggeredOrCancelledEventsNamesBySMId(smId)).hasSize(9);
    }

    @Test
    public void testExecConcurrencyValueOfTask() {
        Executable executable = registry.getTask("com.flipkart.flux.integration.SimpleWorkflow_simpleStringReturningTask_com.flipkart.flux.integration.StringEvent_com.flipkart.flux.integration.StringEvent_version1");
        assertThat(executable).isInstanceOf(TaskExecutableImpl.class);
        TaskExecutableImpl taskExecutable = (TaskExecutableImpl) executable;
        assertThat(taskExecutable.getExecutionConcurrency()).isEqualTo(5);
    }

    @Test
    public void verifyRedriverPolling(){
        dbClearRule.explicitClearTables();
        for(int i = 0 ; i < 100; i++)
            redriverRegistry.registerTask(i * 1L, "smId", 0);
        for(int i = 1; i <= 100 ; i++)
            redriverRegistry.registerTask( 1000L + i, "smId",  (i*10000000L));
        Long total  = messageDao.redriverCount();
        try {
            Thread.sleep(12000);
        }
        catch (InterruptedException ex){
        }
        assertThat(messageDao.redriverCount()).isEqualTo(100L);
        dbClearRule.explicitClearTables();
    }
}
