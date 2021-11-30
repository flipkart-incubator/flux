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

import com.flipkart.flux.api.VersionedEventData;
import com.flipkart.flux.api.core.FluxError;
import com.flipkart.flux.api.core.Task;
import com.flipkart.flux.client.exception.FluxCancelPathException;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.utils.Pair;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixCommandProperties.ExecutionIsolationStrategy;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;
/**
 * <code>TaskExecutor</code> wraps {@link Task} execution with Hystrix.
 *
 * @author regunath.balasubramanian
 */
public class TaskExecutor extends HystrixCommand<Event> {

    public static final String MANAGED_RUNTIME = "managedRuntime";
    /**
     * The task to execute
     */
    private Task task;

    /**
     * The events used in Task execution
     */
    private VersionedEventData[] events;

    /**
     * State Machine Id to which this task belongs to
     */
    private final String stateMachineId;

    /**
     * Name of the event which is emitted by this Task
     */
    private final String outputEventName;

    /**
     * Execution Version of the task.
     */
    private Long executionVersion;

    /**
     * Constructor for this class
     */
    public TaskExecutor(AbstractTask task, VersionedEventData[] events, String stateMachineId, String outputEventName,
                        Long executionVersion) {
        super(Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey(task.getTaskGroupName()))
                .andCommandKey(HystrixCommandKey.Factory.asKey(task.getName()))
                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(task.getName() + "-TP")) // creating a new thread pool per task by appending "-TP" to the task name
                .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter().withCoreSize(task.getExecutionConcurrency()))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        .withExecutionIsolationStrategy(ExecutionIsolationStrategy.THREAD)
                        .withExecutionTimeoutInMilliseconds(task.getExecutionTimeout())));
        this.task = task;
        this.events = events;
        this.stateMachineId = stateMachineId;
        this.outputEventName = outputEventName;
        this.executionVersion = executionVersion;
    }

    /**
     * The HystrixCommand run method. Executes the Task and returns the result or throws an exception in case of a {@link FluxError}
     *
     * @see com.netflix.hystrix.HystrixCommand#run()
     */
    protected Event run() throws Exception {
        try {
            Pair<Object, FluxError> result = this.task.execute(events);
            if (result.getValue() != null) {
                throw result.getValue();
            }
            final SerializedEvent returnObject = (SerializedEvent) result.getKey();
            if (returnObject != null) {
                return new Event(outputEventName, returnObject.getEventType(), Event.EventStatus.triggered,
                        stateMachineId, returnObject.getSerializedEventData(), MANAGED_RUNTIME, executionVersion);
            }
        } catch (Exception ex) {
            boolean isFluxCancelPathException = false;
            Throwable cause = ex;
            while (cause.getCause() != null || cause.getClass().getName().equals(FluxCancelPathException.class.getName())) {
                if (cause.getClass().getName().equals(FluxCancelPathException.class.getName())) {
                    isFluxCancelPathException = true;
                    break;
                }
                cause = cause.getCause();
            }
            if (isFluxCancelPathException) {
                return new Event(outputEventName, null, Event.EventStatus.cancelled, stateMachineId,
                        null, MANAGED_RUNTIME, executionVersion);
            } else {
                throw ex;
            }
        }
        return null;
    }
}