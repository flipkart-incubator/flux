    /*
 * Copyright 2012-2018, the original author or authors.
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

package com.flipkart.flux.guice.module;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.jersey2.InstrumentedResourceMethodApplicationListener;
import com.codahale.metrics.jetty9.InstrumentedHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.flipkart.flux.config.FileLocator;
import com.flipkart.flux.constant.RuntimeConstants;
import com.flipkart.flux.filter.CORSFilter;
import com.flipkart.flux.metrics.MetricsClientImpl;
import com.flipkart.flux.metrics.iface.MetricsClient;
import com.flipkart.flux.resource.DeploymentUnitResource;
import com.flipkart.flux.resource.ExecutionApiResource;
import com.flipkart.flux.resource.StatusResource;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import javax.inject.Named;
import java.io.File;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import static com.flipkart.flux.Constants.METRIC_REGISTRY_NAME;
import static com.flipkart.flux.constant.RuntimeConstants.DASHBOARD_VIEW;

/*
* ExecutionContainer Module prepares execution node FluxRuntime. It prepares api servers and resources at startup.
* */

public class ExecutionContainerModule extends AbstractModule {

    @Override
    public void configure(){
        bind(MetricsClient.class).to(MetricsClientImpl.class).in(Singleton.class);
    }

    @Provides
    public MetricRegistry metricRegistry() {
        return SharedMetricRegistries.getOrCreate(METRIC_REGISTRY_NAME);
    }

    /**
     * Creates a Jetty {@link WebAppContext} for the Execution dashboard
     * @return Jetty WebAppContext
     */
    @Named("ExecutionDashboardContext")
    @Provides
    @javax.inject.Singleton
    WebAppContext getExecutionDashboardWebAppContext() {
        String path = null;
        File[] files = FileLocator.findDirectories("packaged/webapps/dashboard/WEB-INF", null);
        for (File file : files) {
            // we need only WEB-INF from runtime project
            String fileToString = file.toString();
            if (fileToString.contains(".jar!") && fileToString.startsWith("file:/")) {
                fileToString = fileToString.replace("file:/","jar:file:/");
                if (fileToString.contains("runtime-")) {
                    path = fileToString;
                    break;
                }
            } else {
                if (fileToString.contains(DASHBOARD_VIEW)) {
                    path = fileToString;
                    break;
                }
            }
        }
        // trim off the "WEB-INF" part as the WebAppContext path should refer to the parent directory
        if (path.endsWith("WEB-INF")) {
            path = path.replace("WEB-INF", "");
        }
        WebAppContext webAppContext = new WebAppContext(path, RuntimeConstants.DASHBOARD_CONTEXT_PATH);
        return webAppContext;
    }

    /**
     * Creates the Jetty server instance for the admin Dashboard and configures it with the @Named("ExecutionDashboardContext").
     * @param port where the service is available
     * @param acceptorThreads no. of acceptors
     * @param maxWorkerThreads max no. of worker threads
     * @return Jetty Server instance
     */
    @Named("ExecutionDashboardJettyServer")
    @Provides
    @javax.inject.Singleton
    Server getExecutionDashboardJettyServer(@Named("ExecutionDashboard.service.port") int port,
                                   @Named("ExecutionDashboard.service.acceptors") int acceptorThreads,
                                   @Named("ExecutionDashboard.service.selectors") int selectorThreads,
                                   @Named("ExecutionDashboard.service.workers") int maxWorkerThreads,
                                   @Named("ExecutionDashboardContext") WebAppContext webappContext) {
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setMaxThreads(maxWorkerThreads);
        Server server = new Server(threadPool);
        ServerConnector http = new ServerConnector(server, acceptorThreads, selectorThreads);
        http.setPort(port);
        server.addConnector(http);
        server.setHandler(webappContext);
        server.setStopAtShutdown(true);
        return server;
    }

    /**
     * Creates the Jetty server instance for the Flux Execution API endpoint.
     * @return Jetty Server instance
     */
    @Named("ExecutionAPIJettyServer")
    @Provides
    @Singleton
    Server getExecutionAPIJettyServer(@Named("Execution.Node.Api.service.port") int port,
                             @Named("ExecutionAPIResourceConfig")ResourceConfig resourceConfig,
                             @Named("Execution.Node.Api.service.acceptors") int acceptorThreads,
                             @Named("Execution.Node.Api.service.selectors") int selectorThreads,
                             @Named("Execution.Node.Api.service.workers") int maxWorkerThreads,
                             ObjectMapper objectMapper, MetricRegistry metricRegistry) throws URISyntaxException, UnknownHostException {
        JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
        provider.setMapper(objectMapper);
        resourceConfig.register(provider);
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setMaxThreads(maxWorkerThreads);
        Server server = new Server(threadPool);
        ServerConnector http = new ServerConnector(server, acceptorThreads, selectorThreads);
        http.setPort(port);
        server.addConnector(http);
        ServletContextHandler context = new ServletContextHandler(server, "/*");
        ServletHolder servlet = new ServletHolder(new ServletContainer(resourceConfig));
        context.addServlet(servlet, "/*");

        final InstrumentedHandler handler = new InstrumentedHandler(metricRegistry);
        handler.setHandler(context);
        server.setHandler(handler);

        server.setStopAtShutdown(true);
        return server;
    }

    @Named("ExecutionAPIResourceConfig")
    @Provides
    @Singleton
    public ResourceConfig getAPIResourceConfig(ExecutionApiResource executionApiResource, DeploymentUnitResource deploymentUnitResource,
                                               StatusResource statusResource, MetricRegistry metricRegistry) {
        ResourceConfig resourceConfig = new ResourceConfig();

        resourceConfig.register(new InstrumentedResourceMethodApplicationListener(metricRegistry));
        JmxReporter jmxReporter = JmxReporter.forRegistry(metricRegistry).build();

        resourceConfig.register(executionApiResource);
        resourceConfig.register(deploymentUnitResource);
        resourceConfig.register(statusResource);

        resourceConfig.register(CORSFilter.class);
        jmxReporter.start();
        return resourceConfig;
    }

}
