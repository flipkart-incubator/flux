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

package com.flipkart.flux.rule;

import com.flipkart.flux.dao.DummyEventData;
import com.flipkart.flux.dao.DummyOnEntryHook;
import com.flipkart.flux.dao.DummyOnExitHook;
import com.flipkart.flux.dao.DummyTask;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.dao.iface.StatesDAO;
import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.StateMachine;
import org.junit.rules.ExternalResource;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.Set;

/**
 * <code>TestSMRule</code> is a Junit Rule which creates a state machine.
 * @author shyam.akirala
 */
@Singleton
public class TestSMRule extends ExternalResource {

    private final StateMachinesDAO stateMachinesDAO;

    private final StatesDAO statesDAO;

    private StateMachine stateMachine;

    @Inject
    public TestSMRule(StateMachinesDAO stateMachinesDAO, StatesDAO statesDAO) {
        this.stateMachinesDAO = stateMachinesDAO;
        this.statesDAO = statesDAO;
    }

    @Override @Transactional
    protected void before() throws Throwable {
        String onEntryHook = "com.flipkart.flux.dao.DummyOnEntryHook";
        String task = "com.flipkart.flux.dao.DummyTask";
        String onExitHook = "com.flipkart.flux.dao.DummyOnExitHook";
        State<DummyEventData> state1 = statesDAO.create(new State<>(2L, "state1", "desc1", onEntryHook, task, onExitHook, 3L, 60L));
        State<DummyEventData> state2 = statesDAO.create(new State<>(2L, "state1", "desc1", onEntryHook, task, onExitHook, 3L, 60L));
        Set<State<DummyEventData>> states = new HashSet<>();
        states.add(state1);
        states.add(state2);
        StateMachine<DummyEventData> stateMachine1 = new StateMachine<>(2L, "SM_name", "SM_desc", states);
        stateMachine = stateMachinesDAO.create(stateMachine1);
    }

    public StateMachine getStateMachine() {
        return stateMachine;
    }

}
