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

package com.flipkart.flux.metrics;

import com.flipkart.polyguice.core.Initializable;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicConfiguration;
import com.netflix.config.FixedDelayPollingScheduler;
import com.netflix.config.PolledConfigurationSource;
import com.netflix.config.sources.URLConfigurationSource;
import com.netflix.turbine.init.TurbineInit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;

/**
 * <code>TurbineInitializer</code> implements {@link Initializable} and does the thrift of initializing turbine by loading
 * the properties file, registering the properties to Archiaus(https://github.com/Netflix/archaius/) and
 * calling the {@link TurbineInit#init()}
 *
 * @author shyam.akirala
 */
public class TurbineInitializer implements Initializable {

    /** Logger for this class*/
    private static final Logger LOGGER = LoggerFactory.getLogger(TurbineInitializer.class);

    @Override
    public void initialize() {
        LOGGER.info("Initializing turbine");
        try {
            //Find the config file
            URL turbineFileUrl = null;
            String turbineConfig = System.getProperty("flux.turbineConfigFile");
            if (turbineConfig != null) {
                turbineFileUrl = new File(turbineConfig).toURI().toURL();
            } else {
                turbineFileUrl = this.getClass().getClassLoader().getResource("packaged/dashboard-config.properties");
            }
            LOGGER.info("Found dashboard config file: " + turbineFileUrl);

            //Add it as a configuration source
            PolledConfigurationSource source = new URLConfigurationSource(turbineFileUrl);
            DynamicConfiguration configuration = new DynamicConfiguration(source, new FixedDelayPollingScheduler());
            //Add the configurations to Archiaus
            ConfigurationManager.install(configuration);

            //Init Turbine
            TurbineInit.init();
            LOGGER.debug("Successfully initiated Turbine");
        }  catch(Exception e) {
            LOGGER.error("Error configuring and initiating turbine", e);
        }
    }
}
