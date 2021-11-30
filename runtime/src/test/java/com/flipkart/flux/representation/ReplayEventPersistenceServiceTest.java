package com.flipkart.flux.representation;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.InjectFromRole;
import com.flipkart.flux.api.EventData;
import com.flipkart.flux.api.EventDefinition;
import com.flipkart.flux.client.FluxClientComponentModule;
import com.flipkart.flux.client.FluxClientInterceptorModule;
import com.flipkart.flux.constant.RuntimeConstants;
import com.flipkart.flux.dao.iface.AuditDAO;
import com.flipkart.flux.dao.iface.EventsDAO;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.dao.iface.StatesDAO;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.Event.EventStatus;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.guice.module.ContainerModule;
import com.flipkart.flux.guice.module.OrchestrationTaskModule;
import com.flipkart.flux.guice.module.ShardModule;
import com.flipkart.flux.module.RuntimeTestModule;
import com.flipkart.flux.rule.DbClearWithTestSMRule;
import com.flipkart.flux.runner.GuiceJunit4Runner;
import com.flipkart.flux.runner.Modules;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <code>ReplayEventPersistenceServiceTest</code> class tests the functionality of {@link
 * ReplayEventPersistenceService} using JUnit tests.
 *
 * @author akif.khan
 */
@RunWith(GuiceJunit4Runner.class)
@Modules(orchestrationModules = {FluxClientComponentModule.class, ShardModule.class,
        RuntimeTestModule.class, ContainerModule.class, OrchestrationTaskModule.class,
        FluxClientInterceptorModule.class})
public class ReplayEventPersistenceServiceTest {

    private static ObjectMapper objectMapper;

    @InjectFromRole
    @Rule
    public DbClearWithTestSMRule dbClearWithTestSMRule;

    @InjectFromRole
    StateMachinesDAO stateMachinesDAO;

    @InjectFromRole
    EventsDAO eventsDAO;

    @InjectFromRole
    StatesDAO statesDAO;

    @InjectFromRole
    AuditDAO auditDAO;

    @InjectFromRole
    ReplayEventPersistenceService replayEventPersistenceService;

    @Before
    public void setup() {
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testReplayEventPersistenceAndExecutionVersionUpdate() throws Exception {

        StateMachine stateMachine = dbClearWithTestSMRule.getStateMachineWithMultipleReplayableStates();

        // Test with trigger of replayEvent "re1"
        EventData replayEventData1 = new EventData("re1", "replayEvent", "re1_EventData",
                RuntimeConstants.REPLAY_EVENT, Boolean.FALSE);

        List<Long> dependentStateIds_1 = new ArrayList<>();
        dependentStateIds_1.add(2L);
        dependentStateIds_1.add(4L);
        dependentStateIds_1.add(5L);

        List<String> dependentEvents_1 = new ArrayList<>();
        dependentEvents_1.add(objectMapper.writeValueAsString(new EventDefinition(
                "e2", "dummyType")));
        dependentEvents_1.add(objectMapper.writeValueAsString(new EventDefinition(
                "e4", "dummyType")));

        Event replayEvent1 = replayEventPersistenceService.persistAndProcessReplayEvent(stateMachine.getId(),
                replayEventData1, dependentStateIds_1, dependentEvents_1);

        assertThat(replayEvent1.getStatus()).isEqualTo(EventStatus.triggered);
        assertThat(replayEvent1.getEventSource()).contains(RuntimeConstants.REPLAY_EVENT);

        // Initialised replayEvent with executionVersion = 10. ExecutionVersion added should always be
        // incremented from State Machine ExecutionVersion which is by default 0 always.
        assertThat(replayEvent1.getExecutionVersion()).isEqualTo(1L);

        // Asserting increment in executionVersion for StateMachine instance
        assertThat(stateMachinesDAO.findById(stateMachine.getId()).getExecutionVersion()).isEqualTo(1L);

        assertThat(statesDAO.findById(stateMachine.getId(), 1L).getExecutionVersion()).isNotEqualTo(1L);
        assertThat(statesDAO.findById(stateMachine.getId(), 2L).getExecutionVersion()).isEqualTo(1L);
        assertThat(statesDAO.findById(stateMachine.getId(), 3L).getExecutionVersion()).isNotEqualTo(1L);
        assertThat(statesDAO.findById(stateMachine.getId(), 4L).getExecutionVersion()).isEqualTo(1L);
        assertThat(statesDAO.findById(stateMachine.getId(), 5L).getExecutionVersion()).isEqualTo(1L);
        assertThat(statesDAO.findById(stateMachine.getId(), 6L).getExecutionVersion()).isNotEqualTo(1L);

        // Cummulative of all Valid Events. It also tests for events in traversal path cummulative for
        // both invalid/valid events.
        assertThat(eventsDAO.findBySMInstanceId(stateMachine.getId()).size()).isEqualTo(7);
        assertThat(eventsDAO.findAllBySMIdAndName(stateMachine.getId(), "re1").size()).isEqualTo(2);
        assertThat(eventsDAO.findAllBySMIdAndName(stateMachine.getId(), "e2").size()).isEqualTo(2);
        assertThat(eventsDAO.findAllBySMIdAndName(stateMachine.getId(), "e4").size()).isEqualTo(2);

        // Test with trigger of replayEvent "re2"
        EventData replayEventData2 = new EventData("re2", "replayEvent", "re2_EventData",
                RuntimeConstants.REPLAY_EVENT, Boolean.FALSE);
        List<Long> dependentStateIds_2 = new ArrayList<>();
        dependentStateIds_2.add(3L);
        dependentStateIds_2.add(5L);
        dependentStateIds_2.add(6L);

        List<String> dependentEvents_2 = new ArrayList<>();
        dependentEvents_2.add(objectMapper.writeValueAsString(new EventDefinition(
                "e3", "dummyType")));

        Event replayEvent2 = replayEventPersistenceService.persistAndProcessReplayEvent(stateMachine.getId(),
                replayEventData2, dependentStateIds_2, dependentEvents_2);

        assertThat(replayEvent2.getStatus()).isEqualTo(EventStatus.triggered);
        assertThat(replayEvent2.getEventSource()).contains(RuntimeConstants.REPLAY_EVENT);

        assertThat(replayEvent2.getExecutionVersion()).isEqualTo(2L);

        // Asserting increment in executionVersion for StateMachine instance
        assertThat(stateMachinesDAO.findById(stateMachine.getId()).getExecutionVersion()).isEqualTo(2L);

        assertThat(statesDAO.findById(stateMachine.getId(), 1L).getExecutionVersion()).isEqualTo(0L);
        assertThat(statesDAO.findById(stateMachine.getId(), 2L).getExecutionVersion()).isEqualTo(1L);
        assertThat(statesDAO.findById(stateMachine.getId(), 3L).getExecutionVersion()).isEqualTo(2L);
        assertThat(statesDAO.findById(stateMachine.getId(), 4L).getExecutionVersion()).isEqualTo(1L);
        assertThat(statesDAO.findById(stateMachine.getId(), 5L).getExecutionVersion()).isEqualTo(2L);
        assertThat(statesDAO.findById(stateMachine.getId(), 6L).getExecutionVersion()).isEqualTo(2L);

        // Cummulative of all Valid Events. It also tests for events in traversal path cummulative for
        // both invalid/valid events.
        assertThat(eventsDAO.findBySMInstanceId(stateMachine.getId()).size()).isEqualTo(7);
        assertThat(eventsDAO.findAllBySMIdAndName(stateMachine.getId(), "re2").size()).isEqualTo(2);
        assertThat(eventsDAO.findAllBySMIdAndName(stateMachine.getId(), "e3").size()).isEqualTo(2);
    }

    @Test
    public void testConcurrentStateMachineExecutionVersionUpdate() throws Exception {
        // TODO : add Test case here
    }
}
