package com.flipkart.flux.util;

import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.StateMachine;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Dumping ground for all Utils Big and small to be used in tests
 */
public class TestUtils {
    public static StateMachine getStandardTestMachine() {
        return new StateMachine<>(1l,"test_state_machine","test_description",
            Collections.singleton(
                new State<>(1l, "test_state1", "test_state_desc1", "com.flipkart.flux.dao.DummyOnEntryHook", "com.flipkart.flux.dao.DummyTask", "com.flipkart.flux.dao.DummyOnExitHook", 5l, 100l)));
    }

    public static void assertStateMachineEquality(StateMachine actual, StateMachine expected) {
        assertThat(actual).isEqualToIgnoringGivenFields(expected, "states", "context", "createdAt","updatedAt","id");
        assertThat(actual.getStates()).usingElementComparatorIgnoringFields("id","createdAt","updatedAt","stateMachineId").containsOnlyElementsOf(expected.getStates());
    }
}
