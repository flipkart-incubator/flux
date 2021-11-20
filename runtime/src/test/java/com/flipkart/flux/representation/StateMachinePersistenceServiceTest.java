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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.api.EventData;
import com.flipkart.flux.api.EventDefinition;
import com.flipkart.flux.api.StateDefinition;
import com.flipkart.flux.api.StateMachineDefinition;
import com.flipkart.flux.client.FluxClientComponentModule;
import com.flipkart.flux.client.FluxClientInterceptorModule;
import com.flipkart.flux.constant.RuntimeConstants;
import com.flipkart.flux.dao.iface.AuditDAO;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.dao.iface.StateTraversalPathDAO;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.domain.Status;
import com.flipkart.flux.guice.module.ContainerModule;
import com.flipkart.flux.guice.module.OrchestrationTaskModule;
import com.flipkart.flux.guice.module.ShardModule;
import com.flipkart.flux.module.RuntimeTestModule;
import com.flipkart.flux.runner.Modules;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author shyam.akirala
 */
@RunWith(MockitoJUnitRunner.class)
@Modules(orchestrationModules = {FluxClientComponentModule.class, ShardModule.class, RuntimeTestModule.class, ContainerModule.class,
        OrchestrationTaskModule.class, FluxClientInterceptorModule.class})
public class StateMachinePersistenceServiceTest {

    @Mock
    StateMachinesDAO stateMachinesDAO;

    @Mock
    AuditDAO auditDAO;

    @Mock
    StateTraversalPathDAO stateTraversalPathDAO;

    @Mock
    EventPersistenceService eventPersistenceService;

    @Mock
    ObjectMapper objectMapper;

    @Before
    public void setup() {
        objectMapper = new ObjectMapper();
    }


    @Test
    public void maxTaskRetryCountShouldBeTakenIfRetryCountIsHigher() {
        Integer maxTaskRetryCount = 10;
        StateMachinePersistenceService stateMachinePersistenceService = new StateMachinePersistenceService(
                stateMachinesDAO, auditDAO, stateTraversalPathDAO, eventPersistenceService, maxTaskRetryCount);
        StateDefinition stateDefinition = new StateDefinition(1L, "state1", "desc",
                null, "task1", null, 13L, 1000L, Collections.emptyList(),
                null);
        StateMachineDefinition stateMachineDefinition = new StateMachineDefinition("desc",
                "state_machine_1",
                1L, Collections.singleton(stateDefinition), null, null,
                "client_elb_id_1");
        stateMachinePersistenceService.createStateMachine("sample-state-machine-id", stateMachineDefinition);
        State state = new State(1L, "state1", "desc", null, "task1",
                null, Collections.emptyList(), 10L, 1000L, null,
                Status.initialized, null, 0L, "sample-state-machine-id",
                1L);
        verify(stateMachinesDAO).create("sample-state-machine-id", new StateMachine(
                "sample-state-machine-id", 1L, "state_machine_1", "desc",
                Collections.singleton(state), "client_elb_id_1"));
    }

    @Test
    public void retryCountShouldBeTakenIfItIsLessthanMaxAllowed() {
        Integer maxTaskRetryCount = 10;
        StateMachinePersistenceService stateMachinePersistenceService = new StateMachinePersistenceService(
                stateMachinesDAO, auditDAO, stateTraversalPathDAO, eventPersistenceService, maxTaskRetryCount);
        StateDefinition stateDefinition = new StateDefinition(1L, "state1", "desc",
                null, "task1", null, 3L, 1000L, Collections.emptyList(),
                null);
        StateMachineDefinition stateMachineDefinition = new StateMachineDefinition("desc",
                "state_machine_1",
                1L, Collections.singleton(stateDefinition), null, null,
                "client_elb_id_1");
        stateMachinePersistenceService.createStateMachine("sample-state-machine-id", stateMachineDefinition);
        State state = new State(1L, "state1", "desc", null, "task1",
                null, Collections.emptyList(), 3L, 1000L, null,
                Status.initialized, null, 0L, "sample-state-machine-id",
                1L);
        verify(stateMachinesDAO).create("sample-state-machine-id", new StateMachine(
                "sample-state-machine-id", 1L, "state_machine_1", "desc",
                Collections.singleton(state), "client_elb_id_1"));
    }

    @Test
    public void testConvertStateDefinitionToState() throws Exception {

        String stateMachineDefinitionJson = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("state_machine_definition_test.json"));
        Integer maxTaskRetryCount = 10;
        StateMachineDefinition stateMachineDefinition = objectMapper.readValue(stateMachineDefinitionJson, StateMachineDefinition.class);
        Event event1 = new Event("event1", "java.lang.String", Event.EventStatus.pending,
                null, null, null, 0L);
        Event event2 = new Event("event2", "java.lang.String", Event.EventStatus.pending,
                null, null, null, 0L);
        EventDefinition eventDefinition1 = new EventDefinition("event1", "java.lang.String");
        EventDefinition eventDefinition2 = new EventDefinition("event2", "java.lang.String");
        when(eventPersistenceService.convertEventDefinitionToEvent(eventDefinition1)).thenReturn(event1);
        when(eventPersistenceService.convertEventDefinitionToEvent(eventDefinition2)).thenReturn(event2);

        StateMachinePersistenceService stateMachinePersistenceService = new StateMachinePersistenceService(stateMachinesDAO, auditDAO, stateTraversalPathDAO, eventPersistenceService, maxTaskRetryCount);
        stateMachinePersistenceService.createStateMachine(stateMachineDefinition.getCorrelationId(), stateMachineDefinition);
        State state = new State(1L, "test_state2", "desc2", "com.flipkart.flux.dao.DummyOnEntryHook",
                "com.flipkart.flux.dao.TestWorkflow_testTask_java.lang.String_java.lang.String_version1", "com.flipkart.flux.dao.DummyOnExitHook",
                Collections.singletonList(event1.getName()), 3L, 100L, "{\"name\":\"event2\",\"type\":\"java.lang.String\",\"eventSource\":null}", Status.initialized, null,
                0L, "magic_number_1", 1L);
        verify(stateMachinesDAO).create("magic_number_1", new StateMachine("magic_number_1", 1L, "test_state_machine", "desc", Collections.singleton(state), "defaultElbId"));

    }

    @Test
    public void testConvertStateDefinitionToStateWithReplayable() throws Exception {

        String stateMachineDefinitionJson = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("state_machine_definition_single_replayable_state.json"));
        Integer maxTaskRetryCount = 10;
        StateMachineDefinition stateMachineDefinition = objectMapper.readValue(stateMachineDefinitionJson, StateMachineDefinition.class);
        Event event1 = new Event("event1", "java.lang.String", Event.EventStatus.pending,
                null, null, null, 0L);
        Event event2 = new Event("event2", "java.lang.String", Event.EventStatus.pending,
                null, null, null, 0L);
        EventDefinition eventDefinition1 = new EventDefinition("event1", "java.lang.String");
        EventDefinition eventDefinition2 = new EventDefinition("event2", "java.lang.String");
        when(eventPersistenceService.convertEventDefinitionToEvent(eventDefinition1)).thenReturn(event1);
        when(eventPersistenceService.convertEventDefinitionToEvent(eventDefinition2)).thenReturn(event2);

        StateMachinePersistenceService stateMachinePersistenceService = new StateMachinePersistenceService(stateMachinesDAO, auditDAO, stateTraversalPathDAO, eventPersistenceService, maxTaskRetryCount);
        stateMachinePersistenceService.createStateMachine(stateMachineDefinition.getCorrelationId(), stateMachineDefinition);
        State state = new State(1L, "test_state2", "desc2", "com.flipkart.flux.dao.DummyOnEntryHook",
                "com.flipkart.flux.dao.TestWorkflow_testTask_java.lang.String_java.lang.String_version1", "com.flipkart.flux.dao.DummyOnExitHook",
                Collections.singletonList(event1.getName()), 3L, 100L, "{\"name\":\"event2\",\"type\":\"java.lang.String\",\"eventSource\":null}", Status.initialized, null,
                0L, "magic_number_1", 1L, (short) 5, (short) 0, Boolean.TRUE);
        verify(stateMachinesDAO).create("magic_number_1", new StateMachine("magic_number_1", 1L, "test_state_machine", "desc", Collections.singleton(state), "defaultElbId"));

    }

    @Test
    public void testConvertStateDefinitionToStateWithReplayableRetriesGreaterThanThreshold() throws Exception {

        String stateMachineDefinitionJson = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("state_machine_replayableRetries_greater_than_threshold.json"));
        Integer maxTaskRetryCount = 10;
        StateMachineDefinition stateMachineDefinition = objectMapper.readValue(stateMachineDefinitionJson, StateMachineDefinition.class);
        Event event1 = new Event("event1", "java.lang.String", Event.EventStatus.pending,
                null, null, null, 0L);
        Event event2 = new Event("event2", "java.lang.String", Event.EventStatus.pending,
                null, null, null, 0L);
        EventDefinition eventDefinition1 = new EventDefinition("event1", "java.lang.String");
        EventDefinition eventDefinition2 = new EventDefinition("event2", "java.lang.String");
        when(eventPersistenceService.convertEventDefinitionToEvent(eventDefinition1)).thenReturn(event1);
        when(eventPersistenceService.convertEventDefinitionToEvent(eventDefinition2)).thenReturn(event2);

        StateMachinePersistenceService stateMachinePersistenceService
                = new StateMachinePersistenceService(stateMachinesDAO, auditDAO, stateTraversalPathDAO,
                eventPersistenceService, maxTaskRetryCount);
        StateMachine stateMachine = stateMachinePersistenceService.createStateMachine(
                stateMachineDefinition.getCorrelationId(), stateMachineDefinition);
        State replayableState = null;
        for (State state: stateMachine.getStates()) {
            if(state.getReplayable()) {
                replayableState = state;
            }
        }
        assertThat(replayableState.getMaxReplayableRetries()).isEqualTo(RuntimeConstants.MAX_REPLAYABLE_RETRIES);
    }

    /**
     *   Example workflow to test State Traversal Path creation using BFS search util.
     *   Replayable states : [t1, t4, t8, t9]
     *   StateTraversalPaths for Replayable states :
     *   t1 -> [t1, t3, t4, t5, t6, t7, t8, t9]
     *   t4 -> [t4, t6, t7, t8, t9]
     *   t8 -> [t8, t9]
     *   t9 -> [t9]
     *
     *     e0         e1           e3           e5
     *   >---->t1 ---------->t3 ------->t5 --------------------->t6
     *   |      |     > ------^                                 ^ ^
     * e0|      |     |                        e4               | |
     *   |    e1|   e2|       >---------------------------------^ |e7
     *   |      |     |       |                e4                 |
     * --       >----------->t4 -------------------------->t7 ---^
     *   |        e2  |      ^  |
     * e0|      >-----^      |  |
     *   |      |            |  |   e4            e8
     *   >----->t2 ----------   >------->t8 ---------->t9
     *      e0
     *
     * @return
     */
    //TODO: Check the state Definition constructor
    private StateMachineDefinition createStateMachineDefinitionWithReplayableStates() {

        Set<StateDefinition> stateDefinitions = new HashSet<>();
        Set<EventData> eventData = new HashSet<>();
        eventData.add(new EventData("e0", "java.lang.String", "dummy_data", "client"));
        Event e0_domain = new Event("e0", "java.lang.String", Event.EventStatus.pending,
                null, null, null, 0L);
        when(eventPersistenceService.convertEventDefinitionToEvent(new EventDefinition(
                "e0", "java.lang.String"))).thenReturn(e0_domain);

        List<EventDefinition> t1_dependencies = new ArrayList<>();
        t1_dependencies.add(new EventDefinition("e0", "java.lang.String"));
        EventDefinition t1_OutputEvent = new EventDefinition("e1", "java.lang.String");
        eventData.add(new EventData("e1", "java.lang.String", null,
                "managed_runtime"));
        Event e1_domain = new Event("e1", "java.lang.String", Event.EventStatus.pending,
                null, null, null, 0L);
        when(eventPersistenceService.convertEventDefinitionToEvent(t1_OutputEvent)).thenReturn(e1_domain);
        StateDefinition t1 = new StateDefinition(1L, "t1", "desc",
                null, "task1", null, 3L, 1000L, t1_dependencies,
                t1_OutputEvent, Boolean.TRUE);
        stateDefinitions.add(t1);

        List<EventDefinition> t2_dependencies = new ArrayList<>();
        t2_dependencies.add(new EventDefinition("e0", "java.lang.String"));
        EventDefinition t2_OutputEvent = new EventDefinition("e2", "java.lang.String");
        eventData.add(new EventData("e2", "java.lang.String", null,
                "managed_runtime"));
        Event e2_domain = new Event("e2", "java.lang.String", Event.EventStatus.pending,
                null, null, null, 0L);
        when(eventPersistenceService.convertEventDefinitionToEvent(t2_OutputEvent)).thenReturn(e2_domain);
        StateDefinition t2 = new StateDefinition(1L, "t2", "desc",
                null, "task2", null, 3L, 1000L, t2_dependencies,
                t2_OutputEvent);
        stateDefinitions.add(t2);

        List<EventDefinition> t3_dependencies = new ArrayList<>();
        t3_dependencies.add(new EventDefinition("e1", "java.lang.String"));
        t3_dependencies.add(new EventDefinition("e2", "java.lang.String"));
        EventDefinition t3_OutputEvent = new EventDefinition("e3", "java.lang.String");
        eventData.add(new EventData("e3", "java.lang.String", null,
                "managed_runtime"));
        Event e3_domain = new Event("e3", "java.lang.String", Event.EventStatus.pending,
                null, null, null, 0L);
        when(eventPersistenceService.convertEventDefinitionToEvent(t3_OutputEvent)).thenReturn(e3_domain);
        StateDefinition t3 = new StateDefinition(1L, "t3", "desc",
                null, "task3", null, 3L, 1000L, t3_dependencies,
                t3_OutputEvent);
        stateDefinitions.add(t3);

        List<EventDefinition> t4_dependencies = new ArrayList<>();
        t4_dependencies.add(new EventDefinition("e1", "java.lang.String"));
        t4_dependencies.add(new EventDefinition("e2", "java.lang.String"));
        EventDefinition t4_OutputEvent = new EventDefinition("e4", "java.lang.String");
        eventData.add(new EventData("e4", "java.lang.String", null,
                "managed_runtime"));
        Event e4_domain = new Event("e4", "java.lang.String", Event.EventStatus.pending,
                null, null, null, 0L);
        when(eventPersistenceService.convertEventDefinitionToEvent(t4_OutputEvent)).thenReturn(e4_domain);
        StateDefinition t4 = new StateDefinition(1L, "t4", "desc",
                null, "task4", null, 3L, 1000L, t4_dependencies,
                t4_OutputEvent, Boolean.TRUE);
        stateDefinitions.add(t4);

        List<EventDefinition> t5_dependencies = new ArrayList<>();
        t5_dependencies.add(new EventDefinition("e3", "java.lang.String"));
        EventDefinition t5_OutputEvent = new EventDefinition("e5", "java.lang.String");
        eventData.add(new EventData("e5", "java.lang.String", null,
                "managed_runtime"));
        Event e5_domain = new Event("e5", "java.lang.String", Event.EventStatus.pending,
                null, null, null, 0L);
        when(eventPersistenceService.convertEventDefinitionToEvent(t5_OutputEvent)).thenReturn(e5_domain);
        StateDefinition t5 = new StateDefinition(1L, "t5", "desc",
                null, "task5", null, 3L, 1000L, t5_dependencies,
                t5_OutputEvent);
        stateDefinitions.add(t5);

        List<EventDefinition> t6_dependencies = new ArrayList<>();
        t6_dependencies.add(new EventDefinition("e4", "java.lang.String"));
        t6_dependencies.add(new EventDefinition("e5", "java.lang.String"));
        t6_dependencies.add(new EventDefinition("e7", "java.lang.String"));
        EventDefinition t6_OutputEvent = new EventDefinition("e6", "java.lang.String");
        eventData.add(new EventData("e6", "java.lang.String", null,
                "managed_runtime"));
        Event e6_domain = new Event("e6", "java.lang.String", Event.EventStatus.pending,
                null, null, null, 0L);
        when(eventPersistenceService.convertEventDefinitionToEvent(t6_OutputEvent)).thenReturn(e6_domain);
        StateDefinition t6 = new StateDefinition(1L, "t6", "desc",
                null, "task6", null, 3L, 1000L, t6_dependencies,
                t6_OutputEvent);
        stateDefinitions.add(t6);

        List<EventDefinition> t7_dependencies = new ArrayList<>();
        t7_dependencies.add(new EventDefinition("e4", "java.lang.String"));
        EventDefinition t7_OutputEvent = new EventDefinition("e7", "java.lang.String");
        eventData.add(new EventData("e7", "java.lang.String", null,
                "managed_runtime"));
        Event e7_domain = new Event("e7", "java.lang.String", Event.EventStatus.pending,
                null, null, null, 0L);
        when(eventPersistenceService.convertEventDefinitionToEvent(t7_OutputEvent)).thenReturn(e7_domain);
        StateDefinition t7 = new StateDefinition(1L, "t7", "desc",
                null, "task7", null, 3L, 1000L, t7_dependencies,
                t7_OutputEvent);
        stateDefinitions.add(t7);

        List<EventDefinition> t8_dependencies = new ArrayList<>();
        t8_dependencies.add(new EventDefinition("e4", "java.lang.String"));
        EventDefinition t8_OutputEvent = new EventDefinition("e8", "java.lang.String");
        eventData.add(new EventData("e8", "java.lang.String", null,
                "managed_runtime"));
        Event e8_domain = new Event("e8", "java.lang.String", Event.EventStatus.pending,
                null, null, null, 0L);
        when(eventPersistenceService.convertEventDefinitionToEvent(t8_OutputEvent)).thenReturn(e8_domain);
        StateDefinition t8 = new StateDefinition(1L, "t8", "desc",
                null, "task8", null, 3L, 1000L, t8_dependencies,
                t8_OutputEvent, Boolean.TRUE);
        stateDefinitions.add(t8);

        List<EventDefinition> t9_dependencies = new ArrayList<>();
        t9_dependencies.add(new EventDefinition("e8", "java.lang.String"));
        StateDefinition t9 = new StateDefinition(1L, "t9", "desc",
                null, "task9", null, 3L, 1000L, t9_dependencies,
                null, Boolean.TRUE);
        stateDefinitions.add(t9);

        StateMachineDefinition stateMachineDefinition = new StateMachineDefinition("desc",
                "state_machine_2",
                1L, stateDefinitions, eventData, null,
                "client_elb_id_1");

        return stateMachineDefinition;
    }

    @Test
    public void verifyReplayStateTraversalPath() {
        Integer maxTaskRetryCount = 10;
        StateMachinePersistenceService stateMachinePersistenceService = new StateMachinePersistenceService(
                stateMachinesDAO, auditDAO, stateTraversalPathDAO, eventPersistenceService, maxTaskRetryCount);
        StateMachineDefinition stateMachineDefinition = createStateMachineDefinitionWithReplayableStates();
        StateMachine stateMachine = stateMachinePersistenceService.createStateMachine(
                "sample-state-machine-id-2", stateMachineDefinition);
        Map<Long, List<Long>> replayStateTraversalPath = stateMachinePersistenceService.createAndPersistStateTraversal(
                "sample-state-machine-id-2", stateMachine);

        assertThat(replayStateTraversalPath.size()).isEqualTo(4);
        assertThat(replayStateTraversalPath.isEmpty()).isFalse();

        for (State state : stateMachine.getStates()) {
            if(state.getReplayable()) {
                assertThat(replayStateTraversalPath).containsKey(state.getId());
                switch (state.getName()) {
                    case "t1" :
                        assertThat(replayStateTraversalPath.get(state.getId()).size()).isEqualTo(8);
                        assertThat(replayStateTraversalPath.get(state.getId()).contains(state.getName()));
                        assertThat(replayStateTraversalPath.get(state.getId()).contains("t3"));
                        assertThat(replayStateTraversalPath.get(state.getId()).contains("t4"));
                        assertThat(replayStateTraversalPath.get(state.getId()).contains("t5"));
                        assertThat(replayStateTraversalPath.get(state.getId()).contains("t6"));
                        assertThat(replayStateTraversalPath.get(state.getId()).contains("t7"));
                        assertThat(replayStateTraversalPath.get(state.getId()).contains("t8"));
                        assertThat(replayStateTraversalPath.get(state.getId()).contains("t9"));
                        break;
                    case "t4" :
                        assertThat(replayStateTraversalPath.get(state.getId()).size()).isEqualTo(5);
                        assertThat(replayStateTraversalPath.get(state.getId()).contains(state.getName()));
                        assertThat(replayStateTraversalPath.get(state.getId()).contains("t6"));
                        assertThat(replayStateTraversalPath.get(state.getId()).contains("t7"));
                        assertThat(replayStateTraversalPath.get(state.getId()).contains("t8"));
                        assertThat(replayStateTraversalPath.get(state.getId()).contains("t9"));
                        break;
                    case "t8" :
                        assertThat(replayStateTraversalPath.get(state.getId()).size()).isEqualTo(2);
                        assertThat(replayStateTraversalPath.get(state.getId()).contains(state.getName()));
                        assertThat(replayStateTraversalPath.get(state.getId()).contains("t9"));
                        break;
                    case "t9" :
                        assertThat(replayStateTraversalPath.get(state.getId()).size()).isEqualTo(1);
                        assertThat(replayStateTraversalPath.get(state.getId()).contains(state.getName()));
                        break;
                }
            }
        }
    }

    @Test
    public void validateMultipleStatesWithSameReplayEvent() throws IOException {
        Integer maxTaskRetryCount = 10;
        StateMachinePersistenceService stateMachinePersistenceService = new StateMachinePersistenceService(
                stateMachinesDAO, auditDAO, stateTraversalPathDAO, eventPersistenceService, maxTaskRetryCount);
        String stateMachineDefinitionJson = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(
                "state_machine_definition_5.json"));
        StateMachineDefinition stateMachineDefinition = objectMapper.readValue(stateMachineDefinitionJson, StateMachineDefinition.class);
        StateMachineDefinition stateMachineDefinition1 = stateMachinePersistenceService.validateMultipleStatesWithSameReplayEvent(stateMachineDefinition);
        Set<StateDefinition> states = stateMachineDefinition1.getStates();
        states.forEach(state -> {
            if (state.getName().equals("test_state2") || state.getName().equals("test_state3"))
                assertThat(state.isReplayable()).isEqualTo(false);
        });
    }

    @Test
    public void testValidateStateWithMultipleReplayEvent() throws IOException {
        Integer maxTaskRetryCount = 10;
        StateMachinePersistenceService stateMachinePersistenceService = new StateMachinePersistenceService(
                stateMachinesDAO, auditDAO, stateTraversalPathDAO, eventPersistenceService, maxTaskRetryCount);
        String stateMachineDefinitionJson = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("state_machine_definition_4.json"));
        StateMachineDefinition stateMachineDefinition = objectMapper.readValue(stateMachineDefinitionJson, StateMachineDefinition.class);
        StateMachineDefinition stateMachineDefinition1 = stateMachinePersistenceService.validateStateWithMultipleReplayEvent(stateMachineDefinition);
        Set<StateDefinition> states = stateMachineDefinition1.getStates();
        states.forEach(state -> {
            if (state.getName().equals("test_state2"))
                assertThat(state.isReplayable()).isEqualTo(false);
        });
    }

}