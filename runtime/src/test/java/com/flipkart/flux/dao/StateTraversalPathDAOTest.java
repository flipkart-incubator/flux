/*
 * Copyright 2012-2019, the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flipkart.flux.dao;

import com.flipkart.flux.InjectFromRole;
import com.flipkart.flux.client.FluxClientComponentModule;
import com.flipkart.flux.client.FluxClientInterceptorModule;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.dao.iface.StateTraversalPathDAO;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.domain.StateTraversalPath;
import com.flipkart.flux.guice.module.ContainerModule;
import com.flipkart.flux.guice.module.OrchestrationTaskModule;
import com.flipkart.flux.guice.module.ShardModule;
import com.flipkart.flux.module.RuntimeTestModule;
import com.flipkart.flux.rule.DbClearWithTestSMRule;
import com.flipkart.flux.runner.GuiceJunit4Runner;
import com.flipkart.flux.runner.Modules;
import com.flipkart.flux.util.TestUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <code>StateTraversalPathDAOTest</code> class tests the functionality of {@link StateTraversalPathDAO} using JUnit tests.
 *
 * @author akif.khan
 */
@RunWith(GuiceJunit4Runner.class)
@Modules(orchestrationModules = {FluxClientComponentModule.class, ShardModule.class, RuntimeTestModule.class,
        ContainerModule.class, OrchestrationTaskModule.class, FluxClientInterceptorModule.class})
public class StateTraversalPathDAOTest {

    @InjectFromRole
    StateTraversalPathDAO stateTraversalPathDAO;

    @InjectFromRole
    @Rule
    public DbClearWithTestSMRule dbClearWithTestSMRule;

    @InjectFromRole
    StateMachinesDAO stateMachinesDAO;

    @Before
    public void setup() {
    }

    @Test
    public void createStateTraversalPathTest() {
        StateMachine stateMachine = dbClearWithTestSMRule.getStateMachineWithReplayableState();

        List<Long> nextDependentStates = new ArrayList<>();
        nextDependentStates.add(2L);
        StateTraversalPath stateTraversalPath = new StateTraversalPath(stateMachine.getId(), 1L,
                nextDependentStates);
        stateTraversalPathDAO.create(stateMachine.getId(), stateTraversalPath);

        StateTraversalPath stateTraversalPath1 = stateTraversalPathDAO.findById(stateMachine.getId(), 1L);
        assertThat(stateTraversalPath1).isEqualTo(stateTraversalPath);
    }

    @Test
    public void testRetrieveByStateMachineId() throws Exception {
        final StateMachine standardTestMachine = TestUtils.getStandardTestMachineWithReplayableState();
        stateMachinesDAO.create(standardTestMachine.getId(), standardTestMachine);

        List<Long> nextDependentStates1 = new ArrayList<>();
        nextDependentStates1.add(2L);
        nextDependentStates1.add(3L);
        final StateTraversalPath stateTraversalPath1 = new StateTraversalPath("standard-machine-replayable",
                1L, nextDependentStates1);
        stateTraversalPathDAO.create(standardTestMachine.getId(), stateTraversalPath1);

        List<Long> nextDependentStates4 = new ArrayList<>();
        nextDependentStates1.add(3L);
        final StateTraversalPath stateTraversalPath4 = new StateTraversalPath("standard-machine-replayable",
                4L, nextDependentStates4);
        stateTraversalPathDAO.create(standardTestMachine.getId(), stateTraversalPath4);

        assertThat(stateTraversalPathDAO.findByStateMachineId(standardTestMachine.getId()))
                .containsExactly(stateTraversalPath1, stateTraversalPath4);
    }

    @Test
    public void testRetrieveByStateMachineId_forNoReplayableState() throws Exception {

        assertThat(stateTraversalPathDAO.findByStateMachineId("dummy_state_machine_id")).isEmpty();
    }
}
