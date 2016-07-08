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
 *
 */
package com.flipkart.flux.impl.task;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flipkart.flux.api.core.FluxError;
import com.flipkart.flux.client.runtime.FluxRuntimeConnector;

import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.pattern.Backoff;
import akka.pattern.BackoffSupervisor;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

/**
 * <code>AkkaTaskSupervisor</code> creates a supervisor for {@link AkkaTask} instances. This supervisor uses an Exponential BackOff
 * strategy for retries. Also, this supervisor distinguishes transient errors in executions and performs retries only in such cases.
 * 
 * @author regunath.balasubramanian
 *
 */
public class AkkaTaskSupervisor {
	
	/** Counter to help create unique actor names*/
	private static final AtomicLong INSTANCE_COUNTER = new AtomicLong();
	
	/** Constants for retry interval and backoff */
	private static final FiniteDuration MIN_BACKOFF = FiniteDuration.create(2, TimeUnit.SECONDS);
	private static final FiniteDuration MAX_BACKOFF = FiniteDuration.create(600, TimeUnit.SECONDS);
	
	/** Logger instance for this class*/
	/** Logger for this class*/
	private static final Logger LOGGER = LoggerFactory.getLogger(AkkaTaskSupervisor.class);
    
	/** The Flux Runtime Connector instance for dispatching processed EventS and execution status updates*/
	@Inject
	private static FluxRuntimeConnector fluxRuntimeConnector;
	
	/**
	 * Creates supervisor {@link Props} for {@link AkkaTask} with backoff strategy
	 * @param taskActorName name that identifies the Task of the AkkaTask
	 * @param maxRetries no. of max retries
	 * @return supervisor creation Props
	 */
	public static Props getTaskSupervisorProps (String taskActorName, long maxRetries) {
		final Props childProps = Props.create(AkkaTask.class);
		final Props supervisorProps = 
				BackoffSupervisor.props(Backoff.onStop(   // Backoff is ONLY for recreating the Actor. It does not affect re-delivery of messages 
						    childProps,
						    taskActorName + "-" + INSTANCE_COUNTER.incrementAndGet(),
						    MIN_BACKOFF, MAX_BACKOFF, 0.2) // adds 20% "noise" to vary the intervals slightly
				.withSupervisorStrategy(
						new OneForOneStrategy((int)maxRetries, Duration.Inf(), t -> {
					        if (t instanceof FluxError) {
					        	FluxError fe = (FluxError)t;
					        	if (fe.getType().equals(FluxError.ErrorType.timeout)) {
					        		if (fe.getExecutionContextMeta().getAttemptedNoOfRetries() < fe.getExecutionContextMeta().getMaxRetries()) {
						        		LOGGER.info("Retrying execution of Task. Retry count = {}, Cause = {} ",fe.getExecutionContextMeta().getAttemptedNoOfRetries(), 
						        				fe.getMessage());
						        		return SupervisorStrategy.restart();
					        		} else {
					        			LOGGER.warn("Aborting retries for Task Id : {}. Retry count exceeded : {}", fe.getExecutionContextMeta().getTaskId(), 
					        					fe.getExecutionContextMeta().getAttemptedNoOfRetries());
					        			return SupervisorStrategy.stop();
					        		}
					        	} else { 
					        		return SupervisorStrategy.stop();
					        	}
					        } else if (t instanceof RuntimeException) {
					            return SupervisorStrategy.stop();
					        } else {
					            return SupervisorStrategy.escalate();
					        }
						})));
		return supervisorProps;
	}
	
}
