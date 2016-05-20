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

package com.flipkart.flux.module;

import com.flipkart.flux.constant.RuntimeConstants;
import com.flipkart.flux.guice.module.ConfigModule;
import com.flipkart.flux.guice.module.ContainerModule;
import com.flipkart.flux.guice.module.HibernateModule;
import com.flipkart.polyguice.config.ApacheCommonsConfigProvider;
import com.google.inject.AbstractModule;

import java.net.URL;

/**
 * <code>RuntimeTestModule</code> is a Guice module binds modules which are required to run tests.
 * @author shyam.akirala
 */
public class RuntimeTestModule extends AbstractModule{
    @Override
    protected void configure() {
        final URL url = this.getClass().getClassLoader().getResource(RuntimeConstants.CONFIGURATION_YML);
        install(new ConfigModule(url));
        install(new HibernateModule());
        install(new ContainerModule());
    }
}
