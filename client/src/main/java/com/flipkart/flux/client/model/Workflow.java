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

import java.lang.annotation.*;

/**
 * The <Code>Workflow</Code> annotation is used to declare Flux Workflows.
 * Essentially, a new workflow is "submitted" to Flux whenever a method annotated with <Code>Workflow</Code> is executed
 * Any @com.flipkart.flux.client.model.Task methods used within the workflow are executed asynchronously,
 * whenever their dependencies are satisfied.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Workflow {
    /**
     * Each workflow must be versioned.
     * This enables the flux runtime to execute the right task based on the version number of this workflow
     */
    long version();

    String description() default "";
}
