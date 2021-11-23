package com.flipkart.flux.representation;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.InjectFromRole;
import com.flipkart.flux.api.EventData;
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
    public void testReplayEventPersistence() throws Exception {

        StateMachine stateMachine = dbClearWithTestSMRule.getStateMachineWithMultipleReplayableStates();

        EventData replayEventData = new EventData("re1", "replayEvent", "re1_EventData",
                RuntimeConstants.REPLAY_EVENT, Boolean.FALSE);

        List<Long> dependentStateIds = new ArrayList<>();
        dependentStateIds.add(2L);
        dependentStateIds.add(4L);
        dependentStateIds.add(5L);

        List<String> dependentEvents = new ArrayList<>();
        dependentEvents.add("{\"name\":\"e2\",\"type\":\"dummyType\"}");
        dependentEvents.add("{\"name\":\"e4\",\"type\":\"dummyType\"}");

        Event replayEvent = replayEventPersistenceService.persistAndProcessReplayEvent(stateMachine.getId(),
                replayEventData, dependentStateIds, dependentEvents);
        assertThat(replayEvent.getStatus()).isEqualTo(EventStatus.triggered);
        // Add more assertions
    }
}
