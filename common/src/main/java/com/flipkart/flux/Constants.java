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

package com.flipkart.flux;

/**
 * <code>Constants</code> maintains constant variables which would be used across Flux modules.
 */
public interface Constants {

    /**
     * Name of Codahale Metrics registry on which all metrics are published
     */
    String METRIC_REGISTRY_NAME = "mainMetricRegistry";

    /**
     * Root for all the configs.
     */
    String CONFIG_ROOT = "packaged";

    /**
     * Configuration yml resource path.
     */
    String CONFIGURATION_YML = CONFIG_ROOT + "/configuration.yml";
}
