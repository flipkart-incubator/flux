package com.flipkart.flux.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.api.EventDefinition;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.StateMachine;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Dumping ground for all Utils Big and small to be used in tests
 */
public class TestUtils {
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static StateMachine getStandardTestMachine() throws IOException {
        String dummyOutputEvent;
        dummyOutputEvent = objectMapper.writeValueAsString(standardStateMachineOutputEvent());
        Set<String> state4Events = new HashSet<String>(){{ add("event2"); add("event3"); }};
        State state1 = new State(1l, "test_state1", "test_state_desc1", "com.flipkart.flux.dao.DummyOnEntryHook", "com.flipkart.flux.dao.TestWorkflow_DummyTask", "com.flipkart.flux.dao.DummyOnExitHook", Collections.emptySet(), 5l, 100l, null);
        State state2 = new State(1l, "test_state2", "test_state_desc2", "com.flipkart.flux.dao.TestOnEntryHook", "com.flipkart.flux.dao.TestWorkflow_TestTask_event1", "com.flipkart.flux.dao.TestOnExitHook", Collections.singleton("event1"), 3l, 100l, dummyOutputEvent);
        State state3 = new State(1l, "test_state3", "test_state_desc3", "com.flipkart.flux.dao.TestOnEntryHook", "com.flipkart.flux.dao.TestWorkflow_TestTask_event1", "com.flipkart.flux.dao.TestOnExitHook", Collections.singleton("event1"), 3l, 100l, null);
        State state4 = new State(1l, "test_state4", "test_state_desc4", "com.flipkart.flux.dao.DummyOnEntryHook", "com.flipkart.flux.dao.TestWorkflow_DummyTask_event2_event3", "com.flipkart.flux.dao.DummyOnExitHook", state4Events, 5l, 100l,null );
        Set<State> states = new HashSet<State>(){{
            add(state1);
            add(state2);
            add(state3);
            add(state4);
        }};
        return new StateMachine(1l,"test_state_machine","test_description", states);
    }

    public static EventDefinition standardStateMachineOutputEvent() {
        return new EventDefinition("event2","someType");
    }

    public static void assertStateMachineEquality(StateMachine actual, StateMachine expected) {
        assertThat(actual).isEqualToIgnoringGivenFields(expected, "states", "context", "createdAt","updatedAt","id");
        assertThat(actual.getStates()).usingElementComparatorIgnoringFields("id","createdAt","updatedAt","stateMachineId").containsOnlyElementsOf(expected.getStates());
    }

    public static Event getStandardTestEvent() throws JsonProcessingException {
        return new Event("event1", "foo", Event.EventStatus.triggered, null, objectMapper.writeValueAsString(Collections.singletonMap("key", "value")), "test_state1");
    }
}
