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

package com.flipkart.flux.representation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * It initializes in-memory ClientElbCache belonging to Singleton
 * ClientElbPersistenceService object by retrieving oldest entries from database bounded by maximum cache size mentioned.
 * @author akif.khan
 */
@Singleton
public class ClientElbCacheInitializer {

    private ClientElbPersistenceService clientElbPersistenceService;

    @Inject
    public ClientElbCacheInitializer(ClientElbPersistenceService clientElbPersistenceService) {
        this.clientElbPersistenceService =  clientElbPersistenceService;
    }

    public void initialize() {
        clientElbPersistenceService.clientElbCacheInitializer();
    }

}
