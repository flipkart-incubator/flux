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

package com.flipkart.flux.impl.task;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.event.DiagnosticLoggingAdapter;
import akka.event.Logging;
import akka.routing.ActorRefRoutee;
import akka.routing.Router;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.api.EventData;
import com.flipkart.flux.api.EventDefinition;
import com.flipkart.flux.api.ExecutionUpdateData;
import com.flipkart.flux.api.Status;
import com.flipkart.flux.api.core.FluxError;
import com.flipkart.flux.api.core.Task;
import com.flipkart.flux.client.exception.FluxRetriableException;
import com.flipkart.flux.client.runtime.FluxRuntimeConnector;
import com.flipkart.flux.client.runtime.RuntimeCommunicationException;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.impl.message.HookAndEvents;
import com.flipkart.flux.impl.message.TaskAndEvents;
import com.flipkart.flux.metrics.iface.MetricsClient;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.netflix.hystrix.exception.HystrixRuntimeException.FailureType;
import scala.concurrent.duration.FiniteDuration;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.flipkart.flux.Constants.STATE_MACHINE_ID;
import static com.flipkart.flux.Constants.TASK_ID;


/**
 * <code>AkkaTask</code> is an Akka {@link UntypedActor} that executes {@link Task} instances concurrently. Tasks are executed using a {@link TaskExecutor} where
 * the execution of {@link Task#execute(EventData[])} is wrapped with a {@link HystrixCommand} to provide isolation and fault tolerance to
 * the Flux runtime.
 *
 * @author regunath.balasubramanian
 * @author shyam.akirala
 */

public class AkkaTask extends UntypedActor {

    /**
     * Logger instance for this class
     */
    private final DiagnosticLoggingAdapter logger = Logging.getLogger(this);

    /**
     * TaskRegistry instance to look up Task instances from
     */
    @Inject
    private static TaskRegistry taskRegistry;

    /**
     * The Flux Runtime Connector instance for dispatching processed Events and execution status updates
     */
    @Inject
    private static FluxRuntimeConnector fluxRuntimeConnector;

    @Inject
    private static MetricsClient metricsClient;

    /**
     * Router instance for the Hook actors
     */
    @Inject
    @Named("HookRouter")
    private Router hookRouter;

    /**
     * ObjectMapper instance for JSON serialization
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * The Akka Actor callback method for processing the Task
     * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
     */
    public void onReceive(Object message) throws Exception {
        if (TaskAndEvents.class.isAssignableFrom(message.getClass())) {
            try {
                TaskAndEvents taskAndEvent = (TaskAndEvents) message;
                metricsClient.decCounter(new StringBuilder().
                        append("stateMachine.").
                        append(taskAndEvent.getStateMachineName()).
                        append(".task.").
                        append(taskAndEvent.getTaskName()).
                        append(".queueSize").toString());
                Map<String, Object> mdc = new HashMap<String, Object>();
                mdc.put(STATE_MACHINE_ID, taskAndEvent.getStateMachineId().toString());
                mdc.put(TASK_ID, taskAndEvent.getTaskId());
                logger.setMDC(mdc);
                logger.info("Akka task processing state machine: {} task: {}", taskAndEvent.getStateMachineId(), taskAndEvent.getTaskId());
                logger.debug("Actor {} received directive {}", this.getSelf(), taskAndEvent);
                if (!taskAndEvent.getIsFirstTimeExecution()) {
                    taskAndEvent.setCurrentRetryCount(taskAndEvent.getCurrentRetryCount() + 1); // increment the retry count
                    // update the Flux runtime incrementing the retry count for the Task
                    fluxRuntimeConnector.incrementExecutionRetries(taskAndEvent.getStateMachineId(), taskAndEvent.getTaskId());
                }
                AbstractTask task = AkkaTask.taskRegistry.retrieveTask(taskAndEvent.getTaskIdentifier());
                if (task != null) {
                    try {
                        // update the Flux runtime with status of the Task as running
                        updateExecutionStatus(taskAndEvent, Status.running, null, false);
                    } catch (RuntimeCommunicationException e) {
                        logger.error("Error occurred while updating task: {} status to running. Error: {}", taskAndEvent.getTaskId(), e.getMessage());
                        throw new FluxError(FluxError.ErrorType.retriable, e.getMessage(), e, false,
                                new FluxError.ExecutionContextMeta(taskAndEvent.getStateMachineId(), taskAndEvent.getStateMachineName() ,taskAndEvent.getTaskName(), taskAndEvent.getTaskId(),
                                        taskAndEvent.getRetryCount(), taskAndEvent.getCurrentRetryCount()));
                    }
                    // Execute any pre-exec HookS
//                    this.executeHooks(AkkaTask.taskRegistry.getPreExecHooks(task), taskAndEvent.getEvents());
                    final String outputEventName = getOutputEventName(taskAndEvent);
                    final TaskExecutor taskExecutor = new TaskExecutor(task, taskAndEvent.getEvents(), taskAndEvent.getStateMachineId(), outputEventName);
                    Event outputEvent = null;

                    try {
                        long startTime = System.currentTimeMillis();
                        outputEvent = taskExecutor.execute();
                        long endTime = System.currentTimeMillis();

                        if (outputEvent != null) {
                            // after successful task execution, post the generated output event for further processing, also update status as part of same call
                            fluxRuntimeConnector.submitEventAndUpdateStatus(
                                    new EventData(outputEvent.getName(), outputEvent.getType(), outputEvent.getEventData(), outputEvent.getEventSource()),
                                    outputEvent.getStateMachineInstanceId(),
                                    new ExecutionUpdateData(taskAndEvent.getStateMachineId(), taskAndEvent.getStateMachineName(), taskAndEvent.getTaskName() ,taskAndEvent.getTaskId(), Status.completed, taskAndEvent.getRetryCount(),
                                            taskAndEvent.getCurrentRetryCount(), null, true));
                        } else {
                            // update the Flux runtime with status of the Task as completed
                            updateExecutionStatus(taskAndEvent, Status.completed, null, true);
                        }
                        logger.info("State machine: {} task: {} execution time: {}ms event/status submission time: {}ms", taskAndEvent.getStateMachineId(), taskAndEvent.getTaskId(), (endTime-startTime), (System.currentTimeMillis()-endTime));

                    } catch (HystrixRuntimeException hre) {
                        FailureType ft = hre.getFailureType();
                        // we signal a timeout for any of Timeout, ThreadPool Rejection or Short-Circuit - all of these may go through with time and retry
                        if (ft.equals(FailureType.REJECTED_THREAD_EXECUTION) || ft.equals(FailureType.SHORTCIRCUIT) || ft.equals(FailureType.TIMEOUT)) {
                            // update flux runtime with task outcome as timeout
                            updateExecutionStatus(taskAndEvent, Status.errored, ft.toString().toLowerCase() , false);

                            throw new FluxError(FluxError.ErrorType.timeout, ft.toString().toLowerCase(),
                                    null, false,
                                    new FluxError.ExecutionContextMeta(taskAndEvent.getStateMachineId(), taskAndEvent.getStateMachineName(), taskAndEvent.getTaskName(), taskAndEvent.getTaskId(),
                                            taskAndEvent.getRetryCount(), taskAndEvent.getCurrentRetryCount()));
                        } else {
                            //check if the exception hierarchy has FluxRetriableException, if yes trigger retry
                            boolean isFluxRetriableException = false;
                            Throwable cause = hre;
                            while (cause.getCause() != null || cause.getClass().getName().equals(FluxRetriableException.class.getName())) {
                                if (cause.getClass().getName().equals(FluxRetriableException.class.getName())) {
                                    isFluxRetriableException = true;
                                    break;
                                }
                                cause = cause.getCause();
                            }
                            if (isFluxRetriableException) {
                                // mark the task outcome as execution failure, and the task is retriable
                                updateExecutionStatus(taskAndEvent, Status.errored, cause.getClass().getName() + " : " + cause.getMessage(), false);

                                throw new FluxError(FluxError.ErrorType.retriable, cause.getClass().getName() + " : " + cause.getMessage(), null, false,
                                        new FluxError.ExecutionContextMeta(taskAndEvent.getStateMachineId(), taskAndEvent.getStateMachineName(), taskAndEvent.getTaskName(), taskAndEvent.getTaskId(),
                                                taskAndEvent.getRetryCount(), taskAndEvent.getCurrentRetryCount()));
                            } else {
                                // mark the task outcome as execution failure, and the task won't retried
                                updateExecutionStatus(taskAndEvent, Status.errored, cause.getClass().getName() + " : " + cause.getMessage(), true);
                            }
                        }
                    } catch (RuntimeCommunicationException e) {
                        logger.error("Task completed but updateStatus/submit failed. State machine: {} task: {} ErrorMsg: {}", taskAndEvent.getStateMachineId(), taskAndEvent.getTaskId(), e.getMessage());
                        // mark the task outcome as execution failure and throw retriable error
                        updateExecutionStatus(taskAndEvent, Status.errored, e.getMessage(), false);

                        throw new FluxError(FluxError.ErrorType.retriable, e.getMessage(), e, false,
                                new FluxError.ExecutionContextMeta(taskAndEvent.getStateMachineId(), taskAndEvent.getStateMachineName(), taskAndEvent.getTaskName(), taskAndEvent.getTaskId(),
                                        taskAndEvent.getRetryCount(), taskAndEvent.getCurrentRetryCount()));
                    } catch (Exception e) {
                        // mark the task outcome as execution failure
                        updateExecutionStatus(taskAndEvent, Status.errored, e.getMessage(), true);
                    }

                    // Execute any post-exec HookS
//                    this.executeHooks(AkkaTask.taskRegistry.getPostExecHooks(task), taskAndEvent.getEvents());
                } else {
                    logger.error("Task received EventS that it cannot process. State machine: {} task: {} Events received are : {}",
                            taskAndEvent.getStateMachineId(), taskAndEvent.getTaskId(), TaskRegistry.getEventsKey(taskAndEvent.getEvents()));
                }
            } catch (FluxError fe) { //this catch block handles local retries
                if (fe.getType().equals(FluxError.ErrorType.timeout) || fe.getType().equals(FluxError.ErrorType.retriable)) {
                    if (fe.getExecutionContextMeta().getAttemptedNoOfRetries() < fe.getExecutionContextMeta().getMaxRetries()) {
                        logger.info("Scheduling Task Id: {} for retry. Current retry count = {}, Cause = {} ", fe.getExecutionContextMeta().getTaskId(),
                                fe.getExecutionContextMeta().getAttemptedNoOfRetries(), fe.getMessage());
                        // mark first time execution flag of a task to false and schedule the task execution for a later time.
                        // As we set first time execution flag to false, when the actor processes the message again, it will increment the retry count
                        ((TaskAndEvents) message).setFirstTimeExecution(false);
                        getContext().system().scheduler().scheduleOnce(
                                FiniteDuration.create((int) Math.pow(2, fe.getExecutionContextMeta().getAttemptedNoOfRetries() + 1), TimeUnit.SECONDS),
                                getSelf(),
                                message,
                                getContext().system().dispatcher(), null);
                        metricsClient.incCounter(new StringBuilder().
                                append("stateMachine.").
                                append(fe.getExecutionContextMeta().getStateMachineName()).
                                append(".task.").
                                append(fe.getExecutionContextMeta().getTaskName()).
                                append(".queueSize").toString());
                    } else {
                        logger.warning("Aborting retries for Task Id : {}. Retry count exceeded : {}", fe.getExecutionContextMeta().getTaskId(),
                                fe.getExecutionContextMeta().getAttemptedNoOfRetries());
                        // update the Flux runtime to mark the Task as sidelined
                        fluxRuntimeConnector.updateExecutionStatus(new ExecutionUpdateData(fe.getExecutionContextMeta().getStateMachineId(), fe.getExecutionContextMeta().getStateMachineName() ,fe.getExecutionContextMeta().getTaskName(),
                                fe.getExecutionContextMeta().getTaskId(), Status.sidelined, fe.getExecutionContextMeta().getMaxRetries(),
                                fe.getExecutionContextMeta().getAttemptedNoOfRetries(), fe.getMessage(), true));
                    }
                }
            }
        } else if (HookExecutor.STATUS.class.isAssignableFrom(message.getClass())) { //todo Revisit the retry logic when Hook support is added
            // do nothing as we don't process or interpret Hook execution responses
        } else if (message instanceof Terminated) {
            /*
			 * add a fresh local AkkaHook instance to the router. This happens only for local JVM routers i.e when Flux runtime 
			 * in local and not distributed/clustered
			 */
            hookRouter = hookRouter.removeRoutee(((Terminated) message).actor());
            ActorRef r = getContext().actorOf(Props.create(AkkaHook.class));
            getContext().watch(r);
            hookRouter = hookRouter.addRoutee(new ActorRefRoutee(r));
        } else {
            logger.error("Task received a message that it cannot process. Only com.flipkart.flux.impl.message.TaskAndEvents is supported. Message type received is : {}", message.getClass().getName());
            unhandled(message);
        }
    }

    /**
     * Helper method to execute pre and post Task execution Hooks as independent Actor invocations
     */
    private void executeHooks(List<AbstractHook> hooks, EventData[] events) {
        if (hooks != null) {
            for (AbstractHook hook : hooks) {
                HookAndEvents hookAndEvents = new HookAndEvents(hook, events);
                hookRouter.route(hookAndEvents, getSelf());
            }
        }
    }

    /**
     * Helper method to JSON serialize the output event
     */
    private String getOutputEventName(TaskAndEvents taskAndEvent) throws java.io.IOException {
        final String outputEvent = taskAndEvent.getOutputEvent();
        return outputEvent != null ? objectMapper.readValue(outputEvent, EventDefinition.class).getName() : null;
    }

    /**
     * Helper method to update execution status through flux runtime connector
     */
    private void updateExecutionStatus(TaskAndEvents taskAndEvent, Status status, String errorMsg, boolean deleteFromRedriver) {
        fluxRuntimeConnector.updateExecutionStatus(new ExecutionUpdateData(
                taskAndEvent.getStateMachineId(), taskAndEvent.getStateMachineName() ,taskAndEvent.getTaskName(), taskAndEvent.getTaskId(), status, taskAndEvent.getRetryCount(),
                taskAndEvent.getCurrentRetryCount(), errorMsg, deleteFromRedriver));
    }
}
