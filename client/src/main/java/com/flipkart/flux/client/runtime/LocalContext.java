/*
 * Copyright 2012-2016, the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.flipkart.flux.client.runtime;

import com.flipkart.flux.api.*;
import com.flipkart.flux.client.intercept.IllegalInvocationException;
import com.flipkart.flux.client.model.Event;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.*;
import java.util.function.Function;

/**
 * Maintains all local flux related context
 * @author yogesh.nachnani
 */
public class LocalContext {
    private ThreadLocal<StateMachineDefinition> stateMachineDefinition;
    private ThreadLocal<MutableInt> tlUniqueEventCount;
    private ThreadLocal<IdentityHashMap<Event,String>> eventNames;

    public LocalContext() {
        this(new ThreadLocal<>(), new ThreadLocal<>(), new ThreadLocal<>());
    }

    LocalContext(ThreadLocal<StateMachineDefinition> stateMachineDefinition, ThreadLocal<MutableInt> tlUniqueEventCount, ThreadLocal<IdentityHashMap<Event, String>> eventNames) {
        this.stateMachineDefinition = stateMachineDefinition;
        this.tlUniqueEventCount = tlUniqueEventCount;
        this.eventNames = eventNames;
    }

    /**
     * Creates a new, local StateMachineDefinition instance
     * @return
     * @param workflowIdentifier
     * @param version
     * @param description
     */
    public void registerNew(String workflowIdentifier, long version, String description,String correlationId,
                            String clientElbId) {
        if (this.stateMachineDefinition.get() != null) {
            /* This ensures we don't compose workflows within workflows */
            throw new IllegalStateException("A single thread cannot execute more than one workflow");
        }
        stateMachineDefinition.set(new StateMachineDefinition(description,workflowIdentifier, version, new HashSet<>(),
                new HashSet<>(), new HashSet<>(),correlationId, clientElbId));
        tlUniqueEventCount.set(new MutableInt(0));
        this.eventNames.set(new IdentityHashMap<>());
    }

    public void registerNewState(Long version,
                                 String name, String description,
                                 String hookIdentifier, String taskIdentifier,
                                 Long retryCount, Long timeout,
                                 List<EventMetaDataDefinition> dependencySet, EventMetaDataDefinition outputEvent
    ) {
        /*final StateDefinition stateDefinition = new StateDefinition(version, name, description,
            hookIdentifier, taskIdentifier, hookIdentifier,
            retryCount, timeout, dependencySet, outputEvent);*/
        final StateTransitionDefinition stateTransitionDefinition=new StateTransitionDefinition(smId, id, 1, dependencySet, status, True,
                attemptedRetries, outputEvent, createdAt, updatedAt );
        final StateMetaDataDefinition stateMetaDataDefinition=new StateMetaDataDefinition(smId, id, 1, description, task, dependencySet,
                hookIdentifier, hookIdentifier, retryCount, timeout, outputEvent, createdAt);
        this.stateMachineDefinition.get().addStateMetaData(stateMetaDataDefinition);
        this.stateMachineDefinition.get().addStateTransition(stateTransitionDefinition);
    }

    /**
     * Resets the LocalContext so that it is ready to work on the next request
     */
    public void reset() {
        this.stateMachineDefinition.remove();
        this.tlUniqueEventCount.remove();
        this.eventNames.remove();
    }

    /**
     * Returns the state machine definition created for the current thread.
     * Ideally, we should prevent any modifications to the state machine definition after this method is called.
     * TODO Will implement safety features later
     * @return Thread local state machine definition
     */
    public StateMachineDefinition getStateMachineDef() {
        return this.stateMachineDefinition.get();
    }

    /**
     * This is used to determine if the LocalContext had been called before to register a new Workflow (which would
     * happen as part of Workflow interception). If the current thread has not been called by the <code>WorkflowInterceptor</code>
     * then it is being called by the client runtime to execute actual user code.
     * @return
     */
    public boolean isWorkflowInterception() {
        return this.getStateMachineDef() != null;
    }

    public void addEvents(EventData ...events) {
        this.stateMachineDefinition.get().addEventDatas(events);
    }

    public String generateEventName(Event event) {
        final IdentityHashMap<Event, String> eventNamesMap = this.eventNames.get();
        if (!eventNamesMap.containsKey(event)) {
            eventNamesMap.put(event, generateName(event));
        }
        return eventNamesMap.get(event);
    }

    private String generateName(Event event) {
        final int currentEventNumber = this.tlUniqueEventCount.get().intValue();
        this.tlUniqueEventCount.get().increment();
        return event.name() + currentEventNumber;
    }

    /**
     * Checks if the given definition already exists as part of the current state machine.
     * Also, throws an <code>IllegalInvocationException</code> when it encounters that the given definition's
     * name is already used by another definition with a different type
     */
    public EventDefinition checkExistingDefinition(final EventDefinition givenDefinition) {
        /* This may seem like an expensive operation, we can optimise if necessary */
        final StateMachineDefinition stateMachineDefinition = this.stateMachineDefinition.get();
        Set<EventDefinition> allDefinitions = new HashSet<>();
        stateMachineDefinition.getStates().stream().map(new Function<StateDefinition, Collection<EventDefinition>>() {
            @Override
            public Collection<EventDefinition> apply(StateDefinition stateDefinition) {
                return stateDefinition.getDependencies();
            }
        }).forEach(allDefinitions::addAll);
        final Optional<EventDefinition> searchResult =
            allDefinitions.stream().filter(eventDefinition -> givenDefinition.getName().equals(eventDefinition.getName())).findFirst();
        if (!searchResult.isPresent()) {
            return null;
        }
        final EventDefinition eventDefinitionWithMatchingName = searchResult.get();
        if (eventDefinitionWithMatchingName.getType().equals(givenDefinition.getType())) {
            return eventDefinitionWithMatchingName;
        }
        throw new IllegalInvocationException("Cannot invoke two parameters with the same name & different types!");
    }
}
