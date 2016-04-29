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

package com.flipkart.flux.controller;

import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.routing.Router;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.impl.task.AkkaTask;

/**
 * <code>TaskRuntimeController</code> receives events from flux runtime and spawns or uses already spawned
 * {@link AkkaTask} actors to execute {@link com.flipkart.flux.domain.Task}
 * @author shyam.akirala
 */
public class TaskRuntimeController extends UntypedActor{

    Router router;

    @Override
    public void onReceive(Object message) throws Exception {

        if (Event[].class.isAssignableFrom(message.getClass())) {

             //route the request to an actor
             router.route(message, getSender()); //on completion the execution actor directly posts to flux runtime, that's why passing getSender().

        } else if (message instanceof Terminated) {
            //re-spawn the actor in case of termination
        }

    }

}
