package com.flipkart.flux.resource;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.FluxRunTimeService;
import com.flipkart.flux.commons.dto.WorkFlowStateSummary;
import com.flipkart.flux.commons.dto.WorkFlowStatesDetail;
import com.flipkart.flux.commons.dto.WorkflowSummary;
import com.google.inject.Inject;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/teams")
public class FluxUIResource {

    private final FluxRunTimeService fluxRunTimeService;
    private ObjectMapper objectMapper;

    @Inject
    public FluxUIResource(FluxRunTimeService fluxRunTimeService,
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
            WorkflowSummary workflowSummary = fluxRunTimeService.getTeamWorkFloWSummary(teamName);
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
            WorkFlowStateSummary workFlowStateSummary = fluxRunTimeService.getWorkflowStateSummary(teamName,
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
    @Path("{teamName}/workflows/{workflowName}/{version}/{state}/summary")
    public Response getStateDetail(@PathParam("teamName") String teamName,
                                   @PathParam("workflowName") String workflowName,
                                   @PathParam("version") String version,
                                   @PathParam("state") String state,
                                   @QueryParam("pageSize") Integer pageSize,
                                   @QueryParam("index") Integer index) {

        try {

            WorkFlowStatesDetail workFlowStatesDetail = fluxRunTimeService.getWorkflowStatesDetail(teamName,
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
