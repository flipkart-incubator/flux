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

import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.dao.iface.StatesDAO;
import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.guice.module.ConfigModule;
import com.flipkart.flux.guice.module.HibernateModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.Set;

/**
 * <code>StateMachinesDAOTest</code> class tests the functionality of {@link StateMachinesDAO} using JUnit tests.
 * @author shyam.akirala
 * @author kartik.bommepally
 */
public class StateMachinesDAOTest {

    private Injector injector;

    @Before
    public void setup() {
        injector = Guice.createInjector(new ConfigModule(), new HibernateModule());
    }

    @Test
    @Transactional
    public void createSMTest() {
        StateMachinesDAO stateMachinesDAO = injector.getInstance(StateMachinesDAO.class);
        StatesDAO statesDAO = injector.getInstance(StatesDAO.class);
        DummyTask task = new DummyTask();
        DummyOnEntryHook onEntryHook = new DummyOnEntryHook();
        DummyOnExitHook onExitHook = new DummyOnExitHook();
        State<DummyEventData> state1 = statesDAO.create(new State<DummyEventData>(2L, "state1", "desc1", onEntryHook, task, onExitHook, 3L, 60L));
        State<DummyEventData> state2 = statesDAO.create(new State<DummyEventData>(2L, "state2", "desc2", null, null, null, 2L, 50L));
        Set<State<DummyEventData>> states = new HashSet<State<DummyEventData>>();
        states.add(state1);
        states.add(state2);
        StateMachine<DummyEventData> stateMachine = new StateMachine<DummyEventData>(2L, "test_name", "test_desc", states);
        Long savedSMId = stateMachinesDAO.create(stateMachine).getId();

        StateMachine stateMachine1 = stateMachinesDAO.findById(savedSMId);
        Assert.assertNotNull(stateMachine1);
    }
}