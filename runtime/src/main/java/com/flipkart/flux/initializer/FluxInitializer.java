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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.flipkart.flux.FluxRuntimeRole;
import com.flipkart.flux.MigrationUtil.MigrationsRunner;
import com.flipkart.flux.client.FluxClientComponentModule;
import com.flipkart.flux.client.FluxClientInterceptorModule;
import com.flipkart.flux.guice.module.AkkaModule;
import com.flipkart.flux.guice.module.ConfigModule;
import com.flipkart.flux.guice.module.ContainerModule;
import com.flipkart.flux.guice.module.DeploymentUnitModule;
import com.flipkart.flux.guice.module.ExecutionContainerModule;
import com.flipkart.flux.guice.module.ExecutionTaskModule;
import com.flipkart.flux.guice.module.OrchestrationTaskModule;
import com.flipkart.flux.guice.module.OrchestratorContainerModule;
import com.flipkart.flux.guice.module.ShardModule;
import com.flipkart.polyguice.core.support.Polyguice;

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
    private static final Logger logger = LogManager.getLogger(FluxInitializer.class);

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
    public static void main(String[] args) {
        String command = "start";
        if (args.length > 1) {
            command = args[0];
        }
        final FluxInitializer fluxInitializer = new FluxInitializer();
        switch (command) {
            case "start": // default command
                if (args.length == 2) {
                    switch (args[1]) {
                        case "orchestration":
                            fluxRole = FluxRuntimeRole.ORCHESTRATION;
                            break;
                        case "execution":
                            fluxRole = FluxRuntimeRole.EXECUTION;
                            break;
                    }
                } else {
                    fluxRole = FluxRuntimeRole.COMBINED; // no role specified. Default is combined
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
                //using the default role as orchestration for migration
                fluxRole = FluxRuntimeRole.ORCHESTRATION;
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
        logger.info("Running as role : {}", fluxRole);
        final ConfigModule configModule = new ConfigModule(fluxRole);
        //load flux runtime container
        switch (fluxRole) {
            case ORCHESTRATION:
                fluxRuntimeContainer.modules(
                        configModule,
                        new ContainerModule(),
                        new ShardModule(),
                        new OrchestrationTaskModule(),
                        new OrchestratorContainerModule(),
                        new FluxClientComponentModule());
                break;
            case EXECUTION:
                fluxRuntimeContainer.modules(
                        configModule,
                        new ContainerModule(),
                        new ExecutionContainerModule(),
                        new DeploymentUnitModule(),
                        new AkkaModule(),
                        new ExecutionTaskModule(),
                        new FluxClientInterceptorModule(),
                        new FluxClientComponentModule());
                break;
            default:
                // we bring up the combined orchestration + execution runtime
                fluxRuntimeContainer.modules(
                        configModule,
                        new ContainerModule(),
                        new ShardModule(),
                        new OrchestrationTaskModule(),
                        new OrchestratorContainerModule(),
                        new FluxClientComponentModule(),
                        new ExecutionContainerModule(),
                        new DeploymentUnitModule(),
                        new AkkaModule(),
                        new ExecutionTaskModule(),
                        new FluxClientInterceptorModule());
        }

        //scans package com.flipkart.flux for polyguice specific annotations like @Bindable, @Component etc.
        fluxRuntimeContainer.scanPackage("com.flipkart.flux");
        fluxRuntimeContainer.registerConfigurationProvider(configModule.getConfigProvider());
        fluxRuntimeContainer.prepare();
    }

    private void start() {
        logger.info("** Flux starting up... **");
        long start = System.currentTimeMillis();
        //load flux runtime container
        loadFluxRuntimeContainer();
        // this ensures component booter is up and initialised
        switch (fluxRole) {
            case ORCHESTRATION:
                this.fluxRuntimeContainer.getComponentContext().getInstance(OrchestrationOrderedComponentBooter.class);
                break;
            case EXECUTION:
                this.fluxRuntimeContainer.getComponentContext().getInstance(ExecutionOrderedComponentBooter.class);
                break;
            default:
                this.fluxRuntimeContainer.getComponentContext().getInstance(OrchestrationOrderedComponentBooter.class);
                this.fluxRuntimeContainer.getComponentContext().getInstance(ExecutionOrderedComponentBooter.class);
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
    private void migrate(String dbName) {
        loadFluxRuntimeContainer();
        MigrationsRunner migrationsRunner = fluxRuntimeContainer.getComponentContext().getInstance(MigrationsRunner.class);
        migrationsRunner.migrate(dbName);
    }
}
