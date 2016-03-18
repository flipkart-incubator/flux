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

/**
 * A <Code>Hook</Code> specified on a com.flipkart.flux.client.model.Task gets invoked before (pre-entry) and after a task execution
 * Hooks are fired at most once by Flux and are never retried nor are any runtime failures handled.
 *
 */
public interface Hook {
    public void preEntry(FluxContext context);
    public void postEntry(FluxContext context);
}
