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

import com.flipkart.flux.resource.StateMachineResource;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.eclipse.jetty.servlet.DefaultServlet;

import java.util.HashMap;
import java.util.Map;

/**
 * <code>FluxServletModule</code> is a Guice {@link AbstractModule} implementation used for wiring Flux servlets, specifically the ones
 * used to service the API endpoint(s).
 * 
 * @author regunath.balasubramanian
 *
 */
public class FluxServletModule extends ServletModule {

	/** Useful constants for servlet container configuration parts */
	public static final String FSM_SERVLET_PATH = "/fsm";
	
	/**
	 * Configures servlets to be registered via Guice.
	 * @see com.google.inject.servlet.ServletModule#configureServlets()
	 */
    @Override
    protected void configureServlets() {
        bind(DefaultServlet.class).in(Singleton.class);
        bind(StateMachineResource.class).in(Singleton.class);
        Map<String, String> options = new HashMap<>();
        options.put("com.sun.jersey.api.json.POJOMappingFeature", "true");
    	// Register the DispatcherServlet that will service all calls to interact with deployed Flux Finite State Machines
//        serve(FSM_SERVLET_PATH+"/*").with(DispatcherServlet.class, options);
        serve("/*").with(GuiceContainer.class, options);
    }

}
