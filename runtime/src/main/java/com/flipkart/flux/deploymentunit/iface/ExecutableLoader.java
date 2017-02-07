package com.flipkart.flux.deploymentunit.iface;

import com.flipkart.flux.api.core.FluxError;
import com.flipkart.flux.client.registry.Executable;
import com.flipkart.flux.deploymentunit.DeploymentUnit;

import java.util.Map;

/**
 * <code>ExecutableLoader</code> reads the deployment units and puts the methods
 * which are annotated with {@link com.flipkart.flux.client.model.Task} in Executable Registry for the later execution.
 * Created by gaurav.ashok on 26/11/16.
 */
public interface ExecutableLoader {

    Map<String, Executable> loadExecutables(DeploymentUnit deploymentUnit) throws FluxError;
}
