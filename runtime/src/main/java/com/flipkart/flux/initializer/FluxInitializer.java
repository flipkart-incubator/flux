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
import com.flipkart.flux.guice.module.*;
import com.flipkart.flux.impl.boot.TaskModule;
import com.flipkart.polyguice.core.support.Polyguice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;

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
    
    /**
     * Constructor for this class
     */
    public FluxInitializer() {
    	try {
    		this.hostName = InetAddress.getLocalHost().getHostName();
    	} catch (UnknownHostException e) {
    		//ignore the exception, not critical information
    	}        
        this.fluxRuntimeContainer = new Polyguice();
    }

    /**
     * Startup entry method 
     */
    public static void main(String[] args) throws Exception {
        String command = "start";
        if (args != null && args.length> 0) {
            command = args[0];
        }
        final FluxInitializer fluxInitializer = new FluxInitializer();
        switch (command) {
            case "start" :
                fluxInitializer.start();
                break;
            case "migrate" :
                if (args.length < 2) {
                    throw new RuntimeException("<migrate> must be followed with db name");
                }
                if (!(args[1].equals("flux") || args[1].equals("flux_redriver"))) {
                    throw new RuntimeException("<migrate> works only for 'flux' or 'flux_redriver'");
                }
                fluxInitializer.migrate(args[1]);
                break;
        }
    }

    /**
     * 1. Register all relavent modules with the Polyguice container
     * 2. Boot the polyguice container
     */
    private void loadFluxRuntimeContainer() {
        logger.debug("loading flux runtime container");
        final ConfigModule configModule = new ConfigModule();
        fluxRuntimeContainer.modules(
                configModule,
                new HibernateModule(),
                new ContainerModule(),
                new DeploymentUnitModule(),
                new AkkaModule(),
                new TaskModule(),
                new FluxClientInterceptorModule()
        );
        //scans package com.flipkart.flux for polyguice specific annotations like @Bindable, @Component etc.
        fluxRuntimeContainer.scanPackage("com.flipkart.flux");
        fluxRuntimeContainer.registerConfigurationProvider(configModule.getConfigProvider());
        fluxRuntimeContainer.prepare();
    }

    private void start() throws Exception {
    	logger.info("** Flux starting up... **");
    	long start = System.currentTimeMillis();
        //load flux runtime container
        loadFluxRuntimeContainer();
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
