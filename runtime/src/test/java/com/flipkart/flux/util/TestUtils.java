package com.flipkart.flux.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.api.EventDefinition;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.domain.Status;

import java.lang.reflect.Field;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Dumping ground for all Utils Big and small to be used in tests
 */
public class TestUtils {
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static StateMachine getStandardTestMachine() throws Exception {
        List<String> state3Events = new LinkedList<String>(){{ add("event2"); add("event3"); }};
        List<String> state4Events = new LinkedList<String>(){{ add("event0"); }};
        State state1 = new State(1l, "test_state1", "desc1", "com.flipkart.flux.dao.DummyOnEntryHook", "com.flipkart.flux.dao.TestWorkflow_testTask_java.lang.String_version1", "com.flipkart.flux.dao.DummyOnExitHook", Collections.emptyList(), 5l, 100l, toStr(getOutputEvent("event1", String.class)), Status.completed, null, 0l);
        State state2 = new State(1l, "test_state2", "desc2", "com.flipkart.flux.dao.DummyOnEntryHook", "com.flipkart.flux.dao.TestWorkflow_testTask_java.lang.String_java.lang.String_version1", "com.flipkart.flux.dao.DummyOnExitHook", Collections.singletonList("event1"), 3l, 100l, toStr(getOutputEvent("event2", String.class)), Status.completed, null, 0l);
        State state3 = new State(1l, "test_state3", "desc3", "com.flipkart.flux.dao.DummyOnEntryHook", "com.flipkart.flux.dao.TestWorkflow_testTask_void_java.lang.String_java.lang.Integer_version1", "com.flipkart.flux.dao.DummyOnExitHook", state3Events, 3l, 100l, null, Status.initialized, null, 0l);
        State state4 = new State(1l, "test_state4", "desc4", "com.flipkart.flux.dao.DummyOnEntryHook", "com.flipkart.flux.dao.TestWorkflow_dummyTask_java.lang.Integer_java.lang.String_version1", "com.flipkart.flux.dao.DummyOnExitHook", state4Events, 1l, 100l, toStr(getOutputEvent("event3", Integer.class)), Status.initialized, null, 0l);
        Set<State> states = new HashSet<State>(){{
            add(state1);
            add(state2);
            add(state3);
            add(state4);
        }};
        return new StateMachine(1l,"test_state_machine","desc", states, "magic_number_1");
    }

    /** Returns a dummy State machine with states which have the Id's set*/
     public static StateMachine getStandardTestMachineWithId() throws Exception {
        List<String> state3Events = new LinkedList<String>(){{ add("event2"); add("event3"); }};
         List<String> state4Events = new LinkedList<String>(){{ add("event0"); }};
        State state1 = new State(1l, "test_state1", "desc1", "com.flipkart.flux.dao.DummyOnEntryHook", "com.flipkart.flux.dao.TestWorkflow_testTask_java.lang.String_version1", "com.flipkart.flux.dao.DummyOnExitHook", Collections.emptyList(), 5l, 100l, toStr(getOutputEvent("event1", String.class)), Status.initialized, null, 0l);
        State state2 = new State(1l, "test_state2", "desc2", "com.flipkart.flux.dao.DummyOnEntryHook", "com.flipkart.flux.dao.TestWorkflow_testTask_java.lang.String_java.lang.String_version1", "com.flipkart.flux.dao.DummyOnExitHook", Collections.singletonList("event1"), 3l, 100l, toStr(getOutputEvent("event2", String.class)), Status.initialized, null, 0l);
        State state3 = new State(1l, "test_state3", "desc3", "com.flipkart.flux.dao.DummyOnEntryHook", "com.flipkart.flux.dao.TestWorkflow_testTask_void_java.lang.String_java.lang.Integer_version1", "com.flipkart.flux.dao.DummyOnExitHook", state3Events, 3l, 100l, null, Status.initialized, null, 0l);
        State state4 = new State(1l, "test_state4", "desc4", "com.flipkart.flux.dao.DummyOnEntryHook", "com.flipkart.flux.dao.TestWorkflow_dummyTask_java.lang.Integer_java.lang.String_version1", "com.flipkart.flux.dao.DummyOnExitHook", state4Events, 1l, 100l, toStr(getOutputEvent("event3", Integer.class)), Status.initialized, null, 0l);
        setProperty(state1, "id", 1L);
        setProperty(state2, "id", 2L);
        setProperty(state3, "id", 3L);
        setProperty(state4, "id", 4L);
        Set<State> states = new HashSet<State>(){{
            add(state1);
            add(state2);
            add(state3);
            add(state4);
        }};
        StateMachine stateMachine = new StateMachine(1l,"test_state_machine","desc", states, "magic_number_1");
        setProperty(stateMachine, "id", 1L);
        return stateMachine;
    }

    public static EventDefinition getOutputEvent(String name, Class clazz) {
        return new EventDefinition(name,clazz.getCanonicalName());
    }

    public static void assertStateMachineEquality(StateMachine actual, StateMachine expected) {
        assertThat(actual).isEqualToIgnoringGivenFields(expected, "states", "context", "createdAt","updatedAt","id");
        assertThat(actual.getStates()).usingElementComparatorIgnoringFields("id","createdAt","updatedAt","stateMachineId").containsOnlyElementsOf(expected.getStates());
    }

    public static Event getStandardTestEvent() throws JsonProcessingException {
        return new Event("event0", "java.lang.String", Event.EventStatus.triggered, null, "42", null);
    }

    public static String toStr(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    /** Sets an object property using reflection*/
    private static boolean setProperty(Object object, String fieldName, Object fieldValue) {
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
                if(field != null) {
                    field.setAccessible(false);
                }
            }
        }
        return false;
    }
}
