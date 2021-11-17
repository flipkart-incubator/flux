package com.flipkart.flux.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.api.EventDefinition;
import com.flipkart.flux.domain.Context;
import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.impl.RAMContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.*;

/**
 * <Code>SearchUtil</Code> This class is a util class performing breadth first search (BFS).
 * For a given state machine context, this util class checks for a path from a given initial state in a
 * state machine and returns list of all states in its traversal path.
 *
 * @author akif.khan
 */
@Singleton
public class SearchUtil {

    /**
     * ObjectMapper instance to be used for all purposes in this class
     */
    private static ObjectMapper objectMapper = new ObjectMapper();

    List<Long> traversalPathStateIds;

    private static final Logger logger = LoggerFactory.getLogger(SearchUtil.class);

    public List<Long> findStatesInTraversalPath(Context context, StateMachine stateMachine, Long initialStateId) throws RuntimeException {

        // List of stateIds occured in the traversal path of input initial state
        List<Long> traversalPathStateIds = new ArrayList<>();

        // Map to mark visitedState in BFS path search, memoization for BFS path
        Map<Long, Boolean> visitedStateIds = new HashMap<>();

        // Map to store stateId and its corresponding outputEvent
        Map<Long, String> stateOutputEvents = new HashMap<>();

        for (State state : stateMachine.getStates()) {
            try {
                stateOutputEvents.put(state.getId(), getOutputEventName(state.getOutputEvent()));
                visitedStateIds.put(state.getId(), Boolean.FALSE);
            } catch (IOException ex) {
                throw new RuntimeException("Error occurred while deserializing task outputEvent for stateMachineId: "
                        + stateMachine.getId() + " stateId: " + state.getId());
            }
        }

        for (State state : stateMachine.getStates()) {
            // verify this initial dest equal check
            if(!visitedStateIds.get(state.getId())) {
                if(initialStateId != state.getId()) {
                    visitedStateIds = searchPaths(context, initialStateId, state.getId(), visitedStateIds, stateOutputEvents);
                }
                else {
                    visitedStateIds.put(state.getId(), Boolean.TRUE);
                }
            }
        }

        for (Map.Entry<Long, Boolean> isStateVisited : visitedStateIds.entrySet()) {
            if(isStateVisited.getValue()) {
                traversalPathStateIds.add(isStateVisited.getKey());
            }
        }
        return traversalPathStateIds;
    }

    // TODO : Add description, test cases
    // TODO : Will modify to return all states in the path
    // TODO : Make this private
    // BFS to check a path between 2 states
    public Map<Long, Boolean> searchPaths(Context context, Long initialStateId,
                                          Long destinationStateId, Map<Long, Boolean> visitedStateIds,
                                          Map<Long, String> stateOutputEvents) {

        Queue<LinkedList<Long>> queueOfPaths = new LinkedList<>();
        LinkedList<Long> currentPath = new LinkedList<>();

        currentPath.add(initialStateId);
        queueOfPaths.add(currentPath);

        Set<Long> nextDependentStateIds;

        while (queueOfPaths.size() != 0) {
            currentPath = queueOfPaths.poll();
            Long lastStateId = currentPath.getLast();

            if(lastStateId.equals(destinationStateId)) {

                for (Long stateId : currentPath) {
                    visitedStateIds.put(stateId, Boolean.TRUE);
                }
                continue;
            }
            nextDependentStateIds = context.getDependentStateIds(stateOutputEvents.get(lastStateId));

            // handle null nextDependentStateIds
            for (Long dependentStateId : nextDependentStateIds) {
                if(!currentPath.contains(dependentStateId)) {
                    LinkedList<Long> newPath = new LinkedList<>();

                    newPath.addAll(currentPath);
                    newPath.add(dependentStateId);
                    queueOfPaths.add(newPath);
                }
            }
        }
        return visitedStateIds;
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