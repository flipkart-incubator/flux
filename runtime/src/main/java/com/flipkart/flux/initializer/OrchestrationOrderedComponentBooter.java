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

package com.flipkart.flux.initializer;

import com.flipkart.flux.guice.module.AuthNModule;
import com.flipkart.flux.listeners.AuthLifeCycleListener;
import com.flipkart.kloud.authn.filter.AuthConfig;
import com.flipkart.polyguice.core.Initializable;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * <code>OrchestrationOrderedComponentBooter</code> boots various components one after the other. This is makeshift - till we get something equivalent to trooper
 * (https://github.com/regunathb/Trooper)
 *
 * @author yogesh.nachnani
 */
@Singleton
public class OrchestrationOrderedComponentBooter implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(OrchestrationOrderedComponentBooter.class);
    private final Server apiServer;
    private final Server dashboardServer;
    private final AuthConfig authConfig;
    private final WebAppContext webAppContext;


    @Inject
    public OrchestrationOrderedComponentBooter(@Named("APIJettyServer") Server apiServer,
                                               @Named("AuthnConfig") AuthConfig authConfig,
                                               @Named("DashboardJettyServer") Server dashboardServer,
                                               @Named("DashboardContext") WebAppContext webAppContext
    ) {
        this.apiServer = apiServer;
        this.dashboardServer = dashboardServer;
        this.authConfig = authConfig;
        this.webAppContext = webAppContext;

    }

    @Override
    public void initialize() {
        /*
            The following piece of code should ideally be replaced using PolyTrooper or a similar construct.
            We're just making do for now
         */

        try {
            /* Bring up the API server */
            logger.info("loading API server");
            apiServer.start();
            logger.info("API server started. Say Hello!");
            /* Bring up the Dashboard server */
            logger.info("Loading Dashboard Server");
            if (authConfig.isAuthEnabled()) {
                AuthNModule.configureUIApp(webAppContext, authConfig);
            }
            dashboardServer.start();
            logger.info("Dashboard server has started. Say Hello!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
