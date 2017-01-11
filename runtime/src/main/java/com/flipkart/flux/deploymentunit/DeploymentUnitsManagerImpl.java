package com.flipkart.flux.deploymentunit;

import com.flipkart.flux.api.core.FluxError;
import com.flipkart.flux.client.registry.Executable;
import com.flipkart.flux.client.registry.ExecutableRegistry;
import com.flipkart.flux.deploymentunit.iface.DeploymentUnitUtil;
import com.flipkart.flux.deploymentunit.iface.DeploymentUnitsManager;
import com.flipkart.flux.deploymentunit.iface.ExecutableLoader;
import com.flipkart.flux.exception.DuplicateDeploymentUnitException;
import com.flipkart.flux.guice.annotation.ManagedEnv;
import com.flipkart.flux.registry.TaskExecutableImpl;
import com.flipkart.polyguice.core.Initializable;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <code>DeploymentUnitsManagerImpl</code> is an implementation of {@link DeploymentUnitsManager} and handles load/unload/list operations on {@link DeploymentUnit}s
 *
 * @author gaurav.ashok
 */
@Singleton
public class DeploymentUnitsManagerImpl implements DeploymentUnitsManager, Initializable {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentUnitsManagerImpl.class);

    /**
     * To Load deploymentUnits
     */
    @Inject
    private DeploymentUnitUtil deploymentUnitUtil;

    /**
     * To Load executables from a deploymentUnits.
     */
    @Inject
    private ExecutableLoader executableLoader;

    /**
     * Executable Registry to hold the executables
     */
    @Inject
    @ManagedEnv
    private ExecutableRegistry executableRegistry;

    /** Cached thread pool to load deploymentUnits in parallel. */
    private ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * Map to hold all loaded deploymentUnits.
     */
    private Map<String, CopyOnWriteArrayList<DeploymentUnit>> deploymentUnitMap = new ConcurrentHashMap<>();

    @Override
    public DeploymentUnit load(String name, Integer version) throws Exception {
        logger.info("LOADING deployment Unit: {}/{}", name, version);
        Path deploymentUnitDir = Paths.get(name, version.toString());

        // get latest
        DeploymentUnit latestUnit = getLatestFromMap(name);
        if(latestUnit != null) {
            if(latestUnit.getVersion().equals(version)) {
                throw new DuplicateDeploymentUnitException("Deployment Unit with name: " + name + " version: " + version + " exists already");
            } else if(latestUnit.getVersion() > version) {
                // dont allow to load an older version, for now.
                throw new FluxError(FluxError.ErrorType.runtime, "Cannot load the deploymentUnit of an older version." +
                        " Latest version: " + latestUnit.getVersion(), null);
            }
        }

        // load deployment unit.
        DeploymentUnit loadedUnit = deploymentUnitUtil.getDeploymentUnit(deploymentUnitDir);

        addToMap(loadedUnit);

        // load executables from it. If unsuccessful undo all the changes
        try {
            Map<String, Executable> loadedExes = executableLoader.loadExecutables(loadedUnit);

            /* register all loaded executables */
            for(String taskId: loadedExes.keySet()) {
                executableRegistry.registerTask(taskId, loadedExes.get(taskId));
            }
        } catch (FluxError fe) {
            unload(loadedUnit.getName(), loadedUnit.getVersion());
            throw fe;
        }

        return loadedUnit;
    }

    @Override
    public void unload(String name, Integer version) {
        logger.warn("UNLOADING Deployment Unit: {}/{}", name, version);

        DeploymentUnit foundUnit = getFromMap(name, version);
        if(foundUnit == null) {
            return;
        }

        //close the deploymentUnit
        foundUnit.close();

        // remove it from map
        removeFromMap(name, version);

        // remove all executables from registry
        for (String taskId : foundUnit.getTaskMethods().keySet()) {
            Executable exe = executableRegistry.getTask(taskId);

            if(exe instanceof TaskExecutableImpl) {
                // if the executable belongs to this classLoader, remove it
                if(((TaskExecutableImpl) exe).getDeploymentUnitClassLoader() == foundUnit.getDeploymentUnitClassLoader()) {
                    executableRegistry.unregisterTask(taskId);
                }
            } else {
                logger.warn("Executable here must be of type TaskExecutableImpl. Found " + exe);
            }
        }
    }

    @Override
    public Collection<DeploymentUnit> getAllDeploymentUnits() {
        return deploymentUnitMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    /**
     * Loads all deployment units listed by {@code deploymentUnitUtil} on Flux bootup.
     */
    @Override
    public void initialize() {
        // get all deployment units list
        List<Path> paths = Collections.EMPTY_LIST;
        try {
            paths = deploymentUnitUtil.listAllDirectoryUnits();
        } catch(IOException e) {
            logger.error("Failed to list all directories of deploymentUnits");
        }

        if(paths == null || paths.isEmpty()) {
            return;
        }

        // load all of them
        List<Future<DeploymentUnit>> unitFutures =  paths.stream().
                map(path -> executorService.submit(() -> deploymentUnitUtil.getDeploymentUnit(path))).collect(Collectors.toList());

        for(int index = 0; index < unitFutures.size(); ++index) {
            try {
                DeploymentUnit deploymentUnit = unitFutures.get(index).get();
                addToMap(deploymentUnit);
            } catch(ExecutionException ee) {
                logger.error("Unexpected error occurred while loading deploymentUnit: ", paths.get(index), ee);
            } catch(InterruptedException ie) {
                logger.error("DeploymentUnit loading got interrupted: {}", paths.get(index), ie);
            }
        }

        // after loading, iterate and create executables from them.
        List<Future> unitsFutures =  deploymentUnitMap.keySet().stream().
                map(name -> executorService.submit(() -> {
                    List<DeploymentUnit> units = deploymentUnitMap.get(name);

                    Map<String, Executable> executableMap = new ConcurrentHashMap<String, Executable>();
                    Boolean loadFailed = false;
                    for (DeploymentUnit unit : units) {
                        if(!loadFailed) {
                            try {
                                Map<String, Executable> loadedExecutables = executableLoader.loadExecutables(unit);
                                executableMap.putAll(loadedExecutables);
                            } catch (FluxError fe) {
                                /* error occurred. skip loading rest of the versions */
                                logger.error("Unexpected error occurred while loading executables from deploymentUnit: {}/{}", unit.getName(), unit.getVersion(), fe);
                                loadFailed = true;
                            }
                        }
                        else {
                            unload(unit.getName(), unit.getVersion());
                        }
                    }

                    /* register all loaded executables */
                    for(String taskId: executableMap.keySet()) {
                        executableRegistry.registerTask(taskId, executableMap.get(taskId));
                    }
                })).collect(Collectors.toList());

        for (Future unitsFuture : unitsFutures) {
            try {
                unitsFuture.get();
            } catch (InterruptedException e) {
                logger.error("Loading executables interrupted.", e);
            } catch (ExecutionException e) {
                logger.error("Unexpected error occurred while loading executables.", e);
            }
        }
    }

    /**
     * add deploymentUnit in the map, keeping the multiple deploymentUnit of same name in the order of their version.
     * @param unit
     */
    private void addToMap(DeploymentUnit unit) {
        logger.debug("adding deployingUnit: {}/{}", unit.getName(), unit.getVersion());
        if(!deploymentUnitMap.containsKey(unit.getName())) {
            deploymentUnitMap.put(unit.getName(), new CopyOnWriteArrayList<>(Arrays.asList(unit)));
            return;
        }

        List<DeploymentUnit> deploymentUnits = deploymentUnitMap.get(unit.getName());
        //create a new sorted list based on version number including the current deployment unit
        List<DeploymentUnit> newSortedList = Stream.concat(deploymentUnits.stream(), Stream.of(unit))
                .sorted((e1, e2) -> Integer.compare(e1.getVersion(), e2.getVersion())).collect(Collectors.toList());

        //save the list
        deploymentUnitMap.put(unit.getName(), new CopyOnWriteArrayList<>(newSortedList));
    }

    private void removeFromMap(String name, Integer version) {
        logger.debug("removing deploymentUnit: {}/{}", name, version);
        List<DeploymentUnit> units = deploymentUnitMap.get(name);
        if(!CollectionUtils.isEmpty(units)) {
            units.removeIf(e -> e.getVersion().equals(version));
        }
    }

    private DeploymentUnit getFromMap(String name, Integer version) {
        List<DeploymentUnit> units = deploymentUnitMap.get(name);
        if(units != null) {
            return units.stream().filter(e -> e.getVersion().equals(version)).findFirst().orElse(null);
        }
        return null;
    }

    private DeploymentUnit getLatestFromMap(String name) {
        List<DeploymentUnit> units = deploymentUnitMap.get(name);
        if(!CollectionUtils.isEmpty(units)) {
            return units.get(units.size() - 1);
        }
        return null;
    }
}
