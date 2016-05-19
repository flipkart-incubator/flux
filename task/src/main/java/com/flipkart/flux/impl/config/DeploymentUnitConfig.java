package com.flipkart.flux.impl.config;

import java.util.Map;

public class DeploymentUnitConfig {
    private Map<String,RouterConfig> routers;

    public Map<String, RouterConfig> getRouters() {
        return routers;
    }

    public void setRouters(Map<String, RouterConfig> routers) {
        this.routers = routers;
    }
}
