package com.flipkart.flux.deploymentunit.iface;

import com.flipkart.flux.deploymentunit.DeploymentUnit;

import java.util.Collection;

/**
 * Created by gaurav.ashok on 26/11/16.
 */
public interface DeploymentUnitsManager {

    DeploymentUnit load(String name, Integer version) throws Exception;

    void unload(String name, Integer version);

    Collection<DeploymentUnit> getAllDeploymentUnits();
}
