/*
 * Copyright 2012-2016, the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flipkart.flux.guice.module;

import java.net.URISyntaxException;
import java.net.UnknownHostException;

import javax.inject.Named;
import javax.inject.Singleton;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jersey2.InstrumentedResourceMethodApplicationListener;
import com.codahale.metrics.jetty9.InstrumentedHandler;
import com.codahale.metrics.jmx.JmxReporter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.flipkart.flux.filter.CORSFilter;
import com.flipkart.flux.resource.ClientElbResource;
import com.flipkart.flux.resource.StateMachineResource;
import com.flipkart.flux.resource.StatusResource;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

/**
 * <code>ContainerModule</code> is a Guice {@link AbstractModule} implementation used for wiring Flux Orchestration container components.
 *
 * @author regunath.balasubramanian
 * @author kartik.bommepally
 *
 */
public class OrchestratorContainerModule extends AbstractModule {

    @Override
    public void configure(){
    		// nothing to configure
    }

	/**
	 * Creates the Jetty server instance for the Flux API endpoint.
	 * @param port where the service is available.
	 * @return Jetty Server instance
	 */
	@Named("APIJettyServer")
	@Provides
	@Singleton
	Server getAPIJettyServer(@Named("Api.service.port") int port,
							 @Named("APIResourceConfig")ResourceConfig resourceConfig,
							 @Named("Api.service.acceptors") int acceptorThreads,
							 @Named("Api.service.selectors") int selectorThreads,
							 @Named("Api.service.workers") int maxWorkerThreads,
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
        handler.setName("Orchestration-Runtime-Metrics-"); // give a unique name that doesnot conflict with other webapps registered in the same metrics registry
		handler.setHandler(context);
		server.setHandler(handler);

		server.setStopAtShutdown(true);
		return server;
	}

	@Named("APIResourceConfig")
	@Singleton
	@Provides
	public ResourceConfig getAPIResourceConfig(StateMachineResource stateMachineResource,
											   StatusResource statusResource, ClientElbResource clientElbResource,
											   MetricRegistry metricRegistry) {
		ResourceConfig resourceConfig = new ResourceConfig();

		//Register codahale metrics and publish to jmx
		resourceConfig.register(new InstrumentedResourceMethodApplicationListener(metricRegistry));
		JmxReporter jmxReporter = JmxReporter.forRegistry(metricRegistry).build();

		//register resources
		resourceConfig.register(stateMachineResource);
		resourceConfig.register(statusResource);
		resourceConfig.register(clientElbResource);

		resourceConfig.register(CORSFilter.class);
		jmxReporter.start();
		return resourceConfig;
	}

}
