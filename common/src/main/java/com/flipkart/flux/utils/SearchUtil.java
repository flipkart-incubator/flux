package com.flipkart.flux.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.api.EventDefinition;
import com.flipkart.flux.domain.Context;
import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.impl.RAMContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <Code>SearchUtil</Code> This class is a util class performing breadth first search (BFS).
 * For a given state machine context, this util class checks for a path between two input states.
 *
 * @author akif.khan
 */
public class SearchUtil {

    /**
     * ObjectMapper instance to be used for all purposes in this class
     */
    private ObjectMapper objectMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchUtil.class);

    public SearchUtil() {
        objectMapper = new ObjectMapper();
    }

    public List<Long> getStatesInTraversalPath(StateMachine stateMachine, State initialState) {

        //create context and dependency graph
        Context context = new RAMContext(System.currentTimeMillis(), null, stateMachine);

        List<Long> traversalPathStateIds = new ArrayList<>();
        Set<Long> allStateIds = getAllStateIds(stateMachine.getStates());

        for (State state : stateMachine.getStates()) {
            if(pathExists(stateMachine.getStates(), context, stateMachine.getId(), initialState, state)) {
                traversalPathStateIds.add(state.getId());
            }
        }

        return traversalPathStateIds;
    }

    private Set<Long> getAllStateIds(Set<State> states) {

        Set<Long> allStateIds = new HashSet<>();
        for (State state : states) {
            allStateIds.add(state.getId());
        }
        return allStateIds;
    }

    // TODO : Add description, test cases
    // TODO : Will modify to return all states in the path
    // BFS to check a path between 2 states
    public boolean pathExists(Set<State> allStates, Context context, String stateMachineId, State initialState,
                              State destinationState) {

        Map<State, Boolean> visitedState = new HashMap<>();
        LinkedList<State> queue = new LinkedList<>();

        // initialise visited states
        allStates.forEach((state -> {
            visitedState.put(state, Boolean.FALSE);
        }));
        visitedState.put(initialState, Boolean.TRUE);
        queue.add(initialState);

        Set<State> nextDependantStates;

        while (queue.size() != 0) {
            State retrievedState = queue.poll();

            if (retrievedState.getOutputEvent() != null) {
                String outputEventName;

                try {
                    outputEventName = getOutputEventName(retrievedState.getOutputEvent());
                } catch (IOException ex) {
                    throw new RuntimeException("Error occurred while deserializing task outputEvent for stateMachineId: "
                            + stateMachineId + " stateId: " + retrievedState.getId());
                }

                nextDependantStates = context.getDependantStates(outputEventName);

                // TODO: Check for state object equals at object level, need to test.
                for (State dependantState : nextDependantStates) {
                    if (dependantState.getName().equals(destinationState.getName())) {
                        return true;
                    }

                    if (!visitedState.get(dependantState)) {
                        visitedState.put(dependantState, Boolean.TRUE);
                        queue.add(dependantState);
                    }
                }
            }
        }
        return false;
    }

    /**
     * Helper method to JSON serialize the output event
     */
    private String getOutputEventName(String outputEvent) throws java.io.IOException {
        return outputEvent != null ? objectMapper.readValue(outputEvent, EventDefinition.class).getName() : null;
    }
}