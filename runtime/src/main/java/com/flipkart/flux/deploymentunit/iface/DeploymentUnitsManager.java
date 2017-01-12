package com.flipkart.flux.deploymentunit.iface;

import com.flipkart.flux.deploymentunit.DeploymentUnit;

import java.util.Collection;

/**
 * Interface to manage deploymentUnits
 * Created by gaurav.ashok on 26/11/16.
 */
public interface DeploymentUnitsManager {

    /**
     * Load a deploymentUnit into the memory.
     * @param name Name of the deploymentUnit.
     * @param version Version.
     * @return {@link DeploymentUnit} Loaded deploymentUnit.
     * @throws Exception Throws exception in case of any IOException and any fatal exceptions occurred during loading.
     */
    DeploymentUnit load(String name, Integer version) throws Exception;

    /**
     * Unload a deploymentUnit.
     * @param name Name of the deploymentUnit.
     * @param version Version.
     */
    void unload(String name, Integer version);

    /**
     * Returns a collection of all deploymentUnits that are loaded.
     * @return
     */
    Collection<DeploymentUnit> getAllDeploymentUnits();
}
