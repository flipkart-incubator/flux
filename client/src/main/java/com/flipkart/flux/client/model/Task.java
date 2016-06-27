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
 * <Code>Task</Code> Represents a unit of work.
 * Any method annotated with a <Code>Task</Code> is eligible for execution if all the dependent parameters are satisfied
 * Such a method can return either a void or an <Code>Event</Code>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Task {
    /**
     * Each task must be versioned.
     * This enables the flux runtime to execute the right task based on the version number of the caller
     */
    long version();

    /** The number of times Flux can retry the task in case of runtime failures.
     * Note: Runtime failures are failures encountered by Flux - such as a task being timed out, or Flux not receiving the response on time.
     * If a task throws an exception, it means the task executed successfuly and passed a response to flux.
     * This exception is carried back to the caller stack - similar to the way in which jvm bubbles up the exception in regular method calls
     *
     */
    long retries() default 0;

    /**
     * Defines the maximum time flux can "expect" the task to run for.
     * In case the task does not produce a result within the timeout, the task may be retried by Flux (based on the number of retries remaining)
     * Any result produced by the task after the timeout value has elapsed will be ignored by the Flux engine and
     * will not be used by or passed on to other tasks that may be dependent on these results.
     * @return
     */
    long timeout();

    Class<? extends Hook>[] hooks() default {};
}
