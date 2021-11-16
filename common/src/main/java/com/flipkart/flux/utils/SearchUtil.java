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
import java.util.*;

/**
 * <Code>SearchUtil</Code> This class is a util class performing breadth first search (BFS).
 * For a given state machine context, this util class checks for a path from a given input initial state in a
 * state machine and returns a list of all states in its traversal path.
 *
 * @author akif.khan
 */
public class SearchUtil {

    /**
     * ObjectMapper instance to be used for all purposes in this class
     */
    private ObjectMapper objectMapper;

    /**
     * Map to store map for stateId and its corresponding outputEvent.
     * It's required in BFS traversal to get next dependent state for a given state's outputEvent
     */
    private Map<Long, String> stateOutputEvents;

    List<Long> traversalPathStateIds;

    private static final Logger logger = LoggerFactory.getLogger(SearchUtil.class);

    public SearchUtil() {
        objectMapper = new ObjectMapper();
    }

    public List<Long> getStatesInTraversalPath(StateMachine stateMachine, Long initialStateId) throws RuntimeException {

        //create context and dependency graph < event -> dependent states >
        Context context = new RAMContext(System.currentTimeMillis(), null, stateMachine);

        buildStateToOutputEventMap(stateMachine.getId(), stateMachine.getStates());

        traversalPathStateIds = new ArrayList<>();

        for (State state : stateMachine.getStates()) {
            if(pathExists(context, initialStateId, state.getId())) {
                traversalPathStateIds.add(state.getId());
            }
        }
        return traversalPathStateIds;
    }

    private void buildStateToOutputEventMap(String stateMachineId, Set<State> states) throws RuntimeException {

        stateOutputEvents = new HashMap<>();

        for (State state : states) {
            try {
                stateOutputEvents.put(state.getId(), getOutputEventName(state.getOutputEvent()));

            } catch (IOException ex) {
                throw new RuntimeException("Error occurred while deserializing task outputEvent for stateMachineId: "
                        + stateMachineId + " stateId: " + state.getId());
            }
        }
    }


    // TODO : Add description, test cases
    // TODO : Will modify to return all states in the path
    // BFS to check a path between 2 states
    public boolean pathExists(Context context, Long initialStateId,
                              Long destinationStateId) {

        Map<Long, Boolean> visitedState = new HashMap<>();
        LinkedList<Long> queue = new LinkedList<>();

        // initialise visited states
        for (Map.Entry<Long,String> stateOutputEvent : stateOutputEvents.entrySet()) {
            visitedState.put(stateOutputEvent.getKey(), Boolean.FALSE);
        }

        visitedState.put(initialStateId, Boolean.TRUE);
        queue.add(initialStateId);

        Set<Long> nextDependentStateIds;

        while (queue.size() != 0) {
            Long retrievedStateId = queue.poll();

            if (stateOutputEvents.get(retrievedStateId) != null) {
                String outputEventName;

                outputEventName = stateOutputEvents.get(retrievedStateId);

                nextDependentStateIds = context.getDependentStateIds(outputEventName);

                // handle null nextDependentStateIds
                for (Long dependentStateId : nextDependentStateIds) {
                    if (dependentStateId.equals(destinationStateId)) {
                        return true;
                    }

                    if (!visitedState.get(dependentStateId)) {
                        visitedState.put(dependentStateId, Boolean.TRUE);
                        queue.add(dependentStateId);
                    }
                }
            }
        }
        return false;
    }

    /**
     * Helper method to JSON serialize the output event.
     * This is required because in State's outputEvent value is stored as <EventName, EventType> tuple, in such
     * cases this serializer helps to retrieve only EventName.
     */
    private String getOutputEventName(String outputEvent) throws IOException {
        return outputEvent != null ? objectMapper.readValue(outputEvent, EventDefinition.class).getName() : null;
    }
}