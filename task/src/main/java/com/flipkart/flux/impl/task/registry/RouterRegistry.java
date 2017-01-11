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
 *
 */

package com.flipkart.flux.impl.task.registry;

import akka.actor.ActorRef;

/**
 * Interface that allows for akka router lookups
 * @author yogesh.nachnani
 * @author gaurav.ashok
 */
public interface RouterRegistry {

    ActorRef getRouter(String forWorker);

    /**
     * Creates a router if it does not exists. If it exists it would be resized to the given name. Router can be resized to 0.
     * @param name Name of the router.
     * @param newSize New number of the routees that this router will have.
     */
    void createOrResize(String name, int newSize);
}
