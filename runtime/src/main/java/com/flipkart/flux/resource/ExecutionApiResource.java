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

import akka.actor.ActorRef;
import com.codahale.metrics.annotation.Timed;
import com.flipkart.flux.api.core.TaskExecutionMessage;
import com.flipkart.flux.impl.message.TaskAndEvents;
import com.flipkart.flux.impl.task.registry.RouterRegistry;
import com.flipkart.flux.metrics.iface.MetricsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Singleton
@Path("/api/execution")
@Named
public class ExecutionApiResource {
    private static final Logger logger = LoggerFactory.getLogger(ExecutionApiResource.class);

    private RouterRegistry routerRegistry;
    private MetricsClient metricsClient;

    @Inject
    public ExecutionApiResource(RouterRegistry routerRegistry, MetricsClient metricsClient) {
        this.routerRegistry = routerRegistry;
        this.metricsClient = metricsClient;
    }


    @POST
    @Timed
    @Consumes(MediaType.APPLICATION_JSON)
    public Response receiveTaskAndExecutionData(TaskExecutionMessage taskExecutionMessage) {
        TaskAndEvents msg = taskExecutionMessage.getAkkaMessage();
        String routerName = taskExecutionMessage.getRouterName();
        logger.info("Received taskExecutionMessage for stateMachine {} taskId {} taskName {}",
                msg.getStateMachineId(), msg.getTaskId(), msg.getTaskName());
        try {
            ActorRef router = routerRegistry.getRouter(routerName);
            if (router != null) {
                logger.info("Sending msg to router: {} to execute state machine: {} task: {}", router.path(), msg.getStateMachineId(), msg.getTaskId());
                router.tell(msg, ActorRef.noSender());
                metricsClient.incCounter(new StringBuilder().
                        append("stateMachine.").
                        append(msg.getStateMachineName()).
                        append(".task.").
                        append(msg.getTaskName()).
                        append(".queueSize").toString());
            } else {
                logger.error("Corresponding router {} for the execution message not found", taskExecutionMessage.getRouterName());
                return Response.status(Response.Status.NOT_FOUND).entity("Akka router for this executionMessage not found").build();
            }
        } catch (Exception ex) {
            logger.error("Unable to append the task to the Actor Queue {}", ex);
            return Response.serverError().entity(ex.getCause() != null ? ex.getCause().getMessage() : null).build();
        }
        return Response.accepted().build();
    }

}
