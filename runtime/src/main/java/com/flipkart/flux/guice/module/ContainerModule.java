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

import static com.flipkart.flux.Constants.METRIC_REGISTRY_NAME;

import java.io.File;

import javax.inject.Named;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.config.FileLocator;
import com.flipkart.flux.constant.RuntimeConstants;
import com.flipkart.flux.metrics.MetricsClientImpl;
import com.flipkart.flux.metrics.iface.MetricsClient;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * An Guice module implementation that offers functionality common to Orchestration and Execution runtime initialization.
 * @author regu.b
 */
public  class ContainerModule extends AbstractModule {

    @Override
    public void configure(){
        bind(MetricsClient.class).to(MetricsClientImpl.class).in(Singleton.class);
    }

    @Provides
    public MetricRegistry metricRegistry() {
        return SharedMetricRegistries.getOrCreate(METRIC_REGISTRY_NAME);
    }
	
	/**
	 * Creates a Jetty {@link WebAppContext} for the Flux dashboard
	 * @return Jetty WebAppContext
	 */
	@Named("DashboardContext")
	@Provides
	@Singleton
	WebAppContext getDashboardWebAppContext() {
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
				if (fileToString.contains("runtime") ) {
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
	 * Creates the Jetty server instance for the admin Dashboard and configures it with the @Named("DashboardContext").
	 * @param port where the service is available
	 * @param acceptorThreads no. of acceptors
	 * @param maxWorkerThreads max no. of worker threads
	 * @return Jetty Server instance
	 */
	@Named("DashboardJettyServer")
	@Provides
	@Singleton
	Server getDashboardJettyServer(@Named("Dashboard.service.port") int port,
			@Named("Dashboard.service.acceptors") int acceptorThreads,
			@Named("Dashboard.service.selectors") int selectorThreads,
			@Named("Dashboard.service.workers") int maxWorkerThreads,
			@Named("DashboardContext") WebAppContext webappContext) {
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

	//may not be the right module class for this. may need to be moved later.
	@Provides
	@Singleton
	ObjectMapper getObjectMapper() {
		return new ObjectMapper();
	}
	
}
