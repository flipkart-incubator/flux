package com.flipkart.flux.representation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.api.EventData;
import com.flipkart.flux.api.EventDefinition;
import com.flipkart.flux.dao.iface.EventsDAO;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.dao.iface.StatesDAO;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.Status;
import com.flipkart.flux.persistence.DataSourceType;
import com.flipkart.flux.persistence.SelectDataSource;
import com.flipkart.flux.persistence.SessionFactoryContext;
import com.flipkart.flux.persistence.Storage;
import com.google.gson.JsonParseException;
import com.google.inject.name.Named;
import org.hibernate.Session;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author raghavender.m
 * Used as a business layer to interpret Replay event's trigger, and perform DB operations on states and events
 * in triggered ReplayEvent's traversal path.
 */
@Singleton
public class ReplayEventPersistenceService {

    private StateMachinesDAO stateMachinesDAO;
    private EventsDAO eventsDAO;
    private StatesDAO statesDAO;
    private SessionFactoryContext sessionFactoryContext;

    /**
     * ObjectMapper instance to be used for all purposes in this class
     */
    private static ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    public ReplayEventPersistenceService(StateMachinesDAO stateMachinesDAO,
                                         EventsDAO eventsDAO, StatesDAO statesDAO,
                                         @Named("fluxSessionFactoriesContext") SessionFactoryContext sessionFactoryContext) {
        this.stateMachinesDAO = stateMachinesDAO;
        this.eventsDAO = eventsDAO;
        this.statesDAO = statesDAO;
        this.sessionFactoryContext = sessionFactoryContext;
    }

    /**
     * Persists and process triggered Replay Event in a single Transaction.
     * @param replayEventData
     * @param dependantStateIds
     * @param dependantEvents
     * @param stateMachineId
     */
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public Event persistAndProcessReplayEvent(String stateMachineId, EventData replayEventData,
                                              List<Long> dependantStateIds, List<String> dependantEvents) {

        Session session = sessionFactoryContext.getThreadLocalSession();

        // TODO : Ensure that this row is locked throughout the transaction.  Need to add tests for it.
        Long smExecutionVersion = stateMachinesDAO.findByIdForUpdate_NonTransactional(stateMachineId, session) + 1;
        stateMachinesDAO.updateExecutionVersion_NonTransactional(stateMachineId, smExecutionVersion, session);

        ArrayList<Long> stateIds = new ArrayList<>(dependantStateIds);
        statesDAO.updateStatus_NonTransactional(stateMachineId, stateIds, Status.initialized, session);
        statesDAO.updateExecutionVersion_NonTransactional(stateMachineId, stateIds, smExecutionVersion, session);

        eventsDAO.markEventsAsInvalid_NonTransactional(stateMachineId, dependantEvents, session);

        // Remove replay event. Purpose was to mark all it's previous version invalid.
        dependantEvents.remove(replayEventData.getName());

        dependantEvents.parallelStream().forEach(outputEvent -> {
            String eventName, eventType;
            try {
                eventName = getOutputEventName(outputEvent);
                eventType = getOutputEventType(outputEvent);
            } catch (IOException e) {
                throw new JsonParseException("Unable to deserialize value from datastore. Error : "+e.getMessage());
            }
            Event event = new Event(eventName, eventType, Event.EventStatus.pending,
                    stateMachineId, null, null, smExecutionVersion);
            eventsDAO.create(stateMachineId, event);
        });

        //Persist replay event
        // TODO: Use replay event source constant defined here
        Event replayEvent = new Event(replayEventData.getName(), replayEventData.getType(), Event.EventStatus.triggered,
                stateMachineId, replayEventData.getData(), replayEventData.getEventSource(), smExecutionVersion);
        return eventsDAO.create(stateMachineId, replayEvent);
    }

    /**
     * Helper method to JSON serialize the output event for output event name
     */
    private String getOutputEventName(String outputEvent) throws IOException {
        return outputEvent != null ? objectMapper.readValue(outputEvent, EventDefinition.class).getName() : null;
    }

    /**
     * Helper method to JSON serialize the output event for output event type
     */
    private String getOutputEventType(String outputEvent) throws IOException {
        return outputEvent != null ? objectMapper.readValue(outputEvent, EventDefinition.class).getType() : null;
    }
}
