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

package com.flipkart.flux.examples.externalevents;

import com.flipkart.flux.client.runtime.Stoppable;
import com.google.inject.AbstractModule;

/**
 * <code>SellerVerificationWorkflowModule</code> is a Guice {@link AbstractModule} which configures all the classes of the this workflow.
 * This class has to be specified in flux_config.yml file with key "guiceModuleClass". Refer to packaged/flux_config.yml
 *
 * @author shyam.akirala
 */
public class SellerVerificationWorkflowModule extends AbstractModule {

    @Override
    protected void configure() {
        //install other modules
        bind(Stoppable.class).to(SelllerVerificationWorkflowStoppableImpl.class);
        System.out.println("Configuring example deployment unit..........");
    }
}
