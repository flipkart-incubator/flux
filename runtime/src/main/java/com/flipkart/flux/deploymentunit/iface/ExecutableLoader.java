package com.flipkart.flux.deploymentunit.iface;

import com.flipkart.flux.api.core.FluxError;
import com.flipkart.flux.client.registry.Executable;
import com.flipkart.flux.deploymentunit.DeploymentUnit;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by gaurav.ashok on 26/11/16.
 */
public interface ExecutableLoader {

    Map<String, Executable> loadExecutables(DeploymentUnit deploymentUnit) throws FluxError;
}
