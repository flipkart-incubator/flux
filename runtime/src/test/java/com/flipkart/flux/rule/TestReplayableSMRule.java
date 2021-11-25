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

package com.flipkart.flux.rule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.api.EventDefinition;
import com.flipkart.flux.dao.iface.EventsDAO;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.Event.EventStatus;
import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.domain.Status;
import org.junit.rules.ExternalResource;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.flipkart.flux.constant.RuntimeConstants.MAX_REPLAYABLE_RETRIES;

/**
 * <code>TestReplayableSMRule</code> is a Junit Rule which creates a state machine with replayable state
 * @author akif.khan
 */
@Singleton
public class TestReplayableSMRule extends ExternalResource {

    private final StateMachinesDAO stateMachinesDAO;

    private final EventsDAO eventsDAO;

    private StateMachine stateMachine1, stateMachine2;

    private ObjectMapper objectMapper;

    @Inject
    public TestReplayableSMRule(StateMachinesDAO stateMachinesDAO, EventsDAO eventsDAO) {
        this.stateMachinesDAO = stateMachinesDAO;
        this.eventsDAO = eventsDAO;
        objectMapper = new ObjectMapper();
    }

    @Override
    protected void before() throws Throwable {
        StateMachine stateMachine_1 = getStateMachine1();
        stateMachine1 = stateMachinesDAO.create(stateMachine_1.getId(), stateMachine_1);

        StateMachine stateMachine_2 = getStateMachine2();
        stateMachine2 = stateMachinesDAO.create(stateMachine_2.getId(), stateMachine_2);
        stateMachine2CreateAllEvents(stateMachine2.getId());

    }

    private void stateMachine2CreateAllEvents(String stateMachineId) {
        Event e0 = new Event("e0", "dummyType", EventStatus.triggered, stateMachineId,
                "e0_Data","client");
        eventsDAO.create(stateMachineId, e0);

        Event e1 = new Event("e1", "dummyType", EventStatus.triggered, stateMachineId,
                "e1_Data","managedRuntime");
        eventsDAO.create(stateMachineId, e1);

        Event e2 = new Event("e2", "dummyType", EventStatus.triggered, stateMachineId,
                "e2_Data","managedRuntime");
        eventsDAO.create(stateMachineId, e2);

        Event e3 = new Event("e3", "dummyType", EventStatus.triggered, stateMachineId,
                "e3_Data","managedRuntime");
        eventsDAO.create(stateMachineId, e3);

        Event e4 = new Event("e4", "dummyType", EventStatus.triggered, stateMachineId,
                "e4_Data","managedRuntime");
        eventsDAO.create(stateMachineId, e4);

        Event re1 = new Event("re1", "replayEvent", EventStatus.pending, stateMachineId,
                null,null, 10L);
        eventsDAO.create(stateMachineId, re1);

        Event re2 = new Event("re2", "replayEvent", EventStatus.pending, stateMachineId,
                null,null);
        eventsDAO.create(stateMachineId, re2);
    }

    private StateMachine getStateMachine1() {
        String onEntryHook = "com.flipkart.flux.dao.DummyOnEntryHook";
        String task = "com.flipkart.flux.dao.TestWorkflow_dummyTask";
        String onExitHook = "com.flipkart.flux.dao.DummyOnExitHook";
        State state1 = new State(2L, "state1", "desc1", onEntryHook, task, onExitHook,
                null, 3L, 60L, "event1", null, null,
                0l, "2", 1L, Boolean.TRUE);
        List<String> dependencies = new ArrayList<>();
        dependencies.add("event1");
        State state2 = new State(2L, "state2", "desc2", onEntryHook, task, onExitHook,
                dependencies, 3L, 60L, null, null, null,
                0l, "2", 2L);
        Set<State> states = new HashSet<>();
        states.add(state1);
        states.add(state2);
        StateMachine stateMachine = new StateMachine("2", 2L, "SM_name", "SM_desc", states,
                "client_elb_id_1");
        return stateMachine;
    }

    private StateMachine getStateMachine2() throws IOException {
        String onEntryHook = "com.flipkart.flux.dao.DummyOnEntryHook";
        String task = "com.flipkart.flux.dao.TestWorkflow_dummyTask";
        String onExitHook = "com.flipkart.flux.dao.DummyOnExitHook";

        List<String> deps_t1 = new ArrayList<>();
        deps_t1.add("e0");
        String oe1 = objectMapper.writeValueAsString(new EventDefinition("e1", "dummyType"));
        State t1 = new State(2L, "t1", "desc1", onEntryHook, task, onExitHook,
                deps_t1, 3L, 60L, oe1, Status.completed, null,
                0l, "id2", 1L, Boolean.FALSE);

        List<String> deps_t2 = new ArrayList<>();
        deps_t2.add("e1");
        deps_t2.add("re1");
        String oe2 = objectMapper.writeValueAsString(new EventDefinition("e2", "dummyType"));
        State t2 = new State(2L, "t2", "desc2", onEntryHook, task, onExitHook,
                deps_t2, 3L, 60L, oe2, Status.completed, null,
                0l, "id2", 2L, MAX_REPLAYABLE_RETRIES,
                (short)0, Boolean.TRUE, 0L);

        List<String> deps_t3 = new ArrayList<>();
        deps_t3.add("e1");
        deps_t3.add("re2");
        String oe3 = objectMapper.writeValueAsString(new EventDefinition("e3", "dummyType"));
        State t3 = new State(2L, "t3", "desc2", onEntryHook, task, onExitHook,
                deps_t3, 3L, 60L, oe3, Status.completed, null,
                0l, "id2", 3L, MAX_REPLAYABLE_RETRIES,
                (short)0, Boolean.TRUE, 0L);

        List<String> deps_t4 = new ArrayList<>();
        deps_t4.add("e2");
        String oe4 = objectMapper.writeValueAsString(new EventDefinition("e4", "dummyType"));
        State t4 = new State(2L, "t4", "desc1", onEntryHook, task, onExitHook,
                deps_t4, 3L, 60L, oe4, Status.completed, null,
                0l, "id2", 4L, Boolean.FALSE);

        List<String> deps_t5 = new ArrayList<>();
        deps_t5.add("e3");
        deps_t5.add("e4");
        State t5 = new State(2L, "t5", "desc1", onEntryHook, task, onExitHook,
                deps_t5, 3L, 60L, null, Status.completed, null,
                0l, "id2", 5L, Boolean.FALSE);

        List<String> deps_t6 = new ArrayList<>();
        deps_t6.add("e3");
        State t6 = new State(2L, "t6", "desc1", onEntryHook, task, onExitHook,
                deps_t6, 3L, 60L, null, Status.completed, null,
                0l, "id2", 6L, Boolean.FALSE);

        Set<State> states = new HashSet<>();
        states.add(t1);
        states.add(t2);
        states.add(t3);
        states.add(t4);
        states.add(t5);
        states.add(t6);
        StateMachine stateMachine = new StateMachine("id2", 2L, "SM_name", "SM_desc", states,
                "client_elb_id_1");
        return stateMachine;
    }

    public StateMachine getStateMachineWithReplayableState() {
        return stateMachine1;
    }

    public StateMachine getStateMachine2WithReplayableState() {
        return stateMachine2;
    }
}
