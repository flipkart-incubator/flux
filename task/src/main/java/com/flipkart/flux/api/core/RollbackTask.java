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

package com.flipkart.flux.api.core;

import com.flipkart.flux.api.EventData;
import com.flipkart.flux.domain.State;
import javafx.util.Pair;

/**
 * <code>RollbackTask</code> is a {@link Task} that supports compensating execution in case of state transition failures - either in the current {@link State} or a later one.
 * 
 * @author Yogesh
 * @author regunath.balasubramanian
 *
 */
public interface RollbackTask extends Task {
	
    /**
     * Callback method to effect a rollback when state transition fails in the current State or a later one
     * @param rollbackTrigger the Pair of Event and/or FluxError that triggered the rollback
     */
    public void rollback(Pair<EventData,FluxError> rollbackTrigger);
}
