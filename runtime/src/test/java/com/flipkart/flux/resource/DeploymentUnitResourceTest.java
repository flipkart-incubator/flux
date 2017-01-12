package com.flipkart.flux.resource;

import com.flipkart.flux.client.model.Task;
import com.flipkart.flux.config.TaskRouterUtil;
import com.flipkart.flux.deploymentunit.DeploymentUnit;
import com.flipkart.flux.deploymentunit.iface.DeploymentUnitsManager;
import com.flipkart.flux.impl.task.registry.RouterRegistry;
import com.flipkart.polyguice.config.YamlConfiguration;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.Response;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.*;

/**
 * @author gaurav.ashok
 */
@RunWith(MockitoJUnitRunner.class)
public class DeploymentUnitResourceTest {

    DeploymentUnitsManager deploymentUnitsManager = mock(DeploymentUnitsManager.class);

    RouterRegistry routerRegistry = mock(RouterRegistry.class);

    int defaultTaskConcurrency = 3;

    DeploymentUnitResource deploymentUnitResource = new DeploymentUnitResource(deploymentUnitsManager, routerRegistry, new TaskRouterUtil(defaultTaskConcurrency));

    @Test
    public void testLoadDeploymentUnit() throws Exception {
        DeploymentUnit unit1 = getMockedDUnit("unit1", 1, "task1", getMethod1());
        DeploymentUnit unit2 = getMockedDUnit("unit2", 1, "task2", getMethod2());
        DeploymentUnit unit1_new = getMockedDUnit("unit1", 2, "task1", getMethod1());
        when(deploymentUnitsManager.getAllDeploymentUnits()).thenReturn(Arrays.asList(unit1, unit2));
        when(deploymentUnitsManager.load("unit1", 2)).thenReturn(unit1_new);

        Response response = deploymentUnitResource.loadDeploymentUnit("unit1", 2, false);

        Assertions.assertThat(response.getStatus()).isEqualTo(200);

        verify(deploymentUnitsManager).load("unit1", 2);
        verify(routerRegistry).createOrResize("com.flipkart.flux.resource.DeploymentUnitResourceTest_testMethod", defaultTaskConcurrency);
        verifyNoMoreInteractions(deploymentUnitsManager);
        verifyNoMoreInteractions(routerRegistry);
    }

    @Test
    public void testLoadDeploymentUnitReplace_shouldUnloadPreviousDeploymentUnit() throws Exception {
        DeploymentUnit unit1 = getMockedDUnit("unit1", 1, "task1", getMethod1());
        DeploymentUnit unit2 = getMockedDUnit("unit2", 1, "task2", getMethod2());
        DeploymentUnit unit1_new = getMockedDUnit("unit1", 2, "task1", getMethod1());
        when(deploymentUnitsManager.getAllDeploymentUnits()).thenReturn(Arrays.asList(unit1, unit2));
        when(deploymentUnitsManager.load("unit1", 2)).thenReturn(unit1_new);

        Response response = deploymentUnitResource.loadDeploymentUnit("unit1", 2, true);

        Assertions.assertThat(response.getStatus()).isEqualTo(200);

        verify(deploymentUnitsManager).load("unit1", 2);
        verify(routerRegistry).createOrResize("com.flipkart.flux.resource.DeploymentUnitResourceTest_testMethod", 3);
        verify(deploymentUnitsManager).getAllDeploymentUnits();
        verify(deploymentUnitsManager).unload("unit1", 1);

        verifyNoMoreInteractions(deploymentUnitsManager);
        verifyNoMoreInteractions(routerRegistry);
    }

    @Test
    public void testUnloadDeploymentUnit_shouldCallUnloadAndRemoveUnusedRouter() throws Exception {
        DeploymentUnit unit1 = getMockedDUnit("unit1", 1, "task1", getMethod1());
        DeploymentUnit unit2 = getMockedDUnit("unit2", 1, "task2", getMethod2());

        // 2 deploymentUnits are loaded.
        when(deploymentUnitsManager.getAllDeploymentUnits()).thenReturn(Arrays.asList(unit1, unit2));

        // unload first one.
        Response response = deploymentUnitResource.unloadDeploymentUnit("unit1", 1);

        Assertions.assertThat(response.getStatus()).isEqualTo(200);

        verify(deploymentUnitsManager).unload("unit1", 1);
        verify(routerRegistry).createOrResize("com.flipkart.flux.resource.DeploymentUnitResourceTest_testMethod", 0);
        verifyNoMoreInteractions(routerRegistry);
    }

    private DeploymentUnit getMockedDUnit(String name, Integer version, String taskId, Method m) throws Exception {
        DeploymentUnit unit = mock(DeploymentUnit.class);
        when(unit.getName()).thenReturn(name);
        when(unit.getVersion()).thenReturn(version);
        when(unit.getTaskMethods()).thenReturn(Collections.singletonMap(taskId, m));
        when(unit.getTaskConfiguration()).thenReturn(new YamlConfiguration(this.getClass().getClassLoader().getResource("flux_config.yml")));
        return unit;
    }

    private Method getMethod1() throws Exception {
        return this.getClass().getMethod("testMethod", String.class);
    }

    private Method getMethod2() throws Exception {
        return this.getClass().getMethod("testMethod2", String.class);
    }

    @Task(version = 1, timeout = 100)
    public String testMethod(String input) {
        return input;
    }

    @Task(version = 1, timeout = 1000)
    public Integer testMethod2(String input) {
        return input != null ? input.length() : 0;
    }
}
