package com.flipkart.flux.config;

import com.flipkart.flux.client.intercept.MethodId;
import com.flipkart.flux.deploymentunit.DeploymentUnit;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.inject.Named;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class to bundle frequently used methods.
 * @author gaurav.ashok
 */
@Singleton
public class TaskRouterUtil {

    private int defaultNoOfActors;

    @Inject
    public TaskRouterUtil(@Named("routers.default.instancesPerNode") int defaultNoOfActors) {
        this.defaultNoOfActors = defaultNoOfActors;
    }

    public String getRouterName(Method method) {
        return new MethodId(method).getPrefix();
    }

    public Set<String> getRouterNames(DeploymentUnit deploymentUnit) {
        return deploymentUnit.getTaskMethods().values().stream().map(m -> getRouterName(m)).collect(Collectors.toSet());
    }

    public Integer getConcurrency(DeploymentUnit deploymentUnit, String routerName) {
        return Optional.ofNullable((Integer) deploymentUnit.getTaskConfiguration().getProperty(routerName + ".executionConcurrency"))
                .orElse(defaultNoOfActors);
    }
}
