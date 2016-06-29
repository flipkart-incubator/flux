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
 * Used to denote a task parameter which will be posted externally.
 * A task is eligible for execution only when all its parameters are supplied. Using this annotation, the user can
 * show her intention of passing this data to the flux engine externally using the @see com.flipkart.flux.client.FluxClient
 * @author yogesh.nachnani
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExternalEvent {
    /**
     * A user-friendly way for the user to name the external event. This can then be used to post data against this dependency
     * Note that the name given to a parameter needs to be unique across the workflow, i.e you cannot have two parameters with the same name
     * across any of the tasks used in a workflow.
     * Also, when you post data against this named <code>ExternalEvent</code>, the same data is passed to all invocations for that task
     * throughout the workflow.
     * For example, if there is a method <code>void foo(@ExternalEvent("bar") String xyz, Integer i)</code>
     * and method <code>foo()</code> is invoked multiple times during the lifecycle of the workflow, execution of <code>foo()</code> will get triggered
     * only after the external event named "bar" is posted. The data passed against "bar" will be used for all invocations of <code>foo()</code>
     */
    String value();
}
