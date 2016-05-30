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

import akka.actor.ActorRef;
import com.flipkart.flux.MigrationUtil.MigrationsRunner;
import com.flipkart.flux.guice.module.ConfigModule;
import com.flipkart.flux.guice.module.ContainerModule;
import com.flipkart.flux.guice.module.HibernateModule;
import com.flipkart.flux.impl.boot.TaskModule;
import com.flipkart.flux.impl.task.registry.EagerInitRouterRegistryImpl;
import com.flipkart.flux.impl.temp.Work;
import com.flipkart.polyguice.core.support.Polyguice;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.MessageFormat;

import static com.flipkart.flux.constant.RuntimeConstants.CONFIGURATION_YML;

/**
 * <code>FluxInitializer</code> initializes the Flux runtime using the various Guice modules via Polyguice
 * @author shyam.akirala
 * @author regunath.balasubramanian
 */
public class FluxInitializer {

	/** Logger for this class */
    private static final Logger logger = LoggerFactory.getLogger(FluxInitializer.class);
	
	/** The machine name where this Flux instance is running */
	private String hostName;
	
	/** The Flux startup display contents*/
	private static final MessageFormat STARTUP_DISPLAY = new MessageFormat(
            "\n*************************************************************************\n" +
                    " Flux             ___          \n" +
                    "          ___    /   \\        \n" +
                    "         /   \\__| ( ) |       " + "     Startup Time : {0}" + " ms\n" +
                    "   ___  | ( ) |  \\___/        " + "     Host Name: {1} \n " +
                    " /   \\/ \\___/     \\___         \n" +
                    " | ( ) |   \\___    /   \\   \n" +
                    "  \\___/    /   \\__| ( ) |      \n" +
                    "          | ( ) |  \\___/     \n" +
                    "           \\___/        \n" +
                    "*************************************************************************"
    );


	/** The Polyguice DI container */
    private Polyguice fluxRuntimeContainer;
    
    /** THe URL for configuring Polyguice*/
    private final URL configUrl;

    /**
     * Constructor for this class
     * @param config the Config resource for initializing Flux
     */
    public FluxInitializer(String config) {
    	try {
    		this.hostName = InetAddress.getLocalHost().getHostName();
    	} catch (UnknownHostException e) {
    		//ignore the exception, not critical information
    	}        
    	configUrl = this.getClass().getClassLoader().getResource(config);
        this.fluxRuntimeContainer = new Polyguice();
    }

    /**
     * Startup entry method 
     */
    public static void main(String[] args) throws Exception {
        String command = "start";
        String config  = CONFIGURATION_YML;
        if (args != null && args.length> 0) {
            command = args[0];
        }
        final FluxInitializer fluxInitializer = new FluxInitializer(config);
        switch (command) {
            case "start" :
                fluxInitializer.start();
                break;
            case "migrate" :
                fluxInitializer.migrate();
                break;
        }
    }

    /**
     * 1. Register all relavent modules with the Polyguice container
     * 2. Boot the polyguice container
     */
    private void loadFluxRuntimeContainer() {
        logger.debug("loading flux runtime container");
        final ConfigModule configModule = new ConfigModule(configUrl);
        fluxRuntimeContainer.modules(configModule, new HibernateModule(), new ContainerModule(), new TaskModule());
        fluxRuntimeContainer.registerConfigurationProvider(configModule.getConfigProvider());
        fluxRuntimeContainer.prepare();
    }

    private void start() throws Exception {
    	logger.info("** Flux starting up... **");
    	long start = System.currentTimeMillis();
        //load flux runtime container
        loadFluxRuntimeContainer();
        /*
            The following piece of code should ideally be replaced using PolyTrooper or a similar construct.
            We're just making do for now
         */
        initialiseAkkaRuntime(fluxRuntimeContainer);
        /* Bring up the API server */
        logger.debug("loading API server");
        final Server apiJettyServer = fluxRuntimeContainer.getComponentContext().getInstance("APIJettyServer", Server.class);
        apiJettyServer.start();
        logger.debug("API server started. Say Hello!");
        /* Bring up the Dashboard server */
        logger.debug("Loading Dashboard Server");
        final Server dashboardJettyServer = fluxRuntimeContainer.getComponentContext().getInstance("DashboardJettyServer", Server.class);
        dashboardJettyServer.start();
        logger.debug("Dashboard server has started. Say Hello!");

        final Object[] displayArgs = {
				(System.currentTimeMillis() - start),
				this.hostName,
        };
		logger.info(STARTUP_DISPLAY.format(displayArgs));
		logger.info("** Flux startup complete **");
        
        // TODO - remove this call
        testOut();
    }

    /** Helper method to initialize the Akka runtime*/
    private void initialiseAkkaRuntime(Polyguice polyguice) throws InterruptedException {
        // This basically "inits" the system. Will be handled by PolyTrooper
        final EagerInitRouterRegistryImpl routerRegistry = polyguice.getComponentContext().getInstance(EagerInitRouterRegistryImpl.class);
    }

    /** Helper method to perform migrations*/
    private void migrate() {
        loadFluxRuntimeContainer();
        MigrationsRunner migrationsRunner = fluxRuntimeContainer.getComponentContext().getInstance(MigrationsRunner.class);
        migrationsRunner.migrate();
    }

    // TODO (Temporary) - For Mr Regunath to play with :)
    private void testOut() throws InterruptedException {
        final EagerInitRouterRegistryImpl routerRegistry = fluxRuntimeContainer.getComponentContext().getInstance(EagerInitRouterRegistryImpl.class);
        routerRegistry.getRouter("someRouter").tell("Message for some router", ActorRef.noSender());
        routerRegistry.getRouter("someRouterWithoutConfig").tell("Message for some router with no config", ActorRef.noSender());
        Thread.sleep(1000l);
        for (int i = 0 ; i < 10 ; i++) {
            routerRegistry.getRouter("someRouter").tell(new Work(),ActorRef.noSender());
        }
    }

}
