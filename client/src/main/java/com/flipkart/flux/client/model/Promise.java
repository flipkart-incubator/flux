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

package com.flipkart.flux.client.model;

import com.flipkart.flux.examples.orderfulfilment.PackedOrder;

/**
 * Used to wrap the result of a com.flipkart.flux.client.model.Task.
 * This is used by Flux runtime to enable tasks that can now be executed
 * @param <V>
 */
public class Promise<V> {

    public Promise(V v) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    boolean isDone() {
        return false;
    }

    public V get() {
        return null;
    }
}
