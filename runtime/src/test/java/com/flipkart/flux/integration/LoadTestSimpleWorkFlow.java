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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.flipkart.flux.FluxRuntimeRole;
import com.flipkart.flux.InjectFromRole;
import com.flipkart.flux.client.FluxClientComponentModule;
import com.flipkart.flux.client.FluxClientInterceptorModule;
import com.flipkart.flux.deploymentunit.iface.DeploymentUnitsManager;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.guice.module.AkkaModule;
import com.flipkart.flux.guice.module.ConfigModule;
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
import com.flipkart.flux.persistence.dao.iface.EventsDAO;
import com.flipkart.flux.persistence.dao.iface.StateMachinesDAO;
import com.flipkart.flux.persistence.dao.iface.StatesDAO;
import com.flipkart.flux.persistence.dao.impl.ParallelScatterGatherQueryHelper;
import com.flipkart.flux.redriver.dao.MessageDao;
import com.flipkart.flux.registry.TaskExecutableRegistryImpl;
import com.flipkart.flux.rule.DbClearRule;
import com.flipkart.flux.runner.GuiceJunit4Runner;
import com.flipkart.flux.runner.Modules;
import com.flipkart.flux.task.redriver.RedriverRegistry;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

@RunWith(GuiceJunit4Runner.class)
@Modules(orchestrationModules = {ConfigModule.class, FluxClientComponentModule.class, ShardModule.class,
    RuntimeTestModule.class, ContainerModule.class,
    OrchestrationTaskModule.class, OrchestratorContainerModule.class, FluxClientInterceptorModule.class},
    executionModules = {ConfigModule.class, FluxClientComponentModule.class, DeploymentUnitTestModule.class,
        AkkaModule.class, ExecutionTaskModule.class, ExecutionContainerModule.class,ContainerModule.class,
        FluxClientInterceptorModule.class})
public class LoadTestSimpleWorkFlow {

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
  public void loadTestSimpleWorkflowE2E() throws Exception {    
    int count = 1000;
    long start = System.currentTimeMillis();
    for(int i=0;i<count;i++) {
        simpleWorkflow.simpleDummyWorkflow(new StringEvent("startingEvent"));
    }

    /* Asserts*/
    final Set<StateMachine> smInDb = parallelScatterGatherQueryHelper
        .findStateMachinesByNameAndVersion(
            "com.flipkart.flux.integration.SimpleWorkflow_simpleDummyWorkflow_void_com.flipkart.flux.integration.StringEvent_version1",
            1l);
    assertThat(smInDb).hasSize(count);

    /** All the events should be in triggered state after execution*/
    while(true) {
	    int success = 0;
	    for(StateMachine sm : smInDb) {
	    	List<String> resp = eventsDAO.findTriggeredOrCancelledEventsNamesBySMId(sm.getId());
	    	if(resp.size()==3);{
	    		//executed.
	    		success++;
	    	}
	    }

	    if(success == count) {
	        long end = System.currentTimeMillis();
	    	System.out.println("All completed in " + (end-start)/1000 + " seconds");
	    	break;
	    }else {
	    	System.out.println("Yet to complete");
	    	Thread.sleep(1000);
	    }
    }
  }
}