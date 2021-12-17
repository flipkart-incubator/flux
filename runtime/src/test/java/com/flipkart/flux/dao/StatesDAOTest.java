package com.flipkart.flux.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.flipkart.flux.InjectFromRole;
import com.flipkart.flux.client.FluxClientComponentModule;
import com.flipkart.flux.client.FluxClientInterceptorModule;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.dao.iface.StatesDAO;
import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.domain.Status;
import com.flipkart.flux.guice.module.ContainerModule;
import com.flipkart.flux.guice.module.OrchestrationTaskModule;
import com.flipkart.flux.guice.module.ShardModule;
import com.flipkart.flux.module.RuntimeTestModule;
import com.flipkart.flux.rule.DbClearWithTestSMRule;
import com.flipkart.flux.runner.GuiceJunit4Runner;
import com.flipkart.flux.runner.Modules;

/**
 * <code>StatesDAOTest</code> class tests the functionality of {@link StatesDAO} using JUnit tests.
 *
 * @author vartika.bhatia
 */
@RunWith(GuiceJunit4Runner.class)
@Modules(orchestrationModules = {FluxClientComponentModule.class, ShardModule.class, RuntimeTestModule.class, ContainerModule.class,
        OrchestrationTaskModule.class, FluxClientInterceptorModule.class})

public class StatesDAOTest {

    @InjectFromRole
    @Rule
    public DbClearWithTestSMRule dbClearWithTestSMRule;

    @InjectFromRole
    StatesDAO statesDAO;

    @InjectFromRole
    StateMachinesDAO stateMachinesDAO;

    @Before
    public void setup() {
    }

    @Test
    public void testUpdateAttemptedNoOfReplayableRetries() throws Exception {
        StateMachine stateMachine = dbClearWithTestSMRule.getStateMachine();
        State state = statesDAO.findById(stateMachine.getId(), 1L);
        statesDAO.incrementReplayableRetries(stateMachine.getId(), 1L, (short) (state.getAttemptedNumOfReplayableRetries() + 1));
        statesDAO.incrementReplayableRetries(stateMachine.getId(), 1L, (short) (state.getAttemptedNumOfReplayableRetries() + 1));
        statesDAO.updateReplayableRetries(stateMachine.getId(), 1L, (short) 0);
        State state2 = statesDAO.findById(stateMachine.getId(), 1L);
        assertThat(state2.getAttemptedNumOfReplayableRetries()).isEqualTo((short) 0);
    }

    @Test
    public void testIncrementReplayableRetryCount() throws Exception {
        StateMachine stateMachine = dbClearWithTestSMRule.getStateMachine();
        State state1 = statesDAO.findById(stateMachine.getId(), 1L);
        statesDAO.incrementReplayableRetries(stateMachine.getId(), 1L, (short) (state1.getAttemptedNumOfReplayableRetries() + 1));
        State state2 = statesDAO.findById(stateMachine.getId(), 1L);
        assertThat(state1.getAttemptedNumOfReplayableRetries() + 1).isEqualTo(state2.getAttemptedNumOfReplayableRetries());
    }

    @Test
    public void testIncrementRetryCount() throws Exception {
        StateMachine stateMachine = dbClearWithTestSMRule.getStateMachine();
        State state1 = statesDAO.findById(stateMachine.getId(), 1L);
        statesDAO.incrementRetryCount(stateMachine.getId(), 1L);
        State state2 = statesDAO.findById(stateMachine.getId(), 1L);
        assertThat(state1.getAttemptedNumOfRetries() + 1).isEqualTo(state2.getAttemptedNumOfRetries());
    }

    @Test
    public void testUpdateExecutionVersion() throws Exception {
        StateMachine stateMachine = dbClearWithTestSMRule.getStateMachine();
        statesDAO.updateExecutionVersion(stateMachine.getId(), 1L, 5L);
        State state1 = statesDAO.findById(stateMachine.getId(), 1L);
        assertThat(state1.getExecutionVersion()).isEqualTo(5L);
    }

    @Test
    public void testUpdateState() throws Exception {
        StateMachine stateMachine = dbClearWithTestSMRule.getStateMachine();
        State state1 = new State(5L, "state1", "desc1", "com.flipkart.flux.dao.DummyOnEntryHook", "com.flipkart.flux.dao.TestWorkflow_dummyTask", "com.flipkart.flux.dao.DummyOnExitHook", null, 3L, 60L, null, null, null, 0l, "1", 1L);
        statesDAO.updateState(stateMachine.getId(), state1);
        State state2 = statesDAO.findById(stateMachine.getId(), 1L);
        assertThat(state2.getVersion()).isEqualTo(5L);
    }

    @Test
    public void testFindStateByDependentEvent() throws Exception {
        StateMachine stateMachine = dbClearWithTestSMRule.getStateMachine();
        State state1 = new State(2L, "state1", "desc1", "com.flipkart.flux.dao.DummyOnEntryHook", "com.flipkart.flux.dao.TestWorkflow_dummyTask", "com.flipkart.flux.dao.DummyOnExitHook", Collections.singletonList("event1"), 3L, 60L, null, null, null, 0l, "1", 1L);
        statesDAO.updateState(stateMachine.getId(), state1);
        List<State> stateList = statesDAO.findStatesByDependentEvent(stateMachine.getId(), "event1");
        assertThat(stateList.get(0).getName()).isEqualTo("state1");
        assertThat(stateList.get(0).getId()).isEqualTo(1L);

    }

    @Test
    public void testFindStateByDependentReplayEvent() throws Exception {
        StateMachine stateMachine = dbClearWithTestSMRule.getStateMachine();
        State state1 = new State(2L, "state1", "desc1", "com.flipkart.flux.dao.DummyOnEntryHook", "com.flipkart.flux.dao.TestWorkflow_dummyTask", "com.flipkart.flux.dao.DummyOnExitHook", Collections.singletonList("ReplayEvent"), 3L, 60L, null, Status.completed, null, 0l, "1", 1L, (short) 5, (short) 2, Boolean.TRUE);
        statesDAO.updateState(stateMachine.getId(), state1);
        Long stateId = statesDAO.findStateIdByEventName(stateMachine.getId(), "ReplayEvent");
        assertThat(stateId).isEqualTo(1L);
    }

    @SuppressWarnings("unused")
	@Test
    @Ignore("Need ShardId to test this test case")
    public void testFindStateByStatus() throws Exception {
        StateMachine stateMachine = dbClearWithTestSMRule.getStateMachine();
        State state1 = new State(2L, "state1", "desc1", "com.flipkart.flux.dao.DummyOnEntryHook", "com.flipkart.flux.dao.TestWorkflow_dummyTask", "com.flipkart.flux.dao.DummyOnExitHook", Collections.singletonList("ReplayEvent"), 3L, 60L, null, Status.completed, null, 0l, "1", 1L, (short) 5, (short) 2, Boolean.TRUE);
        statesDAO.updateState(stateMachine.getId(), state1);
        State state = statesDAO.findById(stateMachine.getId(), 1L);
    }

    @SuppressWarnings("unused")
    @Test
    @Ignore("Need ShardId to test this test case")
    public void testFindErroredStates() throws Exception {
        StateMachine stateMachine = dbClearWithTestSMRule.getStateMachine();
        State state1 = new State(2L, "state1", "desc1", "com.flipkart.flux.dao.DummyOnEntryHook", "com.flipkart.flux.dao.TestWorkflow_dummyTask", "com.flipkart.flux.dao.DummyOnExitHook", Collections.singletonList("ReplayEvent"), 3L, 60L, null, Status.errored, null, 0l, "1", 1L, (short) 5, (short) 2, Boolean.TRUE);
        statesDAO.updateState(stateMachine.getId(), state1);
        State state = statesDAO.findById(stateMachine.getId(), 1L);
    }

    @Test
    public void testFindAllStatesForGivenStateIds() throws Exception{
        StateMachine stateMachine = dbClearWithTestSMRule.getStateMachine();

        State state1 = statesDAO.findById(stateMachine.getId(),1L);
        State state2 = statesDAO.findById(stateMachine.getId(),2L);

        List<State> actualStates = statesDAO.findAllStatesForGivenStateIds(stateMachine.getId(),
            new ArrayList<>(Arrays.asList(1L,2L)));

        assertThat(actualStates.size() == 2).isTrue();
        assertThat(actualStates.containsAll(new ArrayList<>(Arrays.asList(state1, state2)))).isTrue();

    }

}