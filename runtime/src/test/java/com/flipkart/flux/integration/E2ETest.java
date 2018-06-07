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

import com.flipkart.flux.FluxRole;
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
import com.flipkart.flux.registry.TaskExecutableImpl;
import com.flipkart.flux.registry.TaskExecutableRegistryImpl;
import com.flipkart.flux.rule.DbClearRule;
import com.flipkart.flux.runner.GuiceJunit4Runner;
import com.flipkart.flux.runner.Modules;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(GuiceJunit4Runner.class)
@Modules(orchestrationModules = {ShardModule.class, RuntimeTestModule.class, ContainerModule.class,
        OrchestrationTaskModule.class, FluxClientInterceptorModule.class},
        executionModules = { DeploymentUnitTestModule.class, AkkaModule.class, ExecutionTaskModule.class, ExecutionContainerModule.class, FluxClientInterceptorModule.class})
public class E2ETest {

    @InjectFromRole(value = FluxRole.ORCHESTRATION)
    StateMachinesDAO stateMachinesDAO;

    @InjectFromRole(value = FluxRole.ORCHESTRATION)
    ParallelScatterGatherQueryHelper parallelScatterGatherQueryHelper;

    @InjectFromRole(value = FluxRole.ORCHESTRATION)
    EventsDAO eventsDAO;

    @Rule
    @InjectFromRole(value = FluxRole.ORCHESTRATION)
    public DbClearRule dbClearRule;

    @InjectFromRole(value = FluxRole.ORCHESTRATION)
    SimpleWorkflow simpleWorkflow;

    @InjectFromRole(value = FluxRole.ORCHESTRATION)
    OrchestrationOrderedComponentBooter orchestrationOrderedComponentBooter;

    /**
     * Needed to populate deployment units before beginning the test
     */
    @InjectFromRole(value = FluxRole.EXECUTION)
    DeploymentUnitsManager deploymentUnitManager;

    @InjectFromRole(value = FluxRole.EXECUTION)
    TaskExecutableRegistryImpl registry;

    @InjectFromRole(value = FluxRole.EXECUTION)
    ExecutionOrderedComponentBooter executionOrderedComponentBooter;

    @Before
    public void setUp() {
        try {
            Unirest.post("http://localhost:9998/api/clientelb/create")
                    .queryString("clientId", "client_elb_id_1").queryString("clientElbUrl",
                    "http://localhost:9997").asString();
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() {
        try {
            Unirest.post("http://localhost:9998/api/clientelb/delete")
                    .queryString("clientId", "client_elb_id_1").asString();
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
        final Set<StateMachine> smInDb = parallelScatterGatherQueryHelper.findStateMachinesByNameAndVersion("com.flipkart.flux.integration.SimpleWorkflow_simpleDummyWorkflow_void_com.flipkart.flux.integration.StringEvent_version1", 1l);
        final String smId = smInDb.stream().findFirst().get().getId();
        assertThat(smInDb).hasSize(1);
        assertThat(eventsDAO.findBySMInstanceId(smId)).hasSize(3);

        /** All the events should be in triggered state after execution*/
        assertThat(eventsDAO.findTriggeredOrCancelledEventsNamesBySMId(smId)).hasSize(3);
    }

    @Test
    public void testExecConcurrencyValueOfTask() {
        Executable executable = registry.getTask("com.flipkart.flux.integration.SimpleWorkflow_simpleStringReturningTask_com.flipkart.flux.integration.StringEvent_com.flipkart.flux.integration.StringEvent_version1");

        assertThat(executable).isInstanceOf(TaskExecutableImpl.class);
        TaskExecutableImpl taskExecutable = (TaskExecutableImpl) executable;
        assertThat(taskExecutable.getExecutionConcurrency()).isEqualTo(5);
    }
}
