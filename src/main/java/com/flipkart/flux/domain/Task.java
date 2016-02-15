/*
 * Copyright 2012-2015, the original author or authors.
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
    Pair<Event,FluxError> execute(Event... events);
	void rollback();
}
