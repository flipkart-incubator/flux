package com.flipkart.flux.resource;

import static com.flipkart.flux.constant.RuntimeConstants.DEFAULT_ELB_ID;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;

import com.codahale.metrics.annotation.Timed;
import com.flipkart.flux.api.ClientElbDefinition;
import com.flipkart.flux.domain.ClientElb;
import com.flipkart.flux.representation.ClientElbPersistenceService;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * API for CRUD operations on Client Cluster ELB details
 * @author akif.khan
 */
@Singleton
@Path("/api/client-elb")
@Named
public class ClientElbResource {

    private static final Logger logger = LogManager.getLogger(ClientElbResource.class);

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
                if(verifyingURL.getHost().length() < 1 || verifyingURL.getPath().length() > 0) {
                    throw new MalformedURLException();
                }
                ClientElbDefinition clientElbDefinition = new ClientElbDefinition(clientId, clientElbUrl);
                ClientElb clientElb = clientElbPersistenceService.persistClientElb(
                        clientElbDefinition.getId(), clientElbDefinition);
                return Response.status(Response.Status.CREATED.getStatusCode()).entity(clientElb.getId()).build();
            }
            catch(MalformedURLException ex) {
                logger.error("Malformed URL exception(no path allowed because Elb Url doesn't contain path) {} {} ",
                        ex.getMessage(), ex.getStackTrace());
                return Response.status(Response.Status.BAD_REQUEST).entity("MalformedURLException").build();
            }
            catch(URISyntaxException ex) {
                logger.error("URI Syntax Exception {} {} ", ex.getMessage(), ex.getStackTrace());
                return Response.status(Response.Status.BAD_REQUEST).entity("URISyntaxException").build();
            }
            catch (ConstraintViolationException ex) {
                //In case of Duplicate clientId, return http code 409 conflict
                if (ex.getCause() != null) {
                    if (ex.getCause().getMessage().toLowerCase().contains("duplicate entry")) {
                        return Response.status(Response.Status.CONFLICT.getStatusCode()).entity(
                            ex.getCause().getMessage()).build();

                    }
                }
                logger.error("Constraint Violation during creating ClientElb entry" +
                        " with id {} {} {}", clientId, ex.getCause().getMessage(),
                    ex.getStackTrace());
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(
                    ex.getCause() != null ? ex.getCause().getMessage() : null).build();

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
            String clientElbUrl = clientElbPersistenceService.findByIdClientElb(clientId);
            if (clientElbUrl == null) {
                return Response.status(Response.Status.NOT_FOUND.getStatusCode())
                        .entity("ClientElb with Id: " + clientId + " not found").build();
            }
            return Response.status(Response.Status.FOUND.getStatusCode()).entity(clientElbUrl).build();
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
            if(verifyingURL.getHost().length() < 1 || verifyingURL.getPath().length() > 0) {
                throw new MalformedURLException();
            }
            if(clientId == DEFAULT_ELB_ID) {
                return Response.status(Status.FORBIDDEN).entity("Cannot update reserved clientId: " +
                    clientId).build();
            }
            clientElbPersistenceService.updateClientElb(clientId, clientElbUrl);
        }
        catch(MalformedURLException ex) {
            logger.error("Malformed URL exception(no path allowed because Elb Url doesn't contain path) {} {} ",
                    ex.getMessage(), ex.getStackTrace());
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

        if(clientId == DEFAULT_ELB_ID) {
            return Response.status(Status.FORBIDDEN).entity("Cannot delete reserved clientId: " +
                clientId).build();
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