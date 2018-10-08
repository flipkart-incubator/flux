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

package com.flipkart.flux.initializer;

import com.flipkart.flux.impl.boot.ActorSystemManager;
import com.flipkart.flux.impl.task.registry.RouterRegistry;
import com.flipkart.polyguice.core.Initializable;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/*
* ExecutionOrderedComponentBooter boots startup components of runtime.
* */

@Singleton
public class ExecutionOrderedComponentBooter implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(ExecutionOrderedComponentBooter.class);
    private final Server executionApiServer;
    private final Server executionDashboardServer;
    private final ActorSystemManager actorSystemManager;
    private final RouterRegistry routerRegistry;

    @Inject
    public ExecutionOrderedComponentBooter(RouterRegistry routerRegistry,
                                           @Named("ExecutionAPIJettyServer") Server executionApiServer,
                                           @Named("ExecutionDashboardJettyServer") Server executionDashboardServer,
                                           ActorSystemManager actorSystemManager) {
        this.executionApiServer = executionApiServer;
        this.executionDashboardServer =  executionDashboardServer;
        this.actorSystemManager = actorSystemManager;
        this.routerRegistry = routerRegistry;
    }

    @Override
    public void initialize() {
        /** The akka run time should have booted up by now , check that */
        if (!this.actorSystemManager.isInitialised()) {
            throw new RuntimeException("Actor System should have been initialised by now. WTF!!");
        }

        try {
             /* Bring up the Execution API server */
            logger.info("loading Execution API server");
            executionApiServer.start();
            logger.info("API server started. Say Hello!");
            /* Bring up the Dashboard server */
            logger.info("Loading Execution Dashboard Server");
            executionDashboardServer.start();
            logger.info("Execution Dashboard server has started. Say Hello!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
