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
import com.flipkart.flux.rule.DbClearRule;
import com.flipkart.flux.runner.GuiceJunit4Runner;
import static org.assertj.core.api.Assertions.assertThat;
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

    @Before
    public void setup() {}

    @Test
    public void createSMTest() {
        String onEntryHook = "com.flipkart.flux.dao.DummyOnEntryHook";
        String task = "com.flipkart.flux.dao.DummyTask";
        String onExitHook = "com.flipkart.flux.dao.DummyOnExitHook";
        State<DummyEventData> state1 = new State<DummyEventData>(2L, "state1__", "desc1", onEntryHook, task, onExitHook, 3L, 60L);
        State<DummyEventData> state2 = new State<DummyEventData>(2L, "state2__", "desc2", null, task, null, 2L, 50L);
        Set<State<DummyEventData>> states = new HashSet<>();
        states.add(state1);
        states.add(state2);
        StateMachine<DummyEventData> stateMachine = new StateMachine<>(2L, "SM_name_", "SM_desc_", states);
        Long savedSMId = stateMachinesDAO.create(stateMachine).getId();

        StateMachine stateMachine1 = stateMachinesDAO.findById(savedSMId);
        assertThat(stateMachine).isEqualTo(stateMachine1);
    }

}