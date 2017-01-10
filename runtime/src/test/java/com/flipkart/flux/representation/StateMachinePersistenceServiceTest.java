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

package com.flipkart.flux.representation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.api.StateDefinition;
import com.flipkart.flux.api.StateMachineDefinition;
import com.flipkart.flux.dao.iface.AuditDAO;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.domain.Status;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.Mockito.verify;

/**
 * @author shyam.akirala
 */
@RunWith(MockitoJUnitRunner.class)
public class StateMachinePersistenceServiceTest {

    @Mock
    StateMachinesDAO stateMachinesDAO;

    @Mock
    AuditDAO auditDAO;

    @Mock
    EventPersistenceService eventPersistenceService;

    @Mock
    ObjectMapper objectMapper;

    @Test
    public void maxTaskRetryCountShouldBeTakenIfRetryCountIsHigher() {
        Integer maxTaskRetryCount = 10;
        StateMachinePersistenceService stateMachinePersistenceService = new StateMachinePersistenceService(stateMachinesDAO, auditDAO, eventPersistenceService, maxTaskRetryCount);
        StateDefinition stateDefinition = new StateDefinition(1L, "state1", "desc", null, "task1", null, 13L, 1000L, Collections.emptyList(), null);
        StateMachineDefinition stateMachineDefinition = new StateMachineDefinition("desc", "state_machine_1", 1L, Collections.singleton(stateDefinition), null, null);
        stateMachinePersistenceService.createStateMachine(stateMachineDefinition);
        State state = new State(1L, "state1", "desc", null, "task1", null, Collections.emptyList(), 10L, 1000L, null, Status.initialized, null, 0L);
        verify(stateMachinesDAO).create(new StateMachine(1L, "state_machine_1", "desc", Collections.singleton(state), null));
    }

    @Test
    public void retryCountShouldBeTakenIfItIsLessthanMaxAllowed() {
        Integer maxTaskRetryCount = 10;
        StateMachinePersistenceService stateMachinePersistenceService = new StateMachinePersistenceService(stateMachinesDAO, auditDAO, eventPersistenceService, maxTaskRetryCount);
        StateDefinition stateDefinition = new StateDefinition(1L, "state1", "desc", null, "task1", null, 3L, 1000L, Collections.emptyList(), null);
        StateMachineDefinition stateMachineDefinition = new StateMachineDefinition("desc", "state_machine_1", 1L, Collections.singleton(stateDefinition), null, null);
        stateMachinePersistenceService.createStateMachine(stateMachineDefinition);
        State state = new State(1L, "state1", "desc", null, "task1", null, Collections.emptyList(), 3L, 1000L, null, Status.initialized, null, 0L);
        verify(stateMachinesDAO).create(new StateMachine(1L, "state_machine_1", "desc", Collections.singleton(state), null));
    }
}