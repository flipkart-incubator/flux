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

import com.flipkart.flux.api.EventDefinition;
import com.flipkart.flux.api.StateDefinition;
import com.flipkart.flux.api.StateMachineDefinition;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.dao.iface.StatesDAO;
import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.representation.StateMachinePersistenceService;
import com.flipkart.flux.rules.DbClearRule;
import com.flipkart.flux.runner.GuiceJunit4Runner;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

/**
 * <code>StateMachinesDAOTest</code> class tests the functionality of {@link StateMachinesDAO} using JUnit tests.
 * @author shyam.akirala
 * @author kartik.bommepally
 */
@RunWith(GuiceJunit4Runner.class)
public class StateMachinesDAOTest {

    @Inject
    @Rule
    public DbClearRule dbClearRule;

    @Inject
    StateMachinesDAO stateMachinesDAO;

    @Inject
    StatesDAO statesDAO;

    @Inject
    StateMachinePersistenceService<DummyEventData> stateMachinePersistenceService;

    @Before
    public void setup() {}

    @Test
    public void createSMTest() {
        DummyTask<DummyEventData> task = new DummyTask<DummyEventData>();
        DummyOnEntryHook<DummyEventData> onEntryHook = new DummyOnEntryHook<DummyEventData>();
        DummyOnExitHook<DummyEventData> onExitHook = new DummyOnExitHook<DummyEventData>();
        State<DummyEventData> state1 = new State<DummyEventData>(2L, "state1__", "desc1", onEntryHook, task, onExitHook, 3L, 60L);
        State<DummyEventData> state2 = new State<DummyEventData>(2L, "state2__", "desc2", null, task, null, 2L, 50L);
        Set<State<DummyEventData>> states = new HashSet<>();
        states.add(state1);
        states.add(state2);
        StateMachine<DummyEventData> stateMachine = new StateMachine<>(2L, "SM_name_", "SM_desc_", states);
        String savedSMId = stateMachinesDAO.create(stateMachine).getId();

        StateMachine stateMachine1 = stateMachinesDAO.findById(savedSMId);
        Assert.assertEquals(stateMachine, stateMachine1);
    }

    /** Test creation of State machine from state machine definition */
    @Test
    public void createSMDTest() {
        EventDefinition eventDefinition1 = new EventDefinition("event1");
        EventDefinition eventDefinition2 = new EventDefinition("event2");
        Set<EventDefinition> eventSet1 = new HashSet<>();
        eventSet1.add(eventDefinition1);
        Set<EventDefinition> eventSet2 = new HashSet<>();
        eventSet2.add(eventDefinition2);
        StateDefinition stateDefinition1 = new StateDefinition(1L, "state1_", "desc1_", "com.flipkart.flux.dao.DummyOnEntryHook", "com.flipkart.flux.dao.DummyTask", "com.flipkart.flux.dao.DummyOnExitHook", 0L, 60L, eventSet1);
        StateDefinition stateDefinition2 = new StateDefinition(1L, "state2_", "desc2_", "com.flipkart.flux.dao.DummyOnEntryHook", "com.flipkart.flux.dao.DummyTask", "com.flipkart.flux.dao.DummyOnExitHook", 0L, 60L, eventSet2);
        Set<StateDefinition> stateSet = new HashSet<>();
        stateSet.add(stateDefinition1);
        stateSet.add(stateDefinition2);
        StateMachineDefinition stateMachineDefinition = new StateMachineDefinition(1L, "SMD_name_", "SMD_desc_", stateSet);
        StateMachine stateMachine = stateMachinePersistenceService.createStateMachine(stateMachineDefinition);
        StateMachine stateMachine1 = stateMachinesDAO.findById(stateMachine.getId());
        Assert.assertEquals(stateMachine, stateMachine1);
    }
}