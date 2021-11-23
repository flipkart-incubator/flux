package com.flipkart.flux.representation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.api.EventData;
import com.flipkart.flux.api.EventDefinition;
import com.flipkart.flux.constant.RuntimeConstants;
import com.flipkart.flux.dao.iface.AuditDAO;
import com.flipkart.flux.dao.iface.EventsDAO;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.dao.iface.StatesDAO;
import com.flipkart.flux.domain.AuditRecord;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.Status;
import com.flipkart.flux.exception.ReplayEventException;
import com.flipkart.flux.persistence.DataSourceType;
import com.flipkart.flux.persistence.SelectDataSource;
import com.flipkart.flux.persistence.SessionFactoryContext;
import com.flipkart.flux.persistence.Storage;
import com.google.gson.JsonParseException;
import com.google.inject.name.Named;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import static com.flipkart.flux.constant.RuntimeConstants.DEFAULT_DEPENDENT_EVENTS_MESSAGE;

/**
 * @author raghavender.m
 * @author akif.khan
 * Used as a business layer to interpret Replay event's trigger, and perform DB operations on states and events
 * in triggered ReplayEvent's traversal path.
 */
@Singleton
public class ReplayEventPersistenceService {

    private StateMachinesDAO stateMachinesDAO;
    private EventsDAO eventsDAO;
    private StatesDAO statesDAO;
    private AuditDAO auditDAO;
    private SessionFactoryContext sessionFactoryContext;

    /**
     * ObjectMapper instance to be used for all purposes in this class
     */
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LogManager.getLogger(ReplayEventPersistenceService.class);

    @Inject
    public ReplayEventPersistenceService(StateMachinesDAO stateMachinesDAO,
                                         EventsDAO eventsDAO, StatesDAO statesDAO, AuditDAO auditDAO,
                                         @Named("fluxSessionFactoriesContext") SessionFactoryContext sessionFactoryContext) {
        this.stateMachinesDAO = stateMachinesDAO;
        this.eventsDAO = eventsDAO;
        this.statesDAO = statesDAO;
        this.auditDAO = auditDAO;
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
                                              List<Long> dependantStateIds, List<String> dependantEvents) throws ReplayEventException {

        Session session = sessionFactoryContext.getThreadLocalSession();

        if(session == null){
            throw new HibernateException("Unable to initialize hibernate Session");
        }

        Long smExecutionVersion = stateMachinesDAO.findExecutionVersionBySMIdForUpdate_NonTransactional(stateMachineId, session) + 1;
        stateMachinesDAO.updateExecutionVersion_NonTransactional(stateMachineId, smExecutionVersion, session);

        ArrayList<Long> stateIds = new ArrayList<>(dependantStateIds);
        statesDAO.updateStatus_NonTransactional(stateMachineId,stateIds, Status.initialized, session);
        statesDAO.updateExecutionVersion_NonTransactional(stateMachineId,stateIds,smExecutionVersion, session);

        //create audit records for all the states
        for (Long stateId : stateIds) {
            auditDAO.create_NonTransactional(new AuditRecord(stateMachineId, stateId, 0L,
                            Status.initialized, null, null, smExecutionVersion, DEFAULT_DEPENDENT_EVENTS_MESSAGE),
                    session);
        }

        for (String outputEvent : dependantEvents) {
            String eventName, eventType;
            try {
                eventName = getOutputEventName(outputEvent);
                eventType = getOutputEventType(outputEvent);
            } catch (JsonParseException e) {
                logger.error("Unable to deserialise output event value. Error: {}",e.getMessage());
                throw new ReplayEventException("Unable to deserialize outputEvent value. Error : " + e.getMessage());
            } catch (Exception e){
                logger.error("Exception. Error: {}",e.getMessage());
                throw new ReplayEventException("Exception in processing. Error : " + e.getMessage());
            }
            if (eventName == null || eventType == null){
                logger.error("Event Name: {} or Event Type: {} cannot be null ",eventName,eventType);
                throw new ReplayEventException("Event Name or Event Type cannot be null");
            }
            eventsDAO.markEventAsInvalid_NonTransactional(stateMachineId, eventName, session);
            Event event = new Event(eventName, eventType, Event.EventStatus.pending,
                    stateMachineId, null, null, smExecutionVersion);
            eventsDAO.create_NonTransactional(event, session);
        }

        //Mark replay event as invalid and persist replay event
        eventsDAO.markEventAsInvalid_NonTransactional(stateMachineId, replayEventData.getName(), session);
        String eventSource;
        if(!replayEventData.getEventSource().contains(RuntimeConstants.REPLAY_EVENT)) {
            eventSource = replayEventData.getEventSource()
                    .concat(":" + RuntimeConstants.REPLAY_EVENT);
        }
        else {
            eventSource = replayEventData.getEventSource();
        }
        Event replayEvent = new Event(replayEventData.getName(), replayEventData.getType(), Event.EventStatus.triggered,
                stateMachineId, replayEventData.getData(), eventSource, smExecutionVersion);
        return eventsDAO.create_NonTransactional(replayEvent, session);
    }

    /**
     * Helper method to JSON serialize the output event for output event name
     */
    private String getOutputEventName(String outputEvent) throws JsonParseException, IOException {
        return outputEvent != null ? objectMapper.readValue(outputEvent, EventDefinition.class).getName() : null;
    }

    /**
     * Helper method to JSON serialize the output event for output event type
     */
    private String getOutputEventType(String outputEvent) throws IOException {
        return outputEvent != null ? objectMapper.readValue(outputEvent, EventDefinition.class).getType() : null;
    }
}
