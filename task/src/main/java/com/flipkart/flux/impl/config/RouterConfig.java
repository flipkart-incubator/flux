package com.flipkart.flux.impl.config;

public class RouterConfig {
    Integer totalInstances;
    Integer maxInstancesPerNode;

    public Integer getMaxInstancesPerNode() {
        return maxInstancesPerNode;
    }

    public void setMaxInstancesPerNode(Integer maxInstancesPerNode) {
        this.maxInstancesPerNode = maxInstancesPerNode;
    }

    public Integer getTotalInstances() {
        return totalInstances;
    }

    public void setTotalInstances(Integer totalInstances) {
        this.totalInstances = totalInstances;
    }
}
