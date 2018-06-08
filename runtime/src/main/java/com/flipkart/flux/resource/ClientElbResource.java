package com.flipkart.flux.resource;

import com.codahale.metrics.annotation.Timed;
import com.flipkart.flux.api.ClientElbDefinition;
import com.flipkart.flux.domain.ClientElb;
import com.flipkart.flux.representation.ClientElbPersistenceService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

/**
 * API for CRUD operations on Client Cluster ELB details
 * @author akif.khan
 */
@Singleton
@Path("/api/client-elb")
@Named
public class ClientElbResource {

    private static final Logger logger = LoggerFactory.getLogger(ClientElbResource.class);

    private ClientElbPersistenceService clientElbPersistenceService;

    @Inject
    public ClientElbResource(ClientElbPersistenceService clientElbPersistenceService
    ) {
        this.clientElbPersistenceService = clientElbPersistenceService;

    }

    /**
     * Create and persist new client's ELB details viz. ElbURL and ClientId
     *
     * @param clientId Unique Id specific to a Client Cluster
     * @param clientElbUrl Client ELB URL Address
     */
    @POST
    @Path("/create")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    public Response createClientElb(@QueryParam("clientId") String clientId,
                                @QueryParam("clientElbUrl") String clientElbUrl) {
        if(clientElbUrl == null || clientId == null)
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).entity(
                    "ClientElbUrl or ClientId cannot be null").build();
        else {
            try {
                URL verifyingURL = new URL(clientElbUrl);
                verifyingURL.toURI();
                ClientElbDefinition clientElbDefinition = new ClientElbDefinition(clientId, clientElbUrl);
                ClientElb clientElb = clientElbPersistenceService.persistClientElb(
                        clientElbDefinition.getId(), clientElbDefinition);
                return Response.status(Response.Status.CREATED.getStatusCode()).entity(clientElb.getId()).build();
            }
            catch(MalformedURLException ex) {
                logger.error("Malformed URL exception {} {} ", ex.getMessage(), ex.getStackTrace());
                return Response.status(Response.Status.BAD_REQUEST).entity("MalformedURLException").build();
            }
            catch(URISyntaxException ex) {
                logger.error("URI Syntax Exception {} {} ", ex.getMessage(), ex.getStackTrace());
                return Response.status(Response.Status.BAD_REQUEST).entity("URISyntaxException").build();
            }
            catch(Exception ex) {
                logger.error("Exception occured in ClientElb create {} {} ", ex.getMessage(), ex.getStackTrace());
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }
    }

    /**
     * Query and returns ClientElbUrl identified by input @param clientId
     *
     * @param clientId Unique Id specific to a Client Cluster
     *
     */
    @GET
    @Path("/findById")
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    public Response findByIdClientElb(@QueryParam("clientId") String clientId) {

        if(clientId == null) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).entity(
                    "ClientId cannot be null").build();
        }

        try {
            ClientElb clientElb = clientElbPersistenceService.findByIdClientElb(clientId);
            if (clientElb == null) {
                return Response.status(Response.Status.NOT_FOUND.getStatusCode())
                        .entity("ClientElb with Id: " + clientId + " not found").build();
            }
            return Response.status(Response.Status.FOUND.getStatusCode()).entity(clientElb.getElbUrl()).build();
        }
        catch(Exception ex) {
            logger.error("findById failed for input: "+ clientId + " {} {} ", ex.getMessage(), ex.getStackTrace());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update and persist existing client's ELB details viz. ElbURL and ClientId
     *
     * @param clientId Unique Id specific to a Client Cluster
     * @param clientElbUrl Elb Url that's supposed to be updated.
     */
    @POST
    @Path("/update")
    @Timed
    public Response updateClientElb(@QueryParam("clientId") String clientId,
                                    @QueryParam("clientElbUrl") String clientElbUrl) {

        if(clientId == null || clientElbUrl == null) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).entity(
                    "ClientId OR ClientElbUrl cannot be null").build();
        }
        try {
            URL verifyingURL = new URL(clientElbUrl);
            verifyingURL.toURI();
            clientElbPersistenceService.updateClientElb(clientId, clientElbUrl);
        }
        catch(MalformedURLException ex) {
            logger.error("Malformed URL exception {} {} ", ex.getMessage(), ex.getStackTrace());
            return Response.status(Response.Status.BAD_REQUEST).entity("MalformedURLException").build();
        }
        catch(URISyntaxException ex) {
            logger.error("URI Syntax Exception {} {} ", ex.getMessage(), ex.getStackTrace());
            return Response.status(Response.Status.BAD_REQUEST).entity("URISyntaxException").build();
        }
        catch(Exception ex) {
            logger.error("Update failed for input: "+ clientId + " " + clientElbUrl + " {} {} "
                    , ex.getMessage(), ex.getStackTrace());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.status(Response.Status.ACCEPTED).build();
    }

    /**
     * Delete client's ELB details viz. ElbURL and ClientId
     *
     * @param clientId Unique Id specific to a Client Cluster
     *
     */
    @POST
    @Path("/delete")
    @Timed
    public Response deleteClientElb(@QueryParam("clientId") String clientId) {

        if(clientId == null) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).entity(
                    "ClientId cannot be null").build();
        }

        try {
            clientElbPersistenceService.deleteClientElb(clientId);
        }
        catch(Exception ex) {
            logger.error("Deletion failed for input: "+ clientId + " {} {} ", ex.getMessage(), ex.getStackTrace());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.status(Response.Status.ACCEPTED).build();
    }
}
