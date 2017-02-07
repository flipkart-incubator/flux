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

import akka.actor.Address;
import akka.cluster.Cluster;
import com.flipkart.flux.impl.boot.ActorSystemManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.springframework.util.StringUtils;

import javax.inject.Named;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <code>StatusResource</code> indicates the running status of Flux app.
 * @author shyam.akirala
 * @author gaurav.ashok
 */
@Path("/status")
@Named
@Singleton
public class StatusResource {

    /**
     * Boolean to indicate whether the service is in rotation or not.
     */
    private AtomicBoolean inRotation = new AtomicBoolean(false);

    private ActorSystemManager actorSystemManager;

    @Inject
    public StatusResource(ActorSystemManager actorSystemManager) {
        this.actorSystemManager = actorSystemManager;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRotationStatus() {
        boolean rotationStatus = inRotation.get();

        Map<String, String> response = new HashMap<>();
        response.put("status", rotationStatus ? "up" : "oor");

        int responseCode = (rotationStatus ? Response.Status.OK : Response.Status.NOT_FOUND).getStatusCode();
        return Response.status(responseCode).entity(response).build();
    }

    @POST
    @Path("/oor")
    public void outOfRotation() {
        inRotation.set(false);
    }

    @POST
    @Path("/bir")
    public void bringInRotation() {
        inRotation.set(true);
    }

    /**
     * Api to make a node leave the cluster.
     * @param host hostname/ip of the node.
     * @param port port
     */
    @POST
    @Path("/cluster/leave")
    public Response leaveCluster(@QueryParam("host") String host, @QueryParam("port") Integer port) {
        if(StringUtils.isEmpty(host) || port == null) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).entity("empty hostname or port").build();
        }

        Address akkAddress = new Address("akka.tcp", actorSystemManager.retrieveActorSystem().name(), host, port);
        Cluster cluster = Cluster.get(actorSystemManager.retrieveActorSystem());
        cluster.leave(akkAddress);

        return Response.status(Response.Status.OK.getStatusCode()).build();
    }
}
