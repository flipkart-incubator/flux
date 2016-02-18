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

package com.flipkart.flux.domain;

import javafx.util.Pair;

/**
 * @understands The code to be executed in the current state.
 * The task is eligible for execution once all the dependent Triggers are received
 * Assume the methods are RPCs being performed on a client's compute instance
 * */
public interface Task {
    /**
     * Unpacks the data from events and proceeds with an execution call to the actual URI that represents a remote worker
     * @param events Dependencies that need to be satisfied for this task to be executed
     * @return The event produced by a worker on successful execution OR an error object representing the error.
     */
    Pair<Event,FluxError> execute(Event... events);
	void rollback();
}
