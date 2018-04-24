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

package com.flipkart.flux.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import static com.flipkart.flux.Constants.STATE_MACHINE_ID;

import java.util.Objects;

/**
 * @author akif.khan
 */
public class LoggingUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingUtils.class);

    public static void registerStateMachineIdForLogging(String stateMachineId) {
        if (Objects.isNull(stateMachineId)) {
            LOGGER.warn("Cannot register for logging as stateMachineId is null");
            return;
        }
        MDC.put(STATE_MACHINE_ID, "smId:"+stateMachineId);
    }

    public static void deRegisterStateMachineIdForLogging() {
        try {
            MDC.remove(STATE_MACHINE_ID);
        } catch (Exception ex) {
            LOGGER.error("MDC STATE_MACHINE_ID remove method have thrown exception with message "+ex);
        }
    }
}
