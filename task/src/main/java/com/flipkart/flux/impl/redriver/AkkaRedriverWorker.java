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
package com.flipkart.flux.impl.redriver;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.client.runtime.FluxRuntimeConnector;
import com.flipkart.flux.domain.Status;
import com.flipkart.flux.impl.message.SerializedRedriverTask;
import com.flipkart.flux.impl.message.TaskAndEvents;
import com.flipkart.flux.impl.message.TaskRedriverDetails;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.flipkart.flux.impl.task.registry.RouterRegistry;

import javax.inject.Inject;

/**
 * Actor for redriving Tasks
 * @author regunath.balasubramanian
 */
public class AkkaRedriverWorker extends UntypedActor {
	
	/** Logger instance for this class*/
    private LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    /** The Flux Runtime Connector instance for getting required objects from Flux Runtime*/
    @Inject
    private static FluxRuntimeConnector fluxRuntimeConnector;

    /** The RouterRegistry instance to look up Routers from*/
    @Inject
    private static RouterRegistry routerRegistry;

    /** ObjectMapper instance for JSON deserialization*/
    private static final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * Actor call back method for executing stalled Tasks 
	 * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
	 */
	public void onReceive(Object message) throws Exception {
		if (message instanceof TaskRedriverDetails && 
				((TaskRedriverDetails)message).getAction().equals(TaskRedriverDetails.RegisterAction.Redrive)) {
			// Check if the Task has stalled. If yes, recreate the appropriate TaskAndEvents and drive the task
            Long taskId = ((TaskRedriverDetails) message).getTaskId();
            SerializedRedriverTask redriverTask = objectMapper.readValue(fluxRuntimeConnector.getSerializedRedriverTaskByTaskId(taskId), SerializedRedriverTask.class);
            TaskAndEvents taskAndEvents = redriverTask.getTaskAndEvents();

            if(taskAndEvents.getRetryCount() > taskAndEvents.getCurrentRetryCount() && isTaskRedrivable(redriverTask.getTaskStatus())) {
                //send message to Task Router to trigger task execution
                logger.info("Redriver Task Worker is redriving a task with Id: {}", taskAndEvents.getTaskId());
                String taskIdentifier = taskAndEvents.getTaskIdentifier();
                int secondUnderscorePosition = taskIdentifier.indexOf('_', taskIdentifier.indexOf('_') + 1);
                String routerName = taskIdentifier.substring(0, secondUnderscorePosition == -1 ? taskIdentifier.length() : secondUnderscorePosition); //the name of router would be classFQN_taskMethodName
                ActorRef router = routerRegistry.getRouter(routerName);
                router.tell(taskAndEvents, ActorRef.noSender());
            }
		}else {
			logger.error("Redriver Task Worker received a message that it cannot process (or) a non-redriver action. Message type received is : {}", message.getClass().getName());
			unhandled(message);
		}
	}

    /**
     * Returns whether a task is redrivable based on it's status.
     */
    private boolean isTaskRedrivable(Status taskStatus) {
        return !(taskStatus.equals(Status.completed) ||
                taskStatus.equals(Status.sidelined) ||
                taskStatus.equals(Status.errored) ||
                taskStatus.equals(Status.cancelled));
    }
}
