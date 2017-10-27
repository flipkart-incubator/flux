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
 * <code>FluxCancelPathException</code> tells Flux Runtime to cancel the branch the event is going to travel, until a join node encountered.
 *
 * For Example:
 * T1 -------> T2 ------> T3 ------> T4 -------> T5 -------> T9
 *             |                                             ^
 *             `--------> T6 ------> T7 -------> T8 --------/
 *
 * If task T6 throws this exception, the entire path from T6 until T9 is cancelled. T9 would be executed with the event emitted from task T5.
 *
 * This exception allows a user to achieve Dynamic conditions. Inside a task, based on the previous task output he can decide whether to go ahead in that path or stop there itself.
 *
 * for the above state machine
 *
 * {@literal @}Task(version = 1, retries = 2, timeout = 1000)
 * public ExampleEvent T6(ExampleEvent event) {
 *     if(event.exampleProperty()) {
 *         throw new FluxCancelPathException();
 *     }
 * }
 *
 * Created by shyam.akirala on 29/08/17.
 */
public class FluxCancelPathException extends RuntimeException {

    public FluxCancelPathException() {}
}
