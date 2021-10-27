package com.flipkart.flux.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.api.EventDefinition;
import com.flipkart.flux.api.VersionedEventData;
import com.flipkart.flux.api.core.TaskExecutionMessage;
import com.flipkart.flux.controller.WorkFlowExecutionController;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.domain.Status;
import com.flipkart.flux.impl.message.TaskAndEvents;
import com.google.inject.name.Named;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Dumping ground for all Utils Big and small to be used in tests
 */
public class TestUtils {
    @Inject
    @Named("")
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static String routerName = "someRandomRouterName";

    public static StateMachine getStandardTestMachine() throws Exception {
        String stateMachineId = "magic_number_1";
        List<String> state3Events = new LinkedList<String>() {{
            add("event2");
            add("event3");
        }};
        List<String> state4Events = new LinkedList<String>() {{
            add("event0");
        }};
        State state1 = new State(1l, "test_state1", "desc1", "com.flipkart.flux.dao.DummyOnEntryHook", "com.flipkart.flux.dao.TestWorkflow_testTask_java.lang.String_version1", "com.flipkart.flux.dao.DummyOnExitHook", Collections.emptyList(), 5l, 100l, toStr(getOutputEvent("event1", String.class)), Status.completed, null, 0l, stateMachineId, 1L);
        State state2 = new State(1l, "test_state2", "desc2", "com.flipkart.flux.dao.DummyOnEntryHook", "com.flipkart.flux.dao.TestWorkflow_testTask_java.lang.String_java.lang.String_version1", "com.flipkart.flux.dao.DummyOnExitHook", Collections.singletonList("event1"), 3l, 100l, toStr(getOutputEvent("event2", String.class)), Status.completed, null, 0l, stateMachineId, 2L);
        State state3 = new State(1l, "test_state3", "desc3", "com.flipkart.flux.dao.DummyOnEntryHook", "com.flipkart.flux.dao.TestWorkflow_testTask_void_java.lang.String_java.lang.Integer_version1", "com.flipkart.flux.dao.DummyOnExitHook", state3Events, 3l, 100l, null, Status.initialized, null, 0l, stateMachineId, 3L);
        State state4 = new State(1l, "test_state4", "desc4", "com.flipkart.flux.dao.DummyOnEntryHook", "com.flipkart.flux.dao.TestWorkflow_dummyTask_java.lang.Integer_java.lang.String_version1", "com.flipkart.flux.dao.DummyOnExitHook", state4Events, 1l, 100l, toStr(getOutputEvent("event3", Integer.class)), Status.initialized, null, 0l, stateMachineId, 4L);
        Set<State> states = new HashSet<State>() {{
            add(state1);
            add(state2);
            add(state3);
            add(state4);
        }};
        return new StateMachine(stateMachineId, 1l, "test_state_machine", "desc", states,
                "defaultElbId");
    }

    /**
     * Returns a dummy State machine with states which have the Id's set
     */
    public static StateMachine getStandardTestMachineWithId() throws Exception {
        String stateMachineId = "standard-machine";
        List<String> state3Events = new LinkedList<String>() {{
            add("event2");
            add("event3");
        }};
        List<String> state4Events = new LinkedList<String>() {{
            add("event0");
        }};
        State state1 = new State(1l, "test_state1", "desc1", "com.flipkart.flux.dao.DummyOnEntryHook", "com.flipkart.flux.dao.TestWorkflow_testTask_java.lang.String_version1", "com.flipkart.flux.dao.DummyOnExitHook", Collections.emptyList(), 5l, 100l, toStr(getOutputEvent("event1", String.class)), Status.initialized, null, 0l, stateMachineId, 1L);
        State state2 = new State(1l, "test_state2", "desc2", "com.flipkart.flux.dao.DummyOnEntryHook", "com.flipkart.flux.dao.TestWorkflow_testTask_java.lang.String_java.lang.String_version1", "com.flipkart.flux.dao.DummyOnExitHook", Collections.singletonList("event1"), 3l, 100l, toStr(getOutputEvent("event2", String.class)), Status.initialized, null, 0l, stateMachineId, 2L);
        State state3 = new State(1l, "test_state3", "desc3", "com.flipkart.flux.dao.DummyOnEntryHook", "com.flipkart.flux.dao.TestWorkflow_testTask_void_java.lang.String_java.lang.Integer_version1", "com.flipkart.flux.dao.DummyOnExitHook", state3Events, 3l, 100l, null, Status.initialized, null, 0l, stateMachineId, 3L);
        State state4 = new State(1l, "test_state4", "desc4", "com.flipkart.flux.dao.DummyOnEntryHook", "com.flipkart.flux.dao.TestWorkflow_dummyTask_java.lang.Integer_java.lang.String_version1", "com.flipkart.flux.dao.DummyOnExitHook", state4Events, 1l, 100l, toStr(getOutputEvent("event3", Integer.class)), Status.initialized, null, 0l, stateMachineId, 4L);
        setProperty(state1, "id", 1L);
        setProperty(state2, "id", 2L);
        setProperty(state3, "id", 3L);
        setProperty(state4, "id", 4L);
        Set<State> states = new HashSet<State>() {{
            add(state1);
            add(state2);
            add(state3);
            add(state4);
        }};
        StateMachine stateMachine = new StateMachine(stateMachineId, 1l, "test_state_machine",
                "desc", states, "defaultElbId");
        return stateMachine;
    }

    public static EventDefinition getOutputEvent(String name, Class clazz) {
        return new EventDefinition(name, clazz.getCanonicalName());
    }

    public static void assertStateMachineEquality(StateMachine actual, StateMachine expected) {
        assertThat(actual).isEqualToIgnoringGivenFields(expected, "states", "context", "createdAt", "updatedAt", "id");
        assertThat(actual.getStates().size()).isEqualTo(expected.getStates().size());
        actual.getStates().forEach(actualState -> {
            expected.getStates().forEach(expectedState -> {
                if (actualState.getName() == expected.getName()) {
                    assertStateEquality(actualState, expectedState);
                }
            });
        });
    }

    public static void assertStateEquality(State actual, State expected) {
        assertThat(actual).isEqualToIgnoringGivenFields(expected, "id", "createdAt", "updatedAt", "stateMachineId");
    }

    public static Event getStandardTestEvent() throws JsonProcessingException {
        return new Event("event0", "java.lang.String", Event.EventStatus.triggered, null,
                "42", null, 0L);
    }

    public static String toStr(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    /**
     * Sets an object property using reflection
     */
    public static boolean setProperty(Object object, String fieldName, Object fieldValue) {
        Class<?> clazz = object.getClass();
        while (clazz != null) {
            Field field = null;
            try {
                field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(object, fieldValue);
                return true;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass(); //if the field not found in the current class, search in it's super class
            } catch (Exception e) {
                throw new IllegalStateException(e);
            } finally {
                if (field != null) {
                    field.setAccessible(false);
                }
            }
        }
        return false;
    }

    public static TaskExecutionMessage getStandardTaskExecutionMessage() throws Exception {
        VersionedEventData[] expectedEvents = new VersionedEventData[]{new VersionedEventData("event0",
                "java.lang.String", "42", "runtime", 0L)};
        StateMachine sm = getStandardTestMachine();
        State state = sm.getStates().stream().filter((s) -> s.getId() == 4L).findFirst().orElse(null);
        TaskExecutionMessage msg = new TaskExecutionMessage();
        msg.setRouterName(WorkFlowExecutionController.getRouterName(state.getTask()));
        msg.setAkkaMessage(new TaskAndEvents(state.getName(), state.getTask(), state.getId(), expectedEvents,
                state.getStateMachineId(), "test_state_machine", state.getOutputEvent(),
                state.getRetryCount()));
        return msg;
    }
}