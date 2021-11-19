package com.flipkart.flux.representation;

import com.flipkart.flux.api.EventData;
import com.flipkart.flux.dao.iface.EventsDAO;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.dao.iface.StatesDAO;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.Status;
import com.flipkart.flux.persistence.DataSourceType;
import com.flipkart.flux.persistence.SelectDataSource;
import com.flipkart.flux.persistence.SessionFactoryContext;
import com.flipkart.flux.persistence.Storage;
import com.google.inject.name.Named;
import org.hibernate.Session;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

/**
 * @author raghavender.m
 * Used as a business layer to interpret Replay event's
 */
@Singleton
public class ReplayEventService {

    private StateMachinesDAO stateMachinesDAO;
    private EventsDAO eventsDAO;
    private StatesDAO statesDAO;
    private SessionFactoryContext sessionFactoryContext;

    @Inject
    public ReplayEventService(StateMachinesDAO stateMachinesDAO,
                              EventsDAO eventsDAO, StatesDAO statesDAO,
                              @Named("fluxSessionFactoriesContext") SessionFactoryContext sessionFactoryContext) {
        this.stateMachinesDAO = stateMachinesDAO;
        this.eventsDAO = eventsDAO;
        this.statesDAO = statesDAO;
        this.sessionFactoryContext = sessionFactoryContext;
    }

    /**
     * TODO : Rephrase this description
     * In a Transaction :
     * 1. Increment, update and read executionVersion for this State Machine.
     * 2. Mark all states and Update executionVersion for all states(marked as invalid) retrieved in step 1.
     * 3. Mark dependant events as invalid.
     * 4. Create new event entries in pending status including replay event as triggered containing event data with executionVersion
     * read in step 2.
     *
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

        // TODO : This will be updated to return updated value in same call. Need to add tests for it.
        // TODO : Ensure that this row is locked throughout the transaction
        Long smExecutionVersion = stateMachinesDAO.findById(stateMachineId).getExecutionVersion() + 1;
        stateMachinesDAO.updateExecutionVersion_NonTransactional(stateMachineId, smExecutionVersion, session);

        ArrayList<Long> stateIds = new ArrayList<>(dependantStateIds);
        statesDAO.updateStatus_NonTransactional(stateMachineId, stateIds, Status.initialized, session);
        statesDAO.updateExecutionVersion_NonTransactional(stateMachineId, stateIds, smExecutionVersion, session);

        eventsDAO.markEventsAsInvalid_NonTransactional(stateMachineId, dependantEvents, session);

        // Remove replay event. Purpose was to mark all it's previous version invalid.
        dependantEvents.remove(replayEventData.getName());

        dependantEvents.parallelStream().forEach(eventName -> {
            // TODO : No need to deserialise outputEvent earlier, it should be used here to replace this query
            // To retrieve event meta data (type) for creating new event with new smExecutionVersion
            // Retrieving for executionVersion 0 is fine because type of event doesn't change with executionVersion
            Event currentEvent = eventsDAO.findValidEventsByStateMachineIdAndExecutionVersionAndName(stateMachineId,
                    eventName, 0L);

            Event event = new Event(currentEvent.getName(), currentEvent.getType(), Event.EventStatus.pending,
                    stateMachineId, null, null, smExecutionVersion);
            eventsDAO.create(stateMachineId, event);
        });

        //Persist replay event
        // TODO: Use replay event source constant defined here
        Event replayEvent = new Event(replayEventData.getName(), replayEventData.getType(), Event.EventStatus.triggered,
                stateMachineId, replayEventData.getData(), replayEventData.getEventSource(), smExecutionVersion);
        return eventsDAO.create(stateMachineId, replayEvent);
    }
}
