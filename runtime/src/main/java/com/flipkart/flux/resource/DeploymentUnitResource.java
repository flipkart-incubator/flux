package com.flipkart.flux.resource;

import com.flipkart.flux.client.intercept.MethodId;
import com.flipkart.flux.config.TaskRouterUtil;
import com.flipkart.flux.deploymentunit.DeploymentUnit;
import com.flipkart.flux.deploymentunit.iface.DeploymentUnitsManager;
import com.flipkart.flux.impl.task.registry.RouterRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.inject.Named;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
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

        if(StringUtils.isEmpty(name) || version == null) {
            throw new WebApplicationException(buildResponse(Response.Status.BAD_REQUEST, "deploymentUnit name or version is invalid", null));
        }

        DeploymentUnit loadedUnit;
        try {
            loadedUnit = deploymentUnitManager.load(name, version);
            logger.info("Successfully loaded deploymentUnit: " + loadedUnit.getName() + "/" + loadedUnit.getVersion());
        } catch (Exception e) {
            String errMsg = "Could not load deploymentUnit: " + name + (version == null ? "" : "/" + version);
            logger.error(errMsg, e);
            return buildResponse(Response.Status.INTERNAL_SERVER_ERROR, errMsg, e);
        }

        // deploymentUnit is now loaded. create routers for it
        loadedUnit.getTaskMethods().values().stream().forEach(m -> {
            String routerName = new MethodId(m).getPrefix();
            logger.info("Creating router for " + routerName);
            routerRegistry.resize(routerName, taskRouterUtil.getConcurrency(loadedUnit, routerName));
        });

        if(replaceOld) {
            logger.info("Unloading redundant deploymentUnits");

            for(DeploymentUnit unit: getRedundantUnits(loadedUnit)) {
                logger.info("Unloading deploymentUnit: " + unit.getName() + "/" + unit.getVersion());
                deploymentUnitManager.unload(unit.getName(), unit.getVersion());
            }
        }

        return buildResponse(Response.Status.OK, "Successfully loaded deploymentUnit: " + name + "/" + loadedUnit.getVersion(), null);
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
        if(StringUtils.isEmpty(name)) {
            throw new WebApplicationException(buildResponse(Response.Status.BAD_REQUEST, "deploymentUnit name cannot be null", null));
        }
        if(version == null || version < 0) {
            throw new WebApplicationException(buildResponse(Response.Status.BAD_REQUEST, "invalid version", null));
        }

        DeploymentUnit unitToDelete = deploymentUnitManager.getAllDeploymentUnits().stream()
                .filter(d -> d.getName().equals(name) && d.getVersion() == version).findFirst().orElse(null);

        if(unitToDelete == null) {
            throw new WebApplicationException((buildResponse(Response.Status.NOT_FOUND, "deploymentUnit not found", null)));
        }

        // TODO: decide on the basis of the usage metrics of tasks.
        deploymentUnitManager.unload(name, version);

        // get safe-to-remove routers
        Set<String> routersToDelete = taskRouterUtil.getRouterNames(unitToDelete);

        // remove the inUseRouters from the toDelete set
        Set<String> routersInUse = deploymentUnitManager.getAllDeploymentUnits().stream()
                .filter(d -> d.getName().equals(name) && d.getVersion() > version)
                .map(d -> taskRouterUtil.getRouterNames(d))
                .flatMap(Set::stream)
                .distinct()
                .collect(Collectors.toSet());
        routersToDelete.removeAll(routersInUse);

        // TODO: again decide on the basis of current usage metrics.
        for(String routerName: routersToDelete) {
            // shrink it to 0.
            routerRegistry.resize(routerName, 0);
        }

        return buildResponse(Response.Status.OK, "successfully unloaded deploymentUnit: " + name + "/" + version, null);
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
