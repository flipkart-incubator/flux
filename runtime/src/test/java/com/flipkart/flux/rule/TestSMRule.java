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

import com.flipkart.flux.dao.iface.StateMachinesDAO;
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

    private StateMachine stateMachine;

    @Inject
    public TestSMRule(StateMachinesDAO stateMachinesDAO) {
        this.stateMachinesDAO = stateMachinesDAO;
    }

    @Override @Transactional
    protected void before() throws Throwable {
        String onEntryHook = "com.flipkart.flux.dao.DummyOnEntryHook";
        String task = "com.flipkart.flux.dao.TestWorkflow_dummyTask";
        String onExitHook = "com.flipkart.flux.dao.DummyOnExitHook";
        State state1 = new State(2L, "state1", "desc1", onEntryHook, task, onExitHook, null, 3L, 60L, null, null, null, 0l);
        State state2 = new State(2L, "state2", "desc2", onEntryHook, task, onExitHook, null, 3L, 60L, null, null, null, 0l);
        Set<State> states = new HashSet<>();
        states.add(state1);
        states.add(state2);
        StateMachine stateMachine1 = new StateMachine("1", 2L, "SM_name", "SM_desc", states,null);
        stateMachine = stateMachinesDAO.create(stateMachine1);
    }

    public StateMachine getStateMachine() {
        return stateMachine;
    }

}
