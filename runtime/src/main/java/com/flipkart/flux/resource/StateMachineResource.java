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
 */

package com.flipkart.flux.resource;

import static com.flipkart.flux.constant.RuntimeConstants.DEFAULT_ELB_ID;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.api.EventAndExecutionData;
import com.flipkart.flux.api.EventData;
import com.flipkart.flux.api.EventDefinition;
import com.flipkart.flux.api.ExecutionUpdateData;
import com.flipkart.flux.api.StateMachineDefinition;
import com.flipkart.flux.api.VersionedEventData;
import com.flipkart.flux.client.runtime.EventProxyConnector;
import com.flipkart.flux.constant.RuntimeConstants;
import com.flipkart.flux.controller.WorkFlowExecutionController;
import com.flipkart.flux.dao.ParallelScatterGatherQueryHelper;
import com.flipkart.flux.dao.iface.AuditDAO;
import com.flipkart.flux.dao.iface.EventsDAO;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.dao.iface.StateTraversalPathDAO;
import com.flipkart.flux.dao.iface.StatesDAO;
import com.flipkart.flux.domain.AuditRecord;
import com.flipkart.flux.domain.Context;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.domain.StateMachineStatus;
import com.flipkart.flux.domain.StateTraversalPath;
import com.flipkart.flux.domain.Status;
import com.flipkart.flux.exception.IllegalEventException;
import com.flipkart.flux.exception.RedriverException;
import com.flipkart.flux.exception.ReplayEventException;
import com.flipkart.flux.exception.ReplayableRetryExhaustException;
import com.flipkart.flux.exception.TraversalPathException;
import com.flipkart.flux.impl.RAMContext;
import com.flipkart.flux.metrics.iface.MetricsClient;
import com.flipkart.flux.persistence.DataSourceType;
import com.flipkart.flux.persistence.SelectDataSource;
import com.flipkart.flux.persistence.Storage;
import com.flipkart.flux.representation.IllegalRepresentationException;
import com.flipkart.flux.representation.StateMachinePersistenceService;
import com.flipkart.flux.task.eventscheduler.EventSchedulerRegistry;
import com.flipkart.flux.utils.LoggingUtils;
import com.google.gson.Gson;
import com.google.inject.Inject;

/**
 * <code>StateMachineResource</code> exposes APIs to perform state machine related operations. Ex:
 * Creating SM, receiving event for a SM
 *
 * @author shyam.akirala
 * @author yogesh
 * @author regunath.balasubramanian
 */
@Singleton
@Path("/api/machines")
@Named
public class StateMachineResource {

  /**
   * Single white space label to denote start of processing i.e. the Trigger
   */
  private static final String TRIGGER = " ";

  private static final String CORRELATION_ID = "correlationId";
  /**
   * Logger instance for this class
   */
  private static final Logger logger = LogManager.getLogger(StateMachineResource.class);
  private StateMachinePersistenceService stateMachinePersistenceService;

  private WorkFlowExecutionController workFlowExecutionController;

  private StateMachinesDAO stateMachinesDAO;

  private StatesDAO statesDAO;

  private EventsDAO eventsDAO;

  private AuditDAO auditDAO;

  private EventSchedulerRegistry eventSchedulerRegistry;

  private ObjectMapper objectMapper;

  private MetricsClient metricsClient;

  private ParallelScatterGatherQueryHelper parallelScatterGatherQueryHelper;

  private EventProxyConnector eventProxyConnector;

  private String eventProxyEnabled;

  private StateTraversalPathDAO stateTraversalPathDAO;

  @Inject
  public StateMachineResource(EventsDAO eventsDAO,
      StateMachinePersistenceService stateMachinePersistenceService,
      AuditDAO auditDAO, StateMachinesDAO stateMachinesDAO, StatesDAO statesDAO,
      WorkFlowExecutionController workFlowExecutionController, MetricsClient metricsClient,
      ParallelScatterGatherQueryHelper parallelScatterGatherQueryHelper,
      EventSchedulerRegistry eventSchedulerRegistry,
      EventProxyConnector eventProxyConnector,
      @Named("eventProxyForMigration.enabled") String eventProxyEnabled,
      StateTraversalPath stateTraversalPath,
      StateTraversalPathDAO stateTraversalPathDAO) {
    this.eventsDAO = eventsDAO;
    this.stateMachinePersistenceService = stateMachinePersistenceService;
    this.stateMachinesDAO = stateMachinesDAO;
    this.statesDAO = statesDAO;
    this.auditDAO = auditDAO;
    this.eventSchedulerRegistry = eventSchedulerRegistry;
    this.workFlowExecutionController = workFlowExecutionController;
    this.objectMapper = new ObjectMapper();
    this.metricsClient = metricsClient;
    this.parallelScatterGatherQueryHelper = parallelScatterGatherQueryHelper;
    this.eventProxyConnector = eventProxyConnector;
    this.eventProxyEnabled = eventProxyEnabled;
    this.stateTraversalPathDAO = stateTraversalPathDAO;
  }

  /**
   * Will instantiate a state machine in the flux execution engine
   *
   * @param stateMachineDefinition User input for state machine
   * @return unique machineId of the instantiated state machine
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Timed
  public Response createStateMachine(StateMachineDefinition stateMachineDefinition)
      throws Exception {

    if (stateMachineDefinition == null) {
      throw new IllegalRepresentationException("State machine definition is empty");
    }

    StateMachine stateMachine = null;

    final String stateMachineInstanceId;
    if (stateMachineDefinition.getCorrelationId() != null && !stateMachineDefinition
        .getCorrelationId().isEmpty()) {
      stateMachineInstanceId = stateMachineDefinition.getCorrelationId();
    } else {
      stateMachineInstanceId = UUID.randomUUID().toString();
    }

    if (stateMachineDefinition.getClientElbId() == null) {
      stateMachineDefinition.setClientElbId(DEFAULT_ELB_ID);
    }

    try {
      stateMachine = createAndInitStateMachine(stateMachineInstanceId, stateMachineDefinition);
      metricsClient.markMeter(new StringBuilder().
          append("stateMachine.").
          append(stateMachine.getName()).
          append(".started").toString());
    } catch (ConstraintViolationException ex) {
      //In case of Duplicate correlation key, return http code 409 conflict
      if (ex.getCause() != null) {
        if (ex.getCause().getMessage().toLowerCase().contains("duplicate entry")) {
          return Response.status(Response.Status.CONFLICT.getStatusCode()).entity(
              ex.getCause().getMessage()).build();

        }
      }
      logger.error("Constraint Violation during creating or initiating StateMachine" +
              " with id {} {} {}", stateMachineInstanceId, ex.getCause().getMessage(),
          ex.getStackTrace());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(
          ex.getCause() != null ? ex.getCause().getMessage() : null).build();

    } catch (Exception ex) {
      logger.error(
          "Failed During Creating StateMachine and StateTraversal or Initiating StateMachine with id {} {}",
          stateMachineInstanceId, ex.getStackTrace());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(
          ex.getCause() != null ? ex.getCause().getMessage() : null).build();
    }

    return Response.status(Response.Status.CREATED.getStatusCode()).entity(stateMachine.getId())
        .build();
  }

  /**
   * Creates and starts the state machine. Keeping this method as "protected" so that Transactional
   * interceptor can intercept the call.
   */

  protected StateMachine createAndInitStateMachine(String stateMachineInstanceId,
      StateMachineDefinition stateMachineDefinition)
      throws Exception {
    try {
      // 1. Convert to StateMachine (domain object) and save in DB
      StateMachine stateMachine = stateMachinePersistenceService
          .createStateMachine(stateMachineInstanceId, stateMachineDefinition);

      //create context and dependency graph
      Context context = new RAMContext(System.currentTimeMillis(), null, stateMachine);//TODO: set context id, should we need it ?
      stateMachinePersistenceService
          .createAndPersistStateTraversal(stateMachineInstanceId, stateMachine, context);
      LoggingUtils.registerStateMachineIdForLogging(stateMachine.getId());
      logger.info("Created state machine with Id: {}", stateMachine.getId());
      // 2. initialize and start State Machine
      workFlowExecutionController.initAndStart(stateMachine, context);
      return stateMachine;
    } finally {
      LoggingUtils.deRegisterStateMachineIdForLogging();
    }
  }

  private Boolean isEventSourceContainsReplayable(String eventSource) {
    if (eventSource != null && eventSource.toLowerCase()
        .contains(RuntimeConstants.REPLAY_EVENT.toLowerCase())) {
      return Boolean.TRUE;
    }
    return Boolean.FALSE;
  }

  /**
   * Used to post Data corresponding to an event. This data may be a result of a task getting
   * completed or independently posted (manually, for example)
   *
   * @param machineId machineId the event is to be submitted against
   * @param eventData Json representation of event
   */
  @POST
  @Path("/{machineId}/context/events")
  @Timed
  public Response submitEvent(@PathParam("machineId") String machineId,
      @QueryParam("searchField") String searchField,
      @QueryParam("triggerTime") Long triggerTime,
      EventData eventData
  ) throws Exception {

    try {
      LoggingUtils.registerStateMachineIdForLogging(machineId);
      logger.info("Received event: {} for state machine: {}", eventData.getName(), machineId);
      StateMachine stateMachine = null;
      stateMachine = stateMachinesDAO.findById(machineId);
      if (isEventSourceContainsReplayable(eventData.getEventSource())) {
        return Response.status(Response.Status.FORBIDDEN.getStatusCode()).entity(
            "EventSource cannot contain " + RuntimeConstants.REPLAY_EVENT
                + " as it is specific to replay event").build();
      }
      if (stateMachine == null) {
        if (eventProxyEnabled.equalsIgnoreCase("yes")) {
          logger.warn("StateMachine " + machineId
              + " not found in this cluster. Forwarding this event to the old cluster.");
          if (triggerTime == null) {
            try {
              eventProxyConnector.submitEvent(eventData.getName(), eventData.getData(), machineId,
                  eventData.getEventSource());
            } catch (Exception ex) {
              logger.error("Unable to forward event to old endpoint, error {}", ex);
            }

          } else {
            try {
              eventProxyConnector
                  .submitScheduledEvent(eventData.getName(), eventData.getData(), machineId,
                      eventData.getEventSource(), triggerTime);
            } catch (Exception ex) {
              logger.error("Unable to forward scheduled event to old endpoint, error {}",
                  ex);
            }
          }
          return Response.status(Response.Status.ACCEPTED.getStatusCode()).entity(
              "State Machine with Id: " + machineId
                  + " not found on this cluster. Forwarding the event to the old cluster").build();
        } else {
          logger.error("StateMachine not found with id: {}, rejecting the event", machineId);
          return Response.status(Response.Status.NOT_FOUND)
              .entity("StateMachine not found with id: " + machineId + ", rejecting the event")
              .build();
        }
      }

      if (stateMachine.getStatus() == StateMachineStatus.cancelled) {
        logger.info("Discarding event: {} as State machine: {} is in cancelled state",
            eventData.getName(), stateMachine.getId());
        return Response.status(Response.Status.ACCEPTED.getStatusCode()).entity(
            "State machine with Id: " + stateMachine.getId()
                + " is in 'cancelled' state. Discarding the event.").build();
      }

      if (triggerTime == null) {
        logger.info("Received event: {} for state machine: {}", eventData.getName(), machineId);
        try {
          Event event = eventsDAO.findValidEventBySMIdAndName(machineId, eventData.getName());
          VersionedEventData versionedEventData = new VersionedEventData(eventData.getName(),
              eventData.getType(), eventData.getData(), eventData.getEventSource(),
              eventData.getCancelled(), event.getExecutionVersion());
          if (versionedEventData.getCancelled() != null && versionedEventData.getCancelled()) {
            workFlowExecutionController
                .handlePathCancellation(stateMachine.getId(), versionedEventData);
          } else {
            workFlowExecutionController.postEvent(versionedEventData, stateMachine.getId());
          }
          return Response.status(Response.Status.ACCEPTED).build();
        } catch (IllegalEventException ex) {
          return Response.status(Response.Status.NOT_FOUND.getStatusCode()).entity(ex.getMessage())
              .build();
        }
      } else {
        logger.info("Received event: {} for state machine: {} with triggerTime: {}",
            eventData.getName(), machineId, triggerTime);
        if (searchField == null || !searchField.equals(CORRELATION_ID)) {
          return Response.status(Response.Status.BAD_REQUEST)
              .entity("searchField=correlationId is missing in the request").build();
        }

        //if trigger time is more than below value, it means the value has been passed in milliseconds, convert it to seconds and register
        if (triggerTime > 9999999999L) {
          triggerTime = triggerTime / 1000;
        }
        eventSchedulerRegistry.registerEvent(machineId, eventData.getName(),
            objectMapper.writeValueAsString(eventData), triggerTime);
        return Response.status(Response.Status.ACCEPTED).build();
      }
    } finally {
      LoggingUtils.deRegisterStateMachineIdForLogging();
    }
  }

  /**
   * Used to post Data corresponding to a replay event. This data is independently posted (manually,
   * for example)
   *
   * @param machineId machineId the event is to be submitted against
   * @param eventData Json representation of event
   */
  @POST
  @Path("/{machineId}/context/replayevent")
  @Timed
  public Response submitReplayEvent(@PathParam("machineId") String machineId,
      @QueryParam("searchField") String searchField,
      EventData eventData
  ) throws Exception {

    try {
      if (machineId == null || eventData == null || machineId.isEmpty()) {
        return Response.status(Response.Status.BAD_REQUEST).entity(
            "Please send valid values for machineId and EventData ").build();
      }

      if (checkIfEventDataIsEmpty(eventData)) {
        return Response.status(Response.Status.BAD_REQUEST).entity("Event Data cannot be empty").build();
      }

      LoggingUtils.registerStateMachineIdForLogging(machineId);

      if (eventData.getEventSource() != null) {
        String eventSource;
        if(!eventData.getEventSource().contains(RuntimeConstants.REPLAY_EVENT)) {
          eventSource = eventData.getEventSource() + ":" + RuntimeConstants.REPLAY_EVENT;
          eventData.setEventSource(eventSource);
        }
      } else {
        eventData.setEventSource(RuntimeConstants.REPLAY_EVENT);
      }

      logger.info("Received replay event: {} for state machine id: {}", eventData.getName(),
          machineId);
      StateMachine stateMachine = null;
      stateMachine = stateMachinesDAO.findById(machineId);
      if (stateMachine == null) {
        logger.error("stateMachine with id: {} not found while processing replay event {} ",
            machineId,
            eventData.getName());
        return Response.status(Response.Status.NOT_FOUND).entity(
            "StateMachine with id: " + machineId + " not found while processing replay event: "
                + eventData.getName()).build();
      }
      if (stateMachine.getStatus() == StateMachineStatus.cancelled) {
        logger.info("Discarding replay event: {} as State machine: {} is in cancelled state",
            eventData.getName(), stateMachine.getId());
        return Response.status(Response.Status.PRECONDITION_FAILED.getStatusCode())
            .entity("State machine with Id: "
                + stateMachine.getId() + " is in 'cancelled' state. Discarding the replay event.")
            .build();
      }
      try {
        Optional<Event> replayEvent = eventsDAO.findValidReplayEventBySMIdAndName(machineId,
            eventData.getName());
        if (replayEvent.isPresent()) {
          logger.debug("Found replay event: {} with execution version: {} for SMId: {} ",
              eventData.getName(), replayEvent.get().getExecutionVersion(), machineId);
          workFlowExecutionController.postReplayEvent(eventData, stateMachine);
          return Response.status(Response.Status.ACCEPTED).entity("Successfully submitted "
              + "ReplayEvent: " + eventData.getName() +". Check Flux-Dashboard for this StateMachine Id: "
              + machineId).build();
        } else {
          logger.error("Triggered input event {} doesn't exist as a replay event in database." +
                  " Replay Event is identified by eventSource suffix {}", eventData.getName(),
              RuntimeConstants.REPLAY_EVENT);
          return Response.status(Response.Status.FORBIDDEN).entity(
              "Triggered input event " + eventData.getName()
                  + " doesn't exist as a replay event in database." +
                  " Replay Event is identified by eventSource suffix "
                  + RuntimeConstants.REPLAY_EVENT).build();
        }
      } catch (ReplayableRetryExhaustException ex) {
        logger.error("Replayable retries exceeded. Error: {}", ex.getMessage());
        return Response.status(Response.Status.FORBIDDEN.getStatusCode()).entity(ex.getMessage())
            .build();
      } catch (ReplayEventException e) {
        logger.error("Error in processing the event: {}. Error: {}",eventData.getName(),e.getMessage());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(e.getMessage())
            .build();
      } catch (IllegalEventException ex) {
        logger.error("Error in processing the request. Error: {}", ex.getMessage());
        return Response.status(Response.Status.FORBIDDEN.getStatusCode())
            .entity(ex.getMessage())
            .build();
      } catch (TraversalPathException ex) {
        logger.error("Error in processing the request. Error: {}", ex.getMessage());
        return Response.status(Response.Status.NOT_FOUND.getStatusCode())
            .entity(ex.getMessage())
            .build();
      } catch (RuntimeException ex) {
        logger.error("Error in processing the request. Error: {}", ex.getMessage());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .entity(ex.getMessage())
            .build();
      }
    } finally {
      LoggingUtils.deRegisterStateMachineIdForLogging();
    }
  }

  /**
   * Used to post Data corresponding to an event. This also updates the task status to completed
   * which generated the event.
   *
   * @param machineId machineId the event is to be submitted against
   * @param eventAndExecutionData Json representation of event and execution updation data
   */
  @POST
  @Path("/{machineId}/context/eventandstatus")
  @Timed
  public Response submitEvent(@PathParam("machineId") String machineId,
      EventAndExecutionData eventAndExecutionData
  ) throws Exception {
    try {
      LoggingUtils.registerStateMachineIdForLogging(machineId);
      StateMachine stateMachine = null;
      stateMachine = stateMachinesDAO.findById(machineId);
      if (stateMachine == null) {
        return Response.status(Response.Status.NOT_FOUND.getStatusCode())
            .entity("State machine with Id: " + machineId + " not found").build();
      }
      VersionedEventData versionedEventData = eventAndExecutionData.getVersionedEventData();
      if (isEventSourceContainsReplayable(versionedEventData.getEventSource())) {
        return Response.status(Response.Status.FORBIDDEN.getStatusCode()).entity(
            "EventSource cannot contain " + RuntimeConstants.REPLAY_EVENT
                + " as it is specific to replay event").build();
      }
      ExecutionUpdateData executionUpdateData = eventAndExecutionData.getExecutionUpdateData();
      try {
        if (executionUpdateData.getTaskExecutionVersion().equals(statesDAO.findById(
            machineId, executionUpdateData.getTaskId()).getExecutionVersion())) {
          logger.info(
              "Received event:{} with eventExecutionVersion:{} from state: {} with stateExecutionVersion:{}"
                  +
                  " for state machine: {}", versionedEventData.getName(),
              versionedEventData.getExecutionVersion(),
              executionUpdateData.getTaskId(), executionUpdateData.getTaskExecutionVersion(),
              machineId);
          if (versionedEventData.getCancelled() != null && versionedEventData.getCancelled()) {
            workFlowExecutionController
                .updateTaskStatusAndHandlePathCancellation(machineId, eventAndExecutionData);
          } else {
            workFlowExecutionController.updateTaskStatus(machineId, executionUpdateData.getTaskId(),
                executionUpdateData.getTaskExecutionVersion(), executionUpdateData);
            workFlowExecutionController.postEvent(versionedEventData, stateMachine.getId());
          }
        } else {
          // This is an event for invalid state(execution version no more valid), hence discarded.
          // We can store this event data for audit purpose.
          // TODO: Add test for this scenario.
          logger.info("Discarding event:{} with eventExecutionVersion:{} from state: {} with " +
                  "stateExecutionVersion:{}" +
                  " for state machine: {}. StateExecutionVersion is not valid anymore."
                  + " EventData will be saved in data store for audit purpose with eventExecutionVersion:{}.",
              versionedEventData.getName(), versionedEventData.getExecutionVersion(),
              executionUpdateData.getTaskId(), executionUpdateData.getTaskExecutionVersion(),
              machineId, versionedEventData.getExecutionVersion());
          //Persist the event with the new event data from akka
          workFlowExecutionController.persistDiscardedEvent(machineId, versionedEventData);
        }
      } catch (IllegalEventException ex) {
        return Response.status(Response.Status.NOT_FOUND.getStatusCode()).entity(ex.getMessage())
            .build();
      }
    } finally {
      LoggingUtils.deRegisterStateMachineIdForLogging();
    }
    return Response.status(Response.Status.ACCEPTED).build();
  }


  /**
   * Update EventData of the specified Event name under the specified State machine
   *
   * @param machineId the state machine identifier
   * @return Response with execution status code
   */
  @POST
  @Path("/{machineId}/context/eventupdate")
  @Timed
  public Response updateEvent(@PathParam("machineId") String machineId,
      EventData eventData
  ) throws Exception {

    if (machineId.isEmpty() || machineId == null) {
      logger.error("machineId {} null or empty.", machineId);
      return Response.status(Response.Status.FORBIDDEN.getStatusCode()).entity(
          "StateMachineId cannot be null or empty.").build();
    }
    StateMachine stateMachine = stateMachinesDAO.findById(machineId);
    if (stateMachine == null) {
      logger.error("State Machine with input machineId {} doesn't exist.", machineId);
      return Response.status(Response.Status.NOT_FOUND.getStatusCode()).entity(
          "State Machine with input machineId doesn't exist.").build();
    }
    if (isEventSourceContainsReplayable(eventData.getEventSource())) {
      return Response.status(Response.Status.FORBIDDEN.getStatusCode()).entity(
          "EventSource cannot contain " + RuntimeConstants.REPLAY_EVENT
              + " as it is specific to replay event").build();
    }
    try {
      LoggingUtils.registerStateMachineIdForLogging(machineId);
      if (eventData.getData() == null || eventData.getName() == null
          || eventData.getEventSource() == null) {
        return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).entity(
            "Event Data|Name|Source cannot be null.").build();
      }
      Event event = eventsDAO.findValidEventBySMIdAndName(machineId, eventData.getName());
      if (event == null) {
        logger.error("Event with input event Name {} doesn't exist or is invalid.",
            eventData.getName());
        return Response.status(Response.Status.NOT_FOUND.getStatusCode()).entity(
            "Event with input event Name doesn't exist or is invalid.").build();
      }
      if (event.getStatus() != Event.EventStatus.triggered) {
        return Response.status(Response.Status.FORBIDDEN.getStatusCode()).entity(
            "Input event is not in triggered state.").build();
      }
      logger.info("Received event update request for event:{}", eventData.getName());
      /* List holds objects retrieved from States matching input machineId and eventName as [taskId, machineId, status] */
      List<State> states = statesDAO.findStatesByDependentEvent(machineId, eventData.getName());
      if (validateEventUpdate(states)) {
        VersionedEventData versionedEventData = new VersionedEventData(eventData.getName(),
            eventData.getType(),
            eventData.getData(), eventData.getEventSource(), eventData.getCancelled(),
            event.getExecutionVersion());
        workFlowExecutionController.updateEventData(machineId, versionedEventData);
        for (State state : states) {
          Status stateStatus = state.getStatus();
          if (stateStatus == Status.initialized || stateStatus == Status.errored
              || stateStatus == Status.sidelined) {
            try {
              workFlowExecutionController.unsidelineState(state.getStateMachineId(), state.getId());
            } catch (Exception ex) {
              logger.warn("Unable to unsideline for stateId:{}, execution version:{} msg:{}", state.getId(),
                  state.getExecutionVersion(), ex.getMessage());
            }
          }
        }
        return Response.status(Response.Status.ACCEPTED).build();
      }
    } finally {
      LoggingUtils.deRegisterStateMachineIdForLogging();
    }
    return Response.status(Response.Status.CONFLICT.getStatusCode())
        .entity("Current stateMachine's state is not " +
            "eligible for this event's update, try after some time.").build();
  }

  public boolean validateEventUpdate(List<State> states) {
    boolean canUpdateEventData = false;
    for (State state : states) {
      Status status = state.getStatus();
      if (status == Status.running) {
        canUpdateEventData = false;
        break;
      } else if (status == Status.initialized || status == Status.errored
          || status == Status.sidelined) {
        canUpdateEventData = true;
      }
    }
    return canUpdateEventData;
  }

  /***
   * Update EventData of the specified Event name under the specified State machine for completed tasks
   *
   * @param machineId
   * @param eventData
   * @return
   */
  @POST
  @Path("/{machineId}/context/internaleventupdate")
  @Timed
  public Response updateInternalEvent(@PathParam("machineId") String machineId,
      EventData eventData
  ) throws Exception {
    //TODO: Add check
    if (machineId.isEmpty() || machineId == null) {
      logger.error("machineId {} null or empty.", machineId);
      return Response.status(Response.Status.FORBIDDEN.getStatusCode()).entity(
          "StateMachineId cannot be null or empty.").build();
    }
    LoggingUtils.registerStateMachineIdForLogging(machineId);
    StateMachine stateMachine = stateMachinesDAO.findById(machineId);
    if (stateMachine == null) {
      logger.error("State Machine with input machineId {} doesn't exist.", machineId);
      return Response.status(Response.Status.NOT_FOUND.getStatusCode()).entity(
          "State Machine with input machineId: " + machineId + " doesn't exist.").build();
    }
    if (isEventSourceContainsReplayable(eventData.getEventSource())) {
      return Response.status(Response.Status.FORBIDDEN.getStatusCode()).entity(
          "EventSource cannot contain " + RuntimeConstants.REPLAY_EVENT
              + " as it is specific to replay event").build();
    }
    try {
      if (eventData.getData() == null || eventData.getName() == null
          || eventData.getEventSource() == null) {
        return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).entity(
            "Event Data|Name|Source cannot be null.").build();
      }
      Event event = eventsDAO.findValidEventBySMIdAndName(machineId, eventData.getName());
      if (event == null) {
        logger.error("Event with input event Name {} doesn't exist.", eventData.getName());
        return Response.status(Response.Status.NOT_FOUND.getStatusCode()).entity(
            "Event with input event Name doesn't exist.").build();
      }
      if (event.getStatus() != Event.EventStatus.triggered) {
        return Response.status(Response.Status.FORBIDDEN.getStatusCode()).entity(
            "Input event is not in triggered state.").build();
      }
      logger.info("Received event update request for event: {}", eventData.getName());
      List<State> replayableStates = new ArrayList<>();
      List<State> states = statesDAO.findStatesByDependentEvent(machineId, eventData.getName());
      for (State state : states) {
        if (statesDAO.findById(machineId, state.getId()).getReplayable()) {
          replayableStates.add(state);
        }
      }

      if (replayableStates.isEmpty()) {
        return Response.status(Response.Status.FORBIDDEN.getStatusCode()).entity(
            "None of the states having dependency of event " + eventData.getName()
                + " is replayable.").build();
      }

      if (validateInternalEventUpdate(replayableStates)) {
        VersionedEventData versionedEventData = new VersionedEventData(eventData.getName(),
            eventData.getType(),
            eventData.getData(), eventData.getEventSource(), eventData.getCancelled(),
            event.getExecutionVersion());
        workFlowExecutionController.updateEventData(machineId, versionedEventData);
        return Response.status(Response.Status.ACCEPTED).build();
      }

    } catch (Exception e) {
      throw new Exception(e.getMessage());
    } finally {
      LoggingUtils.deRegisterStateMachineIdForLogging();
    }
    return Response.status(Response.Status.CONFLICT.getStatusCode())
        .entity("Current stateMachine's state is not " +
            "eligible for this event's update, try updating using updateEvent.").build();
  }

  public boolean validateInternalEventUpdate(List<State> states) {
    boolean canUpdateInternalEventData = false;
    for (State state : states) {
      Status status = state.getStatus();
      if (status == Status.completed) {
        canUpdateInternalEventData = true;
        break;
      }
    }
    return canUpdateInternalEventData;
  }

  /**
   * Updates the status of the specified Task under the specified State machine
   *
   * @param machineId the state machine identifier
   * @param stateId the task/state identifier
   * @return Response with execution status code
   */
  @POST
  @Path("/{machineId}/{stateId}/{taskExecutionVersion}/status")
  @Timed
  public Response updateStatus(@PathParam("machineId") String machineId,
      @PathParam("stateId") Long stateId,
      @PathParam("taskExecutionVersion") Long taskExecutionVersion,
      ExecutionUpdateData executionUpdateData
  ) throws Exception {
    this.workFlowExecutionController
        .updateTaskStatus(machineId, stateId, taskExecutionVersion, executionUpdateData);
    return Response.status(Response.Status.ACCEPTED).build();
  }


  /**
   * Increments the retry count for the specified Task under the specified State machine
   *
   * @param machineId the state machine identifier
   * @param stateId the task/state identifier
   * @return Response with execution status code
   */
  @POST
  @Path("/{machineId}/{stateId}/{taskExecutionVersion}/retries/inc")
  @Transactional
  @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
  public Response incrementRetry(@PathParam("machineId") String machineId,
      @PathParam("stateId") Long stateId,
      @PathParam("taskExecutionVersion") Long taskExecutionVersion
  ) {
    this.workFlowExecutionController
        .incrementExecutionRetries(machineId, stateId, taskExecutionVersion);
    return Response.status(Response.Status.ACCEPTED).build();
  }

  /**
   * Triggers task execution if the task is stalled and no.of retries are not exhausted.
   *
   * @param taskId the task/state identifier
   */
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/redrivetask/{machineId}/taskId/{taskId}/taskExecutionVersion/{taskExecutionVersion}")
  @Timed
  public Response redriveTask(@PathParam("machineId") String machineId,
      @PathParam("taskId") Long taskId, @PathParam("taskExecutionVersion") Long executionVersion)
      throws Exception {

    try{
      this.workFlowExecutionController.redriveTask(machineId, taskId, executionVersion);
    }catch (RedriverException e){
      return Response.status(Response.Status.PRECONDITION_FAILED).entity(e.getMessage()).build();
    }

    return Response.status(Response.Status.ACCEPTED).build();
  }

  /**
   * Provides json data to build fsm status graph.
   *
   * @return json representation of fsm
   */
  @GET
  @Path("/{machineId}/fsmdata")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFsmGraphData(@PathParam("machineId") String machineId) throws IOException {
    return Response.status(200).entity(getGraphData(machineId)).build();
  }

  /**
   * Retrieves all errored states for the given range of time for a particular state machine name.
   *
   * @param fromTime starting time for the range
   * @param toTime ending time (inclusive)
   * @return json containing list of [state machine id,  state id, status]
   */
  @GET
  @Path("/{stateMachineName}/states/errored")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getErroredStates(@PathParam("stateMachineName") String stateMachineName,
      @QueryParam("fromTime") String fromTime,
      @QueryParam("toTime") String toTime) {
    if (fromTime == null || toTime == null) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Required params fromTime/toTime are not provided").build();
    }

    Timestamp fromTimestamp = Timestamp.valueOf(fromTime);
    Timestamp toTimestamp = Timestamp.valueOf(toTime);

    if (fromTimestamp.after(toTimestamp)) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("fromTime: " + fromTime + " should be before toTime: " + toTime).build();
    }

    return Response.status(200).entity(parallelScatterGatherQueryHelper
        .findErroredStates(stateMachineName, fromTimestamp, toTimestamp)).build();
  }

  /**
   * Retrieves all states for the given range of time for a particular state machine name. Will also
   * filter by status if it is given.
   *
   * @return json containing list of [state machine id, state id, status]
   */
  @GET
  @Path("/{stateMachineName}/states/listbytime")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getStatesByTime(@PathParam("stateMachineName") String stateMachineName,
      @QueryParam("fromTime") String fromTime,
      @QueryParam("toTime") String toTime,
      @QueryParam("stateName") String stateName,
      @QueryParam("statuses") final List<String> statusStrings) throws Exception {
    if (fromTime == null || toTime == null) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Required params fromTime/toTime are not provided").build();
    }

    Timestamp fromTimestamp = Timestamp.valueOf(fromTime);
    Timestamp toTimestamp = Timestamp.valueOf(toTime);

    if (fromTimestamp.after(toTimestamp)) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("fromTime: " + fromTime + " should be before toTime: " + toTime).build();
    }
    List<Status> statuses = new ArrayList<Status>();
    if (statusStrings != null && !statusStrings.isEmpty()) {
      for (String status : statusStrings) {
        try {
          statuses.add(Status.valueOf(status));
        } catch (IllegalArgumentException e) {
          return Response.status(Response.Status.BAD_REQUEST).entity("status: " + status
              + " must be one of initialized, running, completed, cancelled, errored, sidelined, unsidelined")
              .build();
        }
      }
    }
    return Response.status(200).entity(parallelScatterGatherQueryHelper
        .findStatesByStatus(stateMachineName, fromTimestamp, toTimestamp, stateName, statuses))
        .build();
  }

  /**
   * This api unsidelines a single state and triggers its execution.
   */
  @PUT
  @Path("/{stateMachineId}/{stateId}/unsideline")
  @Produces(MediaType.APPLICATION_JSON)
  @Transactional
  @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
  public Response unsidelineState(@PathParam("stateMachineId") String stateMachineId,
      @PathParam("stateId") Long stateId) {

    workFlowExecutionController.unsidelineState(stateMachineId, stateId);
    return Response.status(Response.Status.ACCEPTED).build();
  }

  /**
   * This api is for resetting the retry count for replayable states
   */
  @PUT
  @Path("/{stateMachineId}/{stateId}/reset_attempted_number_of_retries")
  @Produces(MediaType.APPLICATION_JSON)
  public Response resetAttemptedNoOfRetries(@PathParam("stateMachineId") String stateMachineId,
      @PathParam("stateId") Long stateId) {

    if (stateMachineId == null || stateMachineId.isEmpty() || stateId == null) {
      logger.error("machineId {} null or empty.", stateMachineId);
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("StateMachineId cannot be null or empty").build();
    }
    try {
      LoggingUtils.registerStateMachineIdForLogging(stateMachineId);
      Optional<StateTraversalPath> traversalPathStates = stateTraversalPathDAO.findById(
          stateMachineId, stateId);
      if (traversalPathStates.isPresent()) {
        List<Long> stateIds = traversalPathStates.get().getNextDependentStates();
        List<String> eventNames = new ArrayList<>();
        for (State s : statesDAO.findAllStatesForGivenStateIds(stateMachineId, stateIds)) {
          try {
            eventNames.add(getOutputEventName(s.getOutputEvent()));
          } catch (IOException e) {
            logger.error("Exception in de-serializing the Json output: {} ",
                e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage())
                .build();
          }
        }
        workFlowExecutionController.deleteInvalidEvents(stateMachineId, eventNames);
        workFlowExecutionController.resetAttemptedNumberOfRetries(stateMachineId, stateId);
        return Response.status(Response.Status.ACCEPTED).entity("Retries reset to zero for stateId: "+stateId).build();
      }
      return Response.status(Response.Status.ACCEPTED).entity("Not a replayable state: "+stateId).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    } finally {
      LoggingUtils.deRegisterStateMachineIdForLogging();
    }

  }

  @PUT
  @Path("/{stateMachineId}/cancel")
  @Produces(MediaType.APPLICATION_JSON)
  @Transactional
  @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
  public Response cancelWorkflow(@PathParam("stateMachineId") String stateMachineId,
      @QueryParam("searchField") String searchField) {

    StateMachine stateMachine = null;
    stateMachine = stateMachinesDAO.findById(stateMachineId);

    if (stateMachine == null) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity("State machine with id: " + stateMachineId + " not found").build();
    }

    workFlowExecutionController.cancelWorkflow(stateMachine);

    return Response.status(Response.Status.ACCEPTED).build();
  }

  @GET
  @Path("/{stateMachineId}/info")
  @Produces(MediaType.APPLICATION_JSON)
  @Transactional
  @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
  public Response getStateMachine(@PathParam("stateMachineId") String stateMachineId,
      @QueryParam("searchField") String searchField) {
    StateMachine stateMachine = null;
    stateMachine = stateMachinesDAO.findById(stateMachineId);

    if (stateMachine == null) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity("State machine with id: " + stateMachineId + " not found").build();
    }

    List<Event> events = eventsDAO.findBySMInstanceId(stateMachine.getId());
    List<AuditRecord> auditRecords = auditDAO.findBySMInstanceId(stateMachine.getId());

    Map<String, Object> stateMachineInfo = objectMapper.convertValue(stateMachine, Map.class);
    stateMachineInfo.put("events", events);
    stateMachineInfo.put("auditrecords", auditRecords);

    return Response.status(Response.Status.OK).entity(stateMachineInfo).build();
  }

  /**
   * Retrieves fsm graph data based on FSM Id or correlation id
   */
  private FsmGraph getGraphData(String machineId) throws IOException {
    String fsmId = machineId;

    StateMachine stateMachine;
    stateMachine = stateMachinesDAO.findById(fsmId);

    if (stateMachine == null) {
      throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
          .entity("State machine with Id: " + machineId + " not found").build());
    }
    final FsmGraph fsmGraph = new FsmGraph();

    List<Long> erroredStateIds = new LinkedList<>();

    for (State state : stateMachine.getStates()) {
      if (state.getStatus() == Status.errored || state.getStatus() == Status.sidelined) {
        erroredStateIds.add(state.getId());
      }
    }

    Collections.sort(erroredStateIds);
    fsmGraph.setErroredStateIds(erroredStateIds);
    fsmGraph.setStateMachineId(stateMachine.getId());
    fsmGraph.setFsmVersion(stateMachine.getVersion());
    fsmGraph.setFsmName(stateMachine.getName());
    fsmGraph.setFsmStatus(stateMachine.getStatus());

    Map<String, Event> stateMachineEvents = eventsDAO.findBySMInstanceId(stateMachine.getId())
        .stream().collect(
            Collectors.toMap(Event::getName, (event -> event)));
    Set<String> allOutputEventNames = new HashSet<>();

    final RAMContext ramContext = new RAMContext(System.currentTimeMillis(), null, stateMachine);
        /* After this operation, we'll have nodes for each state and its corresponding output event along with the output event's dependencies mapped out*/
    for (State state : stateMachine.getStates()) {
      final FsmGraphVertex vertex = new FsmGraphVertex(state.getId(),
          this.getStateDisplayName(state.getName()), state.getStatus().name());
      if (state.getOutputEvent() != null) {
        EventDefinition eventDefinition = objectMapper
            .readValue(state.getOutputEvent(), EventDefinition.class);
        final Event outputEvent = stateMachineEvents.get(eventDefinition.getName());
        fsmGraph.addVertex(vertex,
            new FsmGraphEdge(getEventDisplayName(outputEvent.getName()),
                outputEvent.getStatus().name(), outputEvent.getEventSource(),
                outputEvent.getEventData()+"#"+outputEvent.getExecutionVersion(),
                outputEvent.getUpdatedAt()));
        final Set<State> dependantStates = ramContext.getDependantStates(outputEvent.getName());
        dependantStates.forEach((aState) -> fsmGraph.addOutgoingEdge(vertex, aState.getId()));
        allOutputEventNames
            .add(outputEvent.getName()); // we collect all output event names and use them below.
      } else {
        fsmGraph.addVertex(vertex,
            new FsmGraphEdge(null, null, null, null, null));
      }
    }

        /* Handle states with no dependencies, i.e the states that can be triggered as soon as we execute the state machine */
    final Set<State> initialStates = ramContext.getInitialStates(Collections
        .emptySet());// hackety hack.  We're fooling the context to give us only events that depend on nothing
    if (!initialStates.isEmpty()) {
      final FsmGraphEdge initEdge = new FsmGraphEdge(TRIGGER, Event.EventStatus.triggered.name(),
          TRIGGER, null, null);
      initialStates.forEach((state) -> {
        initEdge.addOutgoingVertex(state.getId());
      });
      fsmGraph.addInitStateEdge(initEdge);
    }
        /* Now we handle events that were not "output-ed" by any state, which means that they were given to the workflow at the time of invocation or supplied externally*/
    final HashSet<String> eventsGivenOnWorkflowTrigger = new HashSet<>(stateMachineEvents.keySet());
    eventsGivenOnWorkflowTrigger.removeAll(allOutputEventNames);
    eventsGivenOnWorkflowTrigger.forEach((workflowTriggeredEventName) -> {
      final Event correspondingEvent = stateMachineEvents.get(workflowTriggeredEventName);
      final FsmGraphEdge initEdge = new FsmGraphEdge(
          this.getEventDisplayName(workflowTriggeredEventName),
          correspondingEvent.getStatus().name(), correspondingEvent.getEventSource(),
          correspondingEvent.getEventData()+"#"+correspondingEvent.getExecutionVersion(),
          correspondingEvent.getUpdatedAt());
      final Set<State> dependantStates = ramContext.getDependantStates(workflowTriggeredEventName);
      dependantStates.forEach((state) -> initEdge.addOutgoingVertex(state.getId()));
      fsmGraph.addInitStateEdge(initEdge);
    });

    fsmGraph.setAuditData(auditDAO.findBySMInstanceId(stateMachine.getId()));

    return fsmGraph;
  }

  /**
   * Helper method to return a display friendly name for the specified event name. Returns just the
   * name part from the Event FQN
   */
  private String getEventDisplayName(String eventName) {
    return (eventName == null) ? null
        : this.getDisplayName(eventName.substring(eventName.lastIndexOf(".") + 1));
  }

  /**
   * Helper method to return a display friendly name for state names Returns {@link
   * #getDisplayName(String)}
   */
  private String getStateDisplayName(String stateName) {
    return this.getDisplayName(stateName);
  }

  /**
   * Helper method to return a display friendly name for the specified label. Returns a phrase
   * containing single-space separated words that were split at Camel Case boundaries
   */
  private String getDisplayName(String label) {
    if (label == null) {
      return null;
    }
    String words = label.replaceAll(
        // Based on http://stackoverflow.com/questions/2559759/how-do-i-convert-camelcase-into-human-readable-names-in-java
        String.format("%s|%s|%s",
            "(?<=[A-Z])(?=[A-Z][a-z])",
            "(?<=[^A-Z])(?=[A-Z])",
            "(?<=[A-Za-z])(?=[^A-Za-z])"
        ), " ");
    StringBuffer sb = new StringBuffer();
    for (String s : words.split(" ")) {
      sb.append(Character.toUpperCase(s.charAt(0)));
      if (s.length() > 1) {
        sb.append(s.substring(1).toLowerCase());
        sb.append(
            " "); // add the single space back. Used for wrapping words onto next line in the display
      }
    }
    return sb.toString().trim();
  }

  /***
   * The method returns true if any variable of EventData class is empty
   * @param eventData
   * @return
   */
  //TODO: verify event source again
  private Boolean checkIfEventDataIsEmpty(EventData eventData) {
    if (eventData.getName() == null || eventData.getType() == null || eventData.getData() == null
        || eventData.getName().isEmpty() || eventData.getType().isEmpty() || eventData.getData().isEmpty()) {
      return Boolean.TRUE;
    }
    return Boolean.FALSE;
  }

  /**
   * Helper method to JSON serialize the output event for output event name
   */
  //TODO: move it to common place
  private String getOutputEventName(String outputEvent) throws IOException {
    return outputEvent != null ? objectMapper.readValue(outputEvent, EventDefinition.class)
        .getName() : null;
  }

  @GET
  @Path("/{machineId}/{eventName}/{taskExecutionVersion}/eventdata")
  @Produces(MediaType.APPLICATION_JSON)
  @Transactional
  @SelectDataSource(type = DataSourceType.READ_WRITE, storage = Storage.SHARDED)
  public Response getEventData(
      @PathParam("machineId") String smId,
      @PathParam("eventName") String eventName,
      @PathParam("taskExecutionVersion") Long taskExecutionVersion) {

    if (smId == null || eventName == null || taskExecutionVersion == null) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("smId/eventName/taskExecutionVersion cannot be null")
          .build();
    }

    try {
      Event event =
          workFlowExecutionController.getEventData(smId, eventName, taskExecutionVersion);

      Gson gson = new Gson();
      return Response.status(Response.Status.OK).entity(gson.toJson(event)).build();
    } catch (IllegalEventException e) {
      return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }
  }
}