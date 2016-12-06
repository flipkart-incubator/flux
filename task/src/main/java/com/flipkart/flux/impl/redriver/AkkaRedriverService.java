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

package com.flipkart.flux.impl.redriver;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.flipkart.flux.redriver.service.RedriverService;
import scala.Option;

/**
 * Akka {@link UntypedActor} wrapper for {@link RedriverService} to bind its lifecycle to the actor.
 * It's purpose is to have a single instance of the service running in the cluster.
 * @author gaurav.ashok
 */
public class AkkaRedriverService extends UntypedActor {

    /** Logger instance for this class*/
    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    private final RedriverService redriverService;

    public AkkaRedriverService(RedriverService redriverService) {
        this.redriverService = redriverService;
    }

    @Override
    public void preStart() throws Exception {
        logger.info("Starting Redriver service");
        if(!redriverService.isRunning()) {
            redriverService.start();
        }
    }

    /* Override the default implementation so that preStart is not called again */
    @Override
    public void preRestart(Throwable reason, Option<Object> message) throws Exception {
    }

    /* Override the default implementation so that postStop is not called again */
    @Override
    public void postRestart(Throwable reason) throws Exception {
    }

    @Override
    public void postStop() throws Exception {
        logger.info("Stopping Redriver service");
        if(redriverService.isRunning()) {
            redriverService.stop();
        }
    }

    @Override
    public void onReceive(Object message) throws Exception {
        /* not expecting any message */
        logger.info("RedriverService Actor received message: {}", message);
        unhandled(message);
    }
}
