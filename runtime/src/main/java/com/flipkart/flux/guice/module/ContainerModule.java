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

import java.io.File;

import javax.inject.Named;
import javax.inject.Singleton;

import org.eclipse.jetty.webapp.WebAppContext;

import com.flipkart.flux.config.FileLocator;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

/**
 * <code>ContainerModule</code> is a Guice {@link AbstractModule} implementation used for wiring Flux container components.
 * 
 * @author regunath.balasubramanian
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
	 * @return
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
				if (fileToString.contains("runtime")) {
					path = fileToString;
					break;
				}
			}
		}
		// trim off the "WEB-INF" part as the WebAppContext path should refer to the parent directory
		if (path.endsWith("WEB-INF")) {
			path = path.replace("WEB-INF", "");
		}
		WebAppContext webAppContext = new WebAppContext(path, "/admin");
		return webAppContext;
	}

}
