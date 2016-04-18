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

import static com.flipkart.flux.constant.RuntimeConstants.API_CONTEXT_PATH;
import static com.flipkart.flux.constant.RuntimeConstants.DASHBOARD_CONTEXT_PATH;
import static com.flipkart.flux.constant.RuntimeConstants.DASHBOARD_VIEW;

import java.io.File;
import java.util.EnumSet;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

import com.flipkart.flux.config.FileLocator;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.servlet.GuiceFilter;

/**
 * <code>ContainerModule</code> is a Guice {@link AbstractModule} implementation used for wiring Flux container components.
 * 
 * @author regunath.balasubramanian
 * @author kartik.bommepally
 *
 */
public class ContainerModule extends AbstractModule {

	/**
	 * Performs concrete bindings for interfaces
	 * @see com.google.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
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
		File[] files = FileLocator.findDirectories("WEB-INF", null);
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
		WebAppContext webAppContext = new WebAppContext(path, DASHBOARD_CONTEXT_PATH);
		return webAppContext;
	}
	
	/**
	 * Creates a Jetty {@link ServletContextHandler} for the Flux API endpoint
	 * @return Jetty ServletContextHandler
	 */
	@Named("APIServletContext")
	@Provides
	@Singleton
	ServletContextHandler getAPIServletContext() {
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SECURITY);
        context.setContextPath(API_CONTEXT_PATH);
        // now have a Guice filter process all the requests and dispatch it appropriately
        context.addFilter(GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
        return context;
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
		return server;
	}

	/**
	 * Creates the Jetty server instance for the Flux API endpoint and configures it with the @Named("APIServletContext").  
	 * @param port where the service is available
	 * @param acceptorThreads no. of acceptors
	 * @param maxWorkerThreads max no. of worker threads
	 * @return Jetty Server instance
	 */
	@Named("APIJettyServer")
	@Provides
	@Singleton
	Server getAPIJettyServer(@Named("Api.service.port") int port,
			@Named("Api.service.acceptors") int acceptorThreads,
            @Named("Api.service.selectors") int selectorThreads,
			@Named("Api.service.workers") int maxWorkerThreads,
			@Named("APIServletContext") ServletContextHandler servletContextHandler) {
	    QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setMaxThreads(maxWorkerThreads);
        Server server = new Server(threadPool);
	    ServerConnector http = new ServerConnector(server, acceptorThreads, selectorThreads);
        http.setPort(port);
        server.addConnector(http);
        server.setHandler(servletContextHandler);
        return server;
	}

}
