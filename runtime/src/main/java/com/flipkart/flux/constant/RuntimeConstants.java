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

package com.flipkart.flux.constant;

/**
 * The <code>RuntimeConstants</code> class is a placeholder for all runtime framework constants.
 * 
 * @author regunath.balasubramanian
 * @author kartik.bommepally
 */
public class RuntimeConstants {

    /** Useful constants for servlet container configuration parts */
    public static final String DASHBOARD_CONTEXT_PATH = "/admin";
    public static final String API_CONTEXT_PATH = "/api";


    /**
     * Root for all the configs.
     */
    public static final String CONFIG_ROOT = "packaged";

   /**
     * Root for dashboard webapp configs.
     */
    public static final String DASHBOARD_VIEW = "dashboard";
    public static final String FSM_VIEW = "fsmview";

    /**
     * Configuration yml resource path.
     */
    public static final String CONFIGURATION_YML = CONFIG_ROOT + "/configuration.yml";
    public static final String STATE_MACHINE_RESOURCE_RELATIVE_PATH = "/machines";

    /** default timeout for a task */ //todo: move it to configuration file
    public static final Long defaultTaskTimeout = 1000l;
}