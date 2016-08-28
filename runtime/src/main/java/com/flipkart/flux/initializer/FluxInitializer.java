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

import com.flipkart.flux.MigrationUtil.MigrationsRunner;
import com.flipkart.flux.client.FluxClientInterceptorModule;
import com.flipkart.flux.deploymentunit.ExecutableRegistryPopulator;
import com.flipkart.flux.guice.module.*;
import com.flipkart.flux.impl.boot.TaskModule;
import com.flipkart.polyguice.core.support.Polyguice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.MessageFormat;

import static com.flipkart.flux.Constants.CONFIGURATION_YML;

/**
 * <code>FluxInitializer</code> initializes the Flux runtime using the various Guice modules via Polyguice
 * @author shyam.akirala
 * @author regunath.balasubramanian
 * @author yogesh.nachnani
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
                if (args.length < 3) {
                    throw new RuntimeException("<migrate> must be followed with db name");
                }
                if (!args[2].equals("flux") || args[2].equals("flux_redriver")) {
                    throw new RuntimeException("<migrate> works only for 'flux' or 'flux_redriver'");
                }
                fluxInitializer.migrate(args[2]);
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
        fluxRuntimeContainer.modules(
                configModule,
                new HibernateModule(),
                new ContainerModule(),
                new DeploymentUnitModule(),
                new AkkaModule(),
                new TaskModule(),
                new FluxClientInterceptorModule()
        );
        fluxRuntimeContainer.registerConfigurationProvider(configModule.getConfigProvider());
        fluxRuntimeContainer.prepare();
    }

    private void start() throws Exception {
    	logger.info("** Flux starting up... **");
    	long start = System.currentTimeMillis();
        //load flux runtime container
        loadFluxRuntimeContainer();
        // populates the executable registry with the task methods available in all deployment units
        final ExecutableRegistryPopulator executableRegistryPopulator = this.fluxRuntimeContainer.getComponentContext().getInstance(ExecutableRegistryPopulator.class);
        // this ensures component booter is up and initialised
        final OrderedComponentBooter instance = this.fluxRuntimeContainer.getComponentContext().getInstance(OrderedComponentBooter.class);
        final Object[] displayArgs = {
				(System.currentTimeMillis() - start),
				this.hostName,
        };
		logger.info(STARTUP_DISPLAY.format(displayArgs));
        logger.info("** Flux startup complete **");
    }

    /** Helper method to perform migrations
     * @param dbName - name of the database to run migrations on
     * */
    private void migrate(String dbName) {
        loadFluxRuntimeContainer();
        MigrationsRunner migrationsRunner = fluxRuntimeContainer.getComponentContext().getInstance(MigrationsRunner.class);
        migrationsRunner.migrate(dbName);
    }

}
