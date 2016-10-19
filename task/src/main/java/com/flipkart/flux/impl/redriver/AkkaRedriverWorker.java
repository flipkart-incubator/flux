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

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.flipkart.flux.client.runtime.FluxRuntimeConnector;
import com.flipkart.flux.impl.message.TaskRedriverDetails;

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

	/**
	 * Actor call back method for executing stalled Tasks 
	 * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
	 */
	public void onReceive(Object message) throws Exception {
		if (message instanceof TaskRedriverDetails && 
				((TaskRedriverDetails)message).getAction().equals(TaskRedriverDetails.RegisterAction.Redrive)) {
			// Posts to flux runtime to redrive the task
            Long taskId = ((TaskRedriverDetails) message).getTaskId();
            fluxRuntimeConnector.redriveTask(taskId);

		}else {
			logger.error("Redriver Task Worker received a message that it cannot process (or) a non-redriver action. Message type received is : {}", message.getClass().getName());
			unhandled(message);
		}
	}

}
