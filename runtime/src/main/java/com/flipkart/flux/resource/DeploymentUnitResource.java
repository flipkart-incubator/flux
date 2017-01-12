package com.flipkart.flux.resource;

import com.flipkart.flux.client.intercept.MethodId;
import com.flipkart.flux.config.TaskRouterUtil;
import com.flipkart.flux.deploymentunit.DeploymentUnit;
import com.flipkart.flux.deploymentunit.iface.DeploymentUnitsManager;
import com.flipkart.flux.exception.DuplicateDeploymentUnitException;
import com.flipkart.flux.impl.task.registry.RouterRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Management APIs to load/unload deployment units
 * @author gaurav.ashok
 */
@Path("/api/deployment")
@Named
@Singleton
public class DeploymentUnitResource {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentUnitResource.class);

    /**
     * Manager instance to load deploymentUnit.
     */
    private DeploymentUnitsManager deploymentUnitManager;

    /**
     * Router registry to create new routers.
     */
    private RouterRegistry routerRegistry;

    /**
     * Utility to commonly required functionality.
     */
    private TaskRouterUtil taskRouterUtil;

    @Inject
    public DeploymentUnitResource(DeploymentUnitsManager deploymentUnitsManager, RouterRegistry routerRegistry,
                                  TaskRouterUtil taskRouterUtil) {
        this.deploymentUnitManager = deploymentUnitsManager;
        this.routerRegistry = routerRegistry;
        this.taskRouterUtil = taskRouterUtil;
    }

    /**
     * API to load deploymentUnit in memory and to get it ready so that it can process tasks.
     * @param name Name of the deplomyentUnit.
     * @param version Version.
     * @return message for success or failure.
     */
    @POST
    @Path("/load")
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadDeploymentUnit(@QueryParam("name") String name, @QueryParam("version") Integer version,
                                       @QueryParam("replace") @DefaultValue("false") Boolean replaceOld) {

        if(name == null || name.length() == 0 || version == null || version < 0) {
            throw new WebApplicationException(buildResponse(Response.Status.BAD_REQUEST, "deploymentUnit name or version is invalid"));
        }

        DeploymentUnit loadedUnit;
        try {
            loadedUnit = deploymentUnitManager.load(name, version);
            logger.info("Successfully loaded deploymentUnit: " + loadedUnit.getName() + "/" + loadedUnit.getVersion());
        } catch (DuplicateDeploymentUnitException e) {
            logger.error("Received request to load Deployment unit with name: " + name + " and version: " + version + ", which is already loaded. Discarding it.");
            return buildResponse(Response.Status.CONFLICT, e.getMessage());
        } catch (Exception e) {
            String errMsg = "Could not load deploymentUnit: " + name + "/" + version;
            logger.error(errMsg, e);
            return buildResponse(Response.Status.INTERNAL_SERVER_ERROR, errMsg, e);
        }

        // deploymentUnit is now loaded. create routers for all tasks
        loadedUnit.getTaskMethods().values().stream().forEach(m -> {
            String routerName = new MethodId(m).getPrefix();
            routerRegistry.createOrResize(routerName, taskRouterUtil.getConcurrency(loadedUnit, routerName));
        });

        if(replaceOld) {
            logger.info("Unloading redundant deploymentUnits");

            for(DeploymentUnit unit: getRedundantUnits(loadedUnit)) {
                logger.info("Unloading deploymentUnit: " + unit.getName() + "/" + unit.getVersion());
                deploymentUnitManager.unload(unit.getName(), unit.getVersion());
            }
        }

        return buildResponse(Response.Status.OK, "Successfully loaded deploymentUnit: " + name + "/" + loadedUnit.getVersion());
    }

    /**
     * Unloads a deploymentUnit identified by name and version releasing all acquired resources.
     * @param name Name of deploymentUnit.
     * @param version Version.
     */
    @POST
    @Path("/unload")
    @Produces(MediaType.APPLICATION_JSON)
    public Response unloadDeploymentUnit(@QueryParam("name") String name, @QueryParam("version") Integer version) {

        if(name == null || name.length() == 0 || version == null || version < 0) {
            throw new WebApplicationException(buildResponse(Response.Status.BAD_REQUEST, "deploymentUnit name or version is invalid"));
        }

        DeploymentUnit unitToDelete = deploymentUnitManager.getAllDeploymentUnits().stream()
                .filter(d -> d.getName().equals(name) && d.getVersion() == version).findFirst().orElse(null);

        if(unitToDelete == null) {
            throw new WebApplicationException((buildResponse(Response.Status.NOT_FOUND, "deploymentUnit not found")));
        }

        // TODO: decide on the basis of the usage metrics of tasks.
        deploymentUnitManager.unload(name, version);

        // get safe-to-remove routers
        Set<String> routersToDelete = taskRouterUtil.getRouterNames(unitToDelete);

        // remove the inUseRouters from the toDelete set
        // currently we are checking for all the routers except this deployment unit routers. //todo Decide should we allow unloading only the least numbered DU or anything.
        Set<String> routersInUse = deploymentUnitManager.getAllDeploymentUnits().stream()
                .filter(d -> d.getName().equals(name) && d.getVersion() != version)
                .map(d -> taskRouterUtil.getRouterNames(d))
                .flatMap(Set::stream)
                .distinct()
                .collect(Collectors.toSet());
        routersToDelete.removeAll(routersInUse);

        // TODO: again decide on the basis of current usage metrics.
        for(String routerName: routersToDelete) {
            // shrink it to 0.
            routerRegistry.createOrResize(routerName, 0);
        }

        return buildResponse(Response.Status.OK, "successfully unloaded deploymentUnit: " + name + "/" + version);
    }

    /**
     * Returns a list of previously loaded deploymentUnits which can be safely removed.
     * @param loadedUnit
     * @return
     */
    private List<DeploymentUnit> getRedundantUnits(final DeploymentUnit loadedUnit) {
        int loadedVersion = loadedUnit.getVersion();
        Set<String> tasksNames = loadedUnit.getTaskMethods().keySet();

        List<DeploymentUnit> olderUnits = deploymentUnitManager.getAllDeploymentUnits().stream()
                .filter(d -> d.getName().equals(loadedUnit.getName()) && d.getVersion() < loadedVersion).collect(Collectors.toList());

        return olderUnits.stream().filter(o -> tasksNames.containsAll(o.getTaskMethods().keySet())).collect(Collectors.toList());
    }

    /**
     * Wrapper on {@link #buildResponse(Response.Status, String, Throwable)} which builds a response object from the status code, msg.
     * @param status Http response code.
     * @param msg Accompanying message as a response.
     * @return {@link Response}
     */
    private Response buildResponse(Response.Status status, String msg) {
        return buildResponse(status, msg, null);
    }

    /**
     * Builds a response object from the status code, msg and an optional throwable in case of error.
     * @param status Http response code.
     * @param msg Accompanying message as a response.
     * @param e Optional throwable in case of error.
     * @return {@link Response}
     */
    private Response buildResponse(Response.Status status, String msg, Throwable e) {
        Map<String, String> response = new HashMap<>();
        response.put("msg", msg);
        if(e != null) {
            response.put("error", e.getMessage());
        }
        return Response.status(status).entity(response).build();
    }
}
