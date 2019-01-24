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

import com.flipkart.flux.FluxRuntimeRole;
import com.flipkart.flux.MigrationUtil.MigrationsRunner;
import com.flipkart.flux.client.FluxClientComponentModule;
import com.flipkart.flux.client.FluxClientInterceptorModule;
import com.flipkart.flux.guice.module.*;
import com.flipkart.polyguice.core.support.Polyguice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;

/**
 * <code>FluxInitializer</code> initializes the Flux runtime using the various Guice modules via Polyguice
 *
 * @author shyam.akirala
 * @author regunath.balasubramanian
 * @author yogesh.nachnani
 */
public class FluxInitializer {

    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(FluxInitializer.class);

    public static FluxRuntimeRole fluxRole;

    /**
     * The machine name where this Flux instance is running
     */
    private String hostName;

    /**
     * The Flux startup display contents
     */
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


    /**
     * The Polyguice DI container
     */
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
        if (args != null && args.length > 1) {
            command = args[0];
        }
        final FluxInitializer fluxInitializer = new FluxInitializer();
        switch (command) {
            case "start":
                //default role
                if (args.length == 2) {
                    switch (args[1]) {
                        case "orchestration":
                            fluxRole = FluxRuntimeRole.ORCHESTRATION;
                            break;
                        case "execution":
                            fluxRole = FluxRuntimeRole.EXECUTION;
                            break;
                        default:
                            fluxRole = FluxRuntimeRole.EXECUTION;
                    }
                }
                fluxInitializer.start();
                break;
            case "migrate":
                if (args.length < 2) {
                    throw new RuntimeException("<migrate> must be followed with db name");
                }
                if (!(args[1].equals("flux") || args[1].equals("flux_scheduler"))) {
                    throw new RuntimeException("<migrate> works only for 'flux' or 'flux_scheduler'");
                }
                fluxInitializer.migrate(args[1]);
                break;
            default:
                throw new RuntimeException("Mandatory program arguments missing either be {start orchestration/execution} or {migrate flux/flux_scheduler}");
        }
    }

    /**
     * 1. Register all relavent modules with the Polyguice container
     * 2. Boot the polyguice container
     */

    private void loadFluxRuntimeContainer() {
        logger.debug("loading flux runtime container");
        logger.info("Running as role : {}", fluxRole);
        final ConfigModule configModule = new ConfigModule(fluxRole);
        //load flux runtime container
        // this ensures component booter is up and initialised
        switch (fluxRole) {
            case ORCHESTRATION:
                fluxRuntimeContainer.modules(
                        configModule,
                        new FluxClientComponentModule(),
                        new ShardModule(),
                        new OrchestrationTaskModule(),
                        new AuthNModule(),
                        new ContainerModule());
                break;
            case EXECUTION:
                logger.info("here");
                fluxRuntimeContainer.modules(
                        configModule,
                        new FluxClientComponentModule(),
                        new ExecutionContainerModule(),
                        new DeploymentUnitModule(),
                        new AkkaModule(),
                        new ExecutionTaskModule(),
                        new FluxClientInterceptorModule()
                       );
                break;
            default:
                logger.error("Node running as wrong role, exiting");
                throw new RuntimeException("App started with wrong parameters, should be either orchestration or" +
                        " execution");
        }

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
        if (fluxRole.equals(FluxRuntimeRole.ORCHESTRATION)) {
            final OrchestrationOrderedComponentBooter orchestratorInstance = this.fluxRuntimeContainer.getComponentContext()
                    .getInstance(OrchestrationOrderedComponentBooter.class);
        } else {
            final ExecutionOrderedComponentBooter executionInstance = this.fluxRuntimeContainer.getComponentContext()
                    .getInstance(ExecutionOrderedComponentBooter.class);
        }
        final Object[] displayArgs = {
                (System.currentTimeMillis() - start),
                this.hostName,
        };
        logger.info(STARTUP_DISPLAY.format(displayArgs));
        logger.info("** Flux startup complete **");
    }

    /**
     * Helper method to perform migrations
     */
    private void migrate(String dbName) throws InterruptedException {
        loadFluxRuntimeContainer();
        MigrationsRunner migrationsRunner = fluxRuntimeContainer.getComponentContext().getInstance(MigrationsRunner.class);
        migrationsRunner.migrate(dbName);

    }
}
