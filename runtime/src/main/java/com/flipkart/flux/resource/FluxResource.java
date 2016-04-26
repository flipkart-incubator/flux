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


import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.FluxRuntimeService;
import com.flipkart.flux.commons.dto.WorkflowStateSummary;
import com.flipkart.flux.commons.dto.WorkflowStatesDetail;
import com.flipkart.flux.commons.dto.WorkflowSummary;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * The <code>FluxResource</code> class contains http endpoints of flux
 *
 * @author ashish.bhutani
 *
 */
@Path("/api/teams")
@Named
@Singleton
@Slf4j
public class FluxResource {

    private final FluxRuntimeService fluxRunTimeService;
    private ObjectMapper objectMapper;

    @Inject
    public FluxResource(FluxRuntimeService fluxRunTimeService,
                        ObjectMapper objectMapper) {
        this.fluxRunTimeService = fluxRunTimeService;
        this.objectMapper = objectMapper;
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{teamName}/workflows/summary")
    public Response getSummary(@PathParam("teamName") String teamName) {
        try {
            WorkflowSummary workflowSummary = fluxRunTimeService.getTeamWorkfloWSummary(teamName);
            String response = objectMapper.writeValueAsString(workflowSummary);
            return Response.status(Response.Status.OK.getStatusCode()).entity(response).build();
        } catch (Exception e) {
            //todo error code more refined later. as of now, all are 500
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(e.getMessage()).build();
        }
    }


    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{teamName}/workflows/{workflowName}/{version}/{state}/summary")
    public Response getStateSummary(@PathParam("teamName") String teamName,
                                    @PathParam("workflowName") String workflowName,
                                    @PathParam("version") String version,
                                    @PathParam("state") String state) {

        try {
            WorkflowStateSummary workFlowStateSummary = fluxRunTimeService.getWorkflowStateSummary(teamName,
                    workflowName,
                    version,
                    state);
            String response = objectMapper.writeValueAsString(workFlowStateSummary);
            return Response.status(Response.Status.OK.getStatusCode()).entity(response).build();
        } catch (Exception e) {
            //todo error code more refined later. as of now, all are 500
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(e.getMessage()).build();
        }
    }


    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{teamName}/workflows/{workflowName}/{version}/{state}")
    public Response getStateDetail(@PathParam("teamName") String teamName,
                                   @PathParam("workflowName") String workflowName,
                                   @PathParam("version") String version,
                                   @PathParam("state") String state,
                                   @QueryParam("pageSize") Integer pageSize,
                                   @QueryParam("index") Integer index) {

        try {

            WorkflowStatesDetail workFlowStatesDetail = fluxRunTimeService.getWorkflowStatesDetail(teamName,
                    workflowName,
                    version,
                    state,
                    pageSize,
                    index);
            String response = objectMapper.writeValueAsString(workFlowStatesDetail);
            return Response.status(Response.Status.OK.getStatusCode()).entity(response).build();
        } catch (Exception e) {
            //todo error code more refined later. as of now, all are 500
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(e.getMessage()).build();
        }
    }



}
