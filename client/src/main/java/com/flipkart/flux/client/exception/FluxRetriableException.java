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

package com.flipkart.flux.client.exception;

/**
 * <code>FluxRetriableException</code> indicates an Exception on occurrence of it Flux Runtime retries task execution if
 * the task has not exhausted the configured number of retries.
 *
 * For ex:
 *      {@literal @}Task(version = 1, retires = 2, timeout = 1000)
 *      public Object Task1() {
 *          try {
 *              // do some stuff
 *          } catch(UserException ex) {
 *              throw new FluxRetriableException("message", ex); //wrap the exception with FluxRetriableException to make Flux runtime to retry this task
 *          }
 *      }
 *
 * @author shyam.akirala
 */
public class FluxRetriableException extends RuntimeException {

    /** Constructors */
    public FluxRetriableException(String message) {
        super(message);
    }

    public FluxRetriableException(String message, Throwable cause) {
        super(message, cause);
    }
}
