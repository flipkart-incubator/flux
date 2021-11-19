package com.flipkart.flux.representation;

import com.flipkart.flux.api.EventData;
import com.flipkart.flux.dao.iface.EventsDAO;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.dao.iface.StatesDAO;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.Status;
import com.flipkart.flux.persistence.DataSourceType;
import com.flipkart.flux.persistence.SelectDataSource;
import com.flipkart.flux.persistence.SessionFactoryContext;
import com.flipkart.flux.persistence.Storage;
import com.google.inject.name.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

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
                              EventsDAO eventsDAO, StatesDAO statesDAO,@Named("fluxSessionFactoriesContext") SessionFactoryContext sessionFactoryContext) {
        this.stateMachinesDAO = stateMachinesDAO;
        this.eventsDAO = eventsDAO;
        this.statesDAO = statesDAO;
        this.sessionFactoryContext = sessionFactoryContext;
    }

    /**
     * TODO : Rephrase this description
     * In a Transaction :
     *  1. Increment, update and read executionVersion for this State Machine.
     *  2. Mark all states and Update executionVersion for all states(marked as invalid) retrieved in step 1.
     *  3. Mark dependant events as invalid.
     *  4. Create new event entries in pending status including replay event as triggered containing event data with executionVersion
     *     read in step 2.
     * @param eventData
     * @param dependantStates
     * @param dependantEvents
     * @param stateMachineId
     */
    @Transactional
    @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
    public Event persistAndProcessReplayEvent(String stateMachineId, EventData eventData, Set<State> dependantStates,
                                              List<String> dependantEvents) {

        Session session = sessionFactoryContext.getThreadLocalSession();

        // TODO : This will be updated to return updated value in same call. Need to add tests for it.
        Long smExecutionVersion = stateMachinesDAO.findById(stateMachineId).getExecutionVersion() + 1;
        stateMachinesDAO.updateExecutionVersion_NonTransactional(stateMachineId, smExecutionVersion,session);

        ArrayList<State> states = new ArrayList<>(dependantStates);
        statesDAO.updateStatus_NonTransactional(stateMachineId,states, Status.initialized, session);
        statesDAO.updateExecutionVersion_NonTransactional(stateMachineId,states,smExecutionVersion, session);

        // TODO : This should be moved to EventPersistenceService
        eventsDAO.markEventsAsInvalid_NonTransactional(stateMachineId, dependantEvents, session);

        // Remove replay event. Purpose was to mark all it's previous version invalid.
        dependantEvents.remove(eventData.getName());

        for (String eventName : dependantEvents) {
            // To retrieve event meta data (type) for creating new event with new smExecutionVersion
            // Retrieving for executionVersion 0 is fine because type of event doesn't change with executionVersion
            Event currentEvent = eventsDAO.findValidEventsByStateMachineIdAndExecutionVersionAndName(stateMachineId, eventName, 0L);

            Event event = new Event(currentEvent.getName(), currentEvent.getType(), Event.EventStatus.pending,
                    stateMachineId, null, null, smExecutionVersion);
            eventsDAO.create(stateMachineId, event);
        }

        //Persist replay event
        Event replayEvent = new Event(eventData.getName(), eventData.getType(), Event.EventStatus.triggered, stateMachineId,
                eventData.getData(), eventData.getEventSource(), smExecutionVersion);
        return eventsDAO.create(stateMachineId, replayEvent);
    }
}
