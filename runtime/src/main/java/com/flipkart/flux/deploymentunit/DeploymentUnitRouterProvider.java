/*
 * Copyright 2012-2016, the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flipkart.flux.deploymentunit;

import akka.cluster.routing.ClusterRouterPoolSettings;
import com.flipkart.flux.constant.RuntimeConstants;
import com.flipkart.flux.impl.task.registry.ConfigurationException;
import com.flipkart.flux.impl.task.registry.RouterConfigurationRegistry;
import com.flipkart.polyguice.config.YamlConfiguration;
import com.flipkart.polyguice.core.ConfigurationProvider;
import com.flipkart.polyguice.core.Initializable;
import javafx.util.Pair;
import scala.Option;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Creates {@link akka.routing.Router} for deployment units.
 * @author shyam.akirala
 */
@Singleton
public class DeploymentUnitRouterProvider implements RouterConfigurationRegistry, Initializable {

    ConfigurationProvider configurationProvider;

    private Integer defaultTotalWFInstances;
    private Integer defaultMaxWFInstancesPerNode;

    private Integer defaultTotalTaskInstances;
    private Integer defaultMaxTaskInstancesPerNode;

    private Integer defaultTotalHookInstances;
    private Integer defaultMaxHookInstancesPerNode;

    @Inject
    public DeploymentUnitRouterProvider(ConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
    }

    @Override
    public Iterable<Pair<String, ClusterRouterPoolSettings>> getConfigurations() {

        Set<Pair<String,ClusterRouterPoolSettings>> routerSettings = new HashSet<>();

        try {
            List<String> deploymentUnitNames = DeploymentUnitUtil.getAllDeploymentUnits();
            if (deploymentUnitNames != null) {
                for(String deploymentUnitName : deploymentUnitNames) {
                    routerSettings.addAll(getDURouterConfig(deploymentUnitName));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to create deployment unit routers. Exception: "+e.getMessage());
        }

        return routerSettings;
    }

    public Set<Pair<String, ClusterRouterPoolSettings>> getDURouterConfig(String deploymentUnitName) throws IOException, ClassNotFoundException {

        Set<Pair<String,ClusterRouterPoolSettings>> routerSettings = new HashSet<>();

        URLClassLoader classLoader = DeploymentUnitUtil.getClassLoader(RuntimeConstants.DEPLOYMENT_UNIT_PATH + deploymentUnitName);
        YamlConfiguration configuration = DeploymentUnitUtil.getProperties(classLoader);

        Integer totalWFInstances = (Integer) configuration.getProperty("AkkaConfig.totalWFInstances");
        if (totalWFInstances == null) {
            totalWFInstances = this.defaultTotalWFInstances;
        }

        Integer maxWFInstancesPerNode = (Integer) configuration.getProperty("AkkaConfig.maxWFInstancesPerNode");
        if (maxWFInstancesPerNode == null) {
            maxWFInstancesPerNode = this.defaultMaxWFInstancesPerNode;
        }

        Integer totalTaskInstances = (Integer) configuration.getProperty("AkkaConfig.totalTaskInstances");
        if (totalTaskInstances == null) {
            totalTaskInstances = this.defaultTotalTaskInstances;
        }

        Integer maxTaskInstancesPerNode = (Integer) configuration.getProperty("AkkaConfig.maxTaskInstancesPerNode");
        if (maxTaskInstancesPerNode == null) {
            maxTaskInstancesPerNode = this.defaultMaxTaskInstancesPerNode;
        }

        Integer totalHookInstances = (Integer) configuration.getProperty("AkkaConfig.totalHookInstances");
        if (totalHookInstances == null) {
            totalHookInstances = this.defaultTotalHookInstances;
        }

        Integer maxHookInstancesPerNode = (Integer) configuration.getProperty("AkkaConfig.maxHookInstancesPerNode");
        if (maxHookInstancesPerNode == null) {
            maxHookInstancesPerNode = this.defaultMaxHookInstancesPerNode;
        }

        //get workflow methods and create routers for workflows and hooks
        Set<Method> workflowMethods = DeploymentUnitUtil.getWorkflowMethods(classLoader);
        for (Method method : workflowMethods) {
            routerSettings.add(new Pair<>(method.getName() + RuntimeConstants.ROUTER_SUFFIX, new ClusterRouterPoolSettings(totalWFInstances, maxWFInstancesPerNode, true, Option.empty())));
            routerSettings.add(new Pair<>(method.getName() + RuntimeConstants.HOOK_ROUTER_SUFFIX, new ClusterRouterPoolSettings(totalHookInstances, maxHookInstancesPerNode, true, Option.empty())));
        }

        //get task methods and create routers //TODO: add SM name as prefix
        Set<Method> taskMethods = DeploymentUnitUtil.getTaskMethods(classLoader);
        for (Method method : taskMethods) {
            routerSettings.add(new Pair<>(method.getName() + RuntimeConstants.ROUTER_SUFFIX, new ClusterRouterPoolSettings(totalTaskInstances, maxTaskInstancesPerNode, true, Option.empty())));
        }

        return routerSettings;
    }

    @Override
    public void initialize() {
        this.defaultTotalWFInstances = (Integer) this.configurationProvider.getValue("routers.default.totalWFInstances",Integer.class);
        this.defaultMaxWFInstancesPerNode = (Integer) this.configurationProvider.getValue("routers.default.maxWFInstancesPerNode",Integer.class);

        this.defaultTotalTaskInstances = (Integer) this.configurationProvider.getValue("routers.default.totalTaskInstances",Integer.class);
        this.defaultMaxTaskInstancesPerNode = (Integer) this.configurationProvider.getValue("routers.default.maxTaskInstancesPerNode",Integer.class);

        this.defaultTotalHookInstances = (Integer) this.configurationProvider.getValue("routers.default.totalHookInstances",Integer.class);
        this.defaultMaxHookInstancesPerNode = (Integer) this.configurationProvider.getValue("routers.default.maxHookInstancesPerNode",Integer.class);

        if (this.defaultTotalWFInstances == null ||
                this.defaultMaxWFInstancesPerNode == null ||
                this.defaultTotalTaskInstances == null ||
                this.defaultMaxTaskInstancesPerNode == null ||
                this.defaultTotalHookInstances == null ||
                this.defaultMaxHookInstancesPerNode == null) {
            throw new ConfigurationException("Could not find default deployment unit router configurations!!");
        }
    }
}
