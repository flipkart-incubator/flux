package com.flipkart.flux.deploymentunit;

import com.flipkart.flux.FluxRuntimeRole;
import com.flipkart.flux.InjectFromRole;
import com.flipkart.flux.deploymentunit.iface.DeploymentUnitsManager;
import com.flipkart.flux.guice.module.*;
import com.flipkart.flux.module.DeploymentUnitTestModule;
import com.flipkart.flux.registry.TaskExecutableRegistryImpl;
import com.flipkart.flux.registry.TaskNotFoundException;
import com.flipkart.flux.runner.GuiceJunit4Runner;
import com.flipkart.flux.runner.Modules;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author gaurav.ashok
 */
@RunWith(GuiceJunit4Runner.class)
@Modules(executionModules = {DeploymentUnitTestModule.class , ExecutionTaskModule.class, AkkaModule.class, ExecutionContainerModule.class})
public class DynamicLoadUnloadTest {

    @InjectFromRole(value = FluxRuntimeRole.EXECUTION)
    DeploymentUnitsManager dUnitManager;

    @InjectFromRole(value = FluxRuntimeRole.EXECUTION)
    TaskExecutableRegistryImpl registry;

    @Test
    public void testDynamicLoad() {
        try {
            dUnitManager.load("dynamic1", 1);
        } catch(Exception e) {
            Assert.fail(e.getMessage());
        }

        assertThat(dUnitManager.getAllDeploymentUnits()).hasSize(2);

        assertThat(registry.getTask("com.flipkart.flux.deploymentunit.TestWorkflow2_testTask_void_version1")).isNotNull();

        // try unloading
        dUnitManager.unload("dynamic1", 1);
        assertThat(StoppableImpl.stopCounter).isEqualTo(1);
        assertThat(dUnitManager.getAllDeploymentUnits()).hasSize(1);

        Throwable taskNotFoundErr = null;
        try {
            registry.getTask("com.flipkart.flux.deploymentunit.TestWorkflow2_testTask_void_version1");
        } catch(TaskNotFoundException te) {
            taskNotFoundErr = te;
        }

        assertThat(taskNotFoundErr).isNotNull();
    }
}
