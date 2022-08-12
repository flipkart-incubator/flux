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
import static org.junit.Assert.assertNull;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.flipkart.flux.InjectFromRole;
import com.flipkart.flux.client.FluxClientComponentModule;
import com.flipkart.flux.client.FluxClientInterceptorModule;
import com.flipkart.flux.domain.AuditRecord;
import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.domain.Status;
import com.flipkart.flux.guice.module.ContainerModule;
import com.flipkart.flux.guice.module.OrchestrationTaskModule;
import com.flipkart.flux.guice.module.ShardModule;
import com.flipkart.flux.module.RuntimeTestModule;
import com.flipkart.flux.persistence.dao.iface.AuditDAO;
import com.flipkart.flux.rule.DbClearWithTestSMRule;
import com.flipkart.flux.runner.GuiceJunit4Runner;
import com.flipkart.flux.runner.Modules;

/**
 * <code>AuditDAOTest</code> class tests the functionality of {@link AuditDAO} using JUnit tests.
 *
 * @author shyam.akirala
 * @author kartik.bommepally
 */
@RunWith(GuiceJunit4Runner.class)
@Modules(orchestrationModules = {FluxClientComponentModule.class, ShardModule.class, RuntimeTestModule.class,
        ContainerModule.class, OrchestrationTaskModule.class, FluxClientInterceptorModule.class})
public class AuditDAOTest {

    @InjectFromRole
    AuditDAO auditDAO;

    @InjectFromRole
    @Rule
    public DbClearWithTestSMRule dbClearWithTestSMRule;

    @Before
    public void setup() {
    }

    @Test
    public void createAuditRecordTest() {
        StateMachine stateMachine = dbClearWithTestSMRule.getStateMachine();
        State state = null;
        for (Object ob : stateMachine.getStates()) {
            state = (State) ob;
            break;
        }
        AuditRecord auditRecord = new AuditRecord(stateMachine.getId(), (state != null) ? state.getId() : null,
                0L, Status.completed, null, null, 0L,
                null);
        Long recordId = auditDAO.create(stateMachine.getId(), auditRecord).getId();

        AuditRecord auditRecord1 = auditDAO.findById(stateMachine.getId(), recordId);
        assertThat(auditRecord1).isEqualTo(auditRecord);
    }

    @Test
    public void testCreateWithErrorMessageMoreThan1000Chars() throws Exception{
        StateMachine stateMachine = dbClearWithTestSMRule.getStateMachine();
        State state = null;
        for (Object ob : stateMachine.getStates()) {
            state = (State) ob;
            break;
        }
        String error = RandomStringUtils.randomAlphanumeric(1002);
        AuditRecord auditRecord = new AuditRecord(stateMachine.getId(), (state != null) ? state.getId() : null,
            0L, Status.completed, null, error, 0L,
            null);
        Long recordId = auditDAO.create(stateMachine.getId(), auditRecord).getId();

        AuditRecord auditRecord1 = auditDAO.findById(stateMachine.getId(), recordId);
        assertThat(auditRecord1.getErrors().length()).isEqualTo(AuditDAO.ERROR_MSG_LENGTH_IN_AUDIT);
    }

    @Test
    public void testCreateWithErrorMessageLessThan1000Chars() throws Exception{
        StateMachine stateMachine = dbClearWithTestSMRule.getStateMachine();
        State state = null;
        for (Object ob : stateMachine.getStates()) {
            state = (State) ob;
            break;
        }
        String errorMsg = RandomStringUtils.randomAlphanumeric(30);
        AuditRecord auditRecord = new AuditRecord(stateMachine.getId(), (state != null) ? state.getId() : null,
            0L, Status.completed, null, errorMsg, 0L,
            null);
        Long recordId = auditDAO.create(stateMachine.getId(), auditRecord).getId();

        AuditRecord auditRecord1 = auditDAO.findById(stateMachine.getId(), recordId);
        assertThat(auditRecord1.getErrors()).isEqualTo(errorMsg);
    }
    
    @Test
    public void enableAuditRecordNegativeTest() {
      StateMachine stateMachine = dbClearWithTestSMRule.getStateMachine();
      State state = null;
      for (Object ob : stateMachine.getStates()) {
        state = (State) ob;
        break;
      }
      AuditRecord auditRecord = new AuditRecord(stateMachine.getId(),
          (state != null) ? state.getId() : null,
          0L, Status.completed, null, null, 0L,
          null);
      auditDAO.enableAuditRecord(false);

      AuditRecord auditRecord1 = auditDAO.create(stateMachine.getId(), auditRecord);
      assertNull(auditRecord1);
      auditDAO.enableAuditRecord(true);
    }
}