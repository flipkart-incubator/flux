package com.flipkart.flux.deploymentunit;

import com.flipkart.flux.client.FluxClientInterceptorModule;
import com.flipkart.flux.client.registry.ExecutableRegistry;
import com.flipkart.flux.deploymentunit.iface.DeploymentUnitsManager;
import com.flipkart.flux.guice.annotation.ManagedEnv;
import com.flipkart.flux.guice.module.AkkaModule;
import com.flipkart.flux.guice.module.ContainerModule;
import com.flipkart.flux.guice.module.HibernateModule;
import com.flipkart.flux.impl.boot.TaskModule;
import com.flipkart.flux.module.DeploymentUnitTestModule;
import com.flipkart.flux.module.RuntimeTestModule;
import com.flipkart.flux.registry.TaskNotFoundException;
import com.flipkart.flux.runner.GuiceJunit4Runner;
import com.flipkart.flux.runner.Modules;
import com.google.inject.Inject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author gaurav.ashok
 */
@RunWith(GuiceJunit4Runner.class)
@Modules({DeploymentUnitTestModule.class,HibernateModule.class,RuntimeTestModule.class,ContainerModule.class,AkkaModule.class,TaskModule.class,FluxClientInterceptorModule.class})
public class DynamicLoadUnloadTest {

    @Inject
    DeploymentUnitsManager dUnitManager;

    @Inject
    @ManagedEnv
    ExecutableRegistry registry;

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
