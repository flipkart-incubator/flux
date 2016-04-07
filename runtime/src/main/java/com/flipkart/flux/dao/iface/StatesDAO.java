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

package com.flipkart.flux.dao.iface;

import com.flipkart.flux.domain.State;

/**
 * Provides methods to perform CR operations on {@link com.flipkart.flux.domain.State}
 * @author shyam.akirala
 */
public interface StatesDAO {

    /** Creates a state in db and returns the saved object*/
    State create(State state);

    /** Retrieves a state by it's unique identifier*/
    State findById(String id);
}
