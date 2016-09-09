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
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.ActorRefRoutee;
import akka.routing.Router;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.api.EventData;
import com.flipkart.flux.api.EventDefinition;
import com.flipkart.flux.api.ExecutionUpdateData;
import com.flipkart.flux.api.Status;
import com.flipkart.flux.api.core.FluxError;
import com.flipkart.flux.api.core.Task;
import com.flipkart.flux.client.runtime.FluxRuntimeConnector;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.impl.message.HookAndEvents;
import com.flipkart.flux.impl.message.TaskAndEvents;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.netflix.hystrix.exception.HystrixRuntimeException.FailureType;

import scala.Option;
import scala.concurrent.duration.FiniteDuration;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * <code>AkkaTask</code> is an Akka {@link UntypedActor} that executes {@link Task} instances concurrently. Tasks are executed using a {@link TaskExecutor} where 
 * the execution of {@link Task#execute(com.flipkart.flux.domain.Event...)} is wrapped with a {@link HystrixCommand} to provide isolation and fault tolerance to 
 * the Flux runtime. 
 *
 * @author regunath.balasubramanian
 */

public class AkkaTask extends UntypedActor {

    /** Logger instance for this class*/
    private LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    /** TaskRegistry instance to look up Task instances from */
    @Inject
    private static TaskRegistry taskRegistry;

    /** The Flux Runtime Connector instance for dispatching processed EventS and execution status updates*/
    @Inject
    private static FluxRuntimeConnector fluxRuntimeConnector;

    /** Router instance for the Hook actors*/
    @Inject
    @Named("HookRouter")
    private Router hookRouter;

    /** ObjectMapper instance for JSON serialization*/
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Callback method when this Actor is restarted by the Supervisor actor. This happens only when this Actor throws FluxError.ErrorType.timeout.
     * @see akka.actor.UntypedActor#preRestart(java.lang.Throwable, scala.Option)
     */
    public void preRestart(Throwable reason, Option<Object> message) {
        if (FluxError.class.isAssignableFrom(reason.getClass()) && TaskAndEvents.class.isAssignableFrom(message.get().getClass())) {
            FluxError fluxError = (FluxError)reason;
            TaskAndEvents taskAndEvent = (TaskAndEvents)message.get();
            taskAndEvent.setCurrentRetryCount(fluxError.getExecutionContextMeta().getAttemptedNoOfRetries() + 1); // increment the retry count
            // update the Flux runtime incrementing the retry count for the Task
            fluxRuntimeConnector.incrementExecutionRetries(taskAndEvent.getStateMachineId(), taskAndEvent.getTaskId());
            // we reschedule the message back on this Actor to see if it goes through when retried. TODO : Uses a fixed 1 second time delay and no exponential backoff for now
            getContext().system().scheduler().scheduleOnce(
                    FiniteDuration.create(1, TimeUnit.SECONDS),
                    getSelf(),
                    taskAndEvent,
                    getContext().system().dispatcher(), null);
        }
    }

    /**
     * The Akka Actor callback method for processing the Task
     * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
     */
    public void onReceive(Object message) throws Exception {
        //todo actor can take a call when to throw TaskResumableException
        if (TaskAndEvents.class.isAssignableFrom(message.getClass())) {
            TaskAndEvents taskAndEvent = (TaskAndEvents)message;
            logger.debug("Received directive {}", taskAndEvent);
            AbstractTask task = AkkaTask.taskRegistry.retrieveTask(taskAndEvent.getTaskIdentifier());
            if (task != null) {
                // update the Flux runtime with status of the Task as running
                fluxRuntimeConnector.updateExecutionStatus(new ExecutionUpdateData(
                        taskAndEvent.getStateMachineId(), taskAndEvent.getTaskId(), Status.running, taskAndEvent.getRetryCount(), taskAndEvent.getCurrentRetryCount()));
                // Execute any pre-exec HookS
                this.executeHooks(AkkaTask.taskRegistry.getPreExecHooks(task), taskAndEvent.getEvents());
                final String outputEventName = getOutputEventName(taskAndEvent);
                final TaskExecutor taskExecutor = new TaskExecutor(task, taskAndEvent.getEvents(), taskAndEvent.getStateMachineId(), outputEventName);
                Event outputEvent = null;
                try {
                    outputEvent = taskExecutor.execute();
                    // update the Flux runtime with status of the Task as completed
                    fluxRuntimeConnector.updateExecutionStatus(
                            new ExecutionUpdateData(taskAndEvent.getStateMachineId(), taskAndEvent.getTaskId(), Status.completed, taskAndEvent.getRetryCount(), taskAndEvent.getCurrentRetryCount()));
                } catch (HystrixRuntimeException hre) {
                	FailureType ft = hre.getFailureType();
                	// we signal a timeout for any of Timeout, ThreadPool Rejection or Short-Circuit - all of these may go through with time and retry
                	if (ft.equals(FailureType.REJECTED_THREAD_EXECUTION) || ft.equals(FailureType.SHORTCIRCUIT) || ft.equals(FailureType.TIMEOUT)) {
                        // update flux runtime with task outcome as timeout
                        fluxRuntimeConnector.updateExecutionStatus(new ExecutionUpdateData(
                                taskAndEvent.getStateMachineId(), taskAndEvent.getTaskId(), Status.errored, taskAndEvent.getRetryCount(),
                                taskAndEvent.getCurrentRetryCount(), "Execution timeout for : " + task.getName()));
                        throw new FluxError(FluxError.ErrorType.timeout, "Execution timeout for : " + task.getName(),
                                null, false,
                                new FluxError.ExecutionContextMeta(taskAndEvent.getStateMachineId(), taskAndEvent.getTaskId(),
                                        taskAndEvent.getRetryCount(), taskAndEvent.getCurrentRetryCount()));
                    } else {
                        // mark the task outcome as execution failure
                        fluxRuntimeConnector.updateExecutionStatus(
                                new ExecutionUpdateData(taskAndEvent.getStateMachineId(), taskAndEvent.getTaskId(), Status.errored,
                                        taskAndEvent.getRetryCount(), taskAndEvent.getCurrentRetryCount(), hre.getMessage()));
                    }
                } catch (Exception e) {
                    // mark the task outcome as execution failure
                    fluxRuntimeConnector.updateExecutionStatus(
                            new ExecutionUpdateData(taskAndEvent.getStateMachineId(), taskAndEvent.getTaskId(), Status.errored,
                                    taskAndEvent.getRetryCount(), taskAndEvent.getCurrentRetryCount(), e.getMessage()));
                } finally {
                    if (outputEvent != null) {
                        getSender().tell(outputEvent, getContext().parent()); // we send back the parent Supervisor Actor as the sender
                    }
                }
                // Execute any post-exec HookS
                this.executeHooks(AkkaTask.taskRegistry.getPostExecHooks(task), taskAndEvent.getEvents());
            } else {
                logger.error("Task received EventS that it cannot process. Events received are : {}",TaskRegistry.getEventsKey(taskAndEvent.getEvents()));
            }
        } else if (HookExecutor.STATUS.class.isAssignableFrom(message.getClass())) {
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

    /** Helper method to JSON serialize the output event*/
    private String getOutputEventName(TaskAndEvents taskAndEvent) throws java.io.IOException {
        final String outputEvent = taskAndEvent.getOutputEvent();
        return outputEvent != null ? objectMapper.readValue(outputEvent, EventDefinition.class).getName() : null;
    }

}
