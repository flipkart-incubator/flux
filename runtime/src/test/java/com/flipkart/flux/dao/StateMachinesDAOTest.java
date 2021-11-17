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
 */

package com.flipkart.flux.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.InjectFromRole;
import com.flipkart.flux.client.FluxClientComponentModule;
import com.flipkart.flux.client.FluxClientInterceptorModule;
import com.flipkart.flux.dao.iface.EventsDAO;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.domain.StateMachineStatus;
import com.flipkart.flux.guice.module.ContainerModule;
import com.flipkart.flux.guice.module.OrchestrationTaskModule;
import com.flipkart.flux.guice.module.ShardModule;
import com.flipkart.flux.module.RuntimeTestModule;
import com.flipkart.flux.persistence.CryptHashGenerator;
import com.flipkart.flux.rule.DbClearWithTestSMRule;
import com.flipkart.flux.runner.GuiceJunit4Runner;
import com.flipkart.flux.runner.Modules;
import com.flipkart.flux.util.TestUtils;
import org.hibernate.exception.DataException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <code>StateMachinesDAOTest</code> class tests the functionality of {@link StateMachinesDAOImpl} using JUnit tests.
 *
 * @author raghavender.m
 */
@RunWith(GuiceJunit4Runner.class)
@Modules(orchestrationModules = {FluxClientComponentModule.class, ShardModule.class, RuntimeTestModule.class, ContainerModule.class,
        OrchestrationTaskModule.class, FluxClientInterceptorModule.class})
public class StateMachinesDAOTest {

    @InjectFromRole
    private EventsDAO eventsDAO;

    @InjectFromRole
    @Rule
    public DbClearWithTestSMRule dbClearWithTestSMRule;

    @InjectFromRole
    private StateMachinesDAO stateMachinesDAO;

    private ObjectMapper objectMapper;

    private CryptHashGenerator cryptHashGenerator;

    @Before
    public void setup() {
        objectMapper = new ObjectMapper();
        cryptHashGenerator = new CryptHashGenerator();
    }

    @Test
    public void testFindById() throws Exception {
        final StateMachine standardTestMachine = TestUtils.getStandardTestMachineWithId();
        stateMachinesDAO.create(standardTestMachine.getId(), standardTestMachine);
        assertThat(stateMachinesDAO.findById(standardTestMachine.getId()).getName()).isEqualTo(standardTestMachine.getName());
        assertThat(stateMachinesDAO.findById(standardTestMachine.getId()).getId()).isEqualTo(standardTestMachine.getId());
    }

    @Test
    public void testUpdateStatus() throws Exception {
        /* Doesn't matter, but still setting it up */
        final StateMachine standardTestMachine = TestUtils.getStandardTestMachineWithId();
        stateMachinesDAO.create(standardTestMachine.getId(), standardTestMachine);
        stateMachinesDAO.updateStatus(standardTestMachine.getId(), StateMachineStatus.cancelled);

        /* Actual test */
        assertThat(stateMachinesDAO.findById(standardTestMachine.getId()).getStatus()).isEqualTo(StateMachineStatus.cancelled);
    }

    @Test
    public void testUpdateExecutionVersion() throws Exception {
        final StateMachine standardTestMachine = TestUtils.getStandardTestMachine();
        stateMachinesDAO.create(standardTestMachine.getId(), standardTestMachine);

        stateMachinesDAO.updateExecutionVersion(standardTestMachine.getId(), 2l);
        assertThat(stateMachinesDAO.findById(standardTestMachine.getId()).getExecutionVersion()).isEqualTo(2);

        stateMachinesDAO.updateExecutionVersion(standardTestMachine.getId(), 0l);
        assertThat(stateMachinesDAO.findById(standardTestMachine.getId()).getExecutionVersion()).isEqualTo(0l);
    }

    @Test(expected = DataException.class)
    public void testUpdateExecutionVersionWithInvalidInput() throws Exception {
        final StateMachine standardTestMachine = TestUtils.getStandardTestMachine();
        stateMachinesDAO.create(standardTestMachine.getId(), standardTestMachine);

        // Hibernate should throw exception
        stateMachinesDAO.updateExecutionVersion(standardTestMachine.getId(), -2l);
    }
}
