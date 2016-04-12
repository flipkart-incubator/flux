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

/**
 * <code>Hook</code> is user defined code that is executed asynchronously on entry or exit of a {@link State}
 * The outcome of Hook execution does not impact state transition. Hooks are executed every time a state transition happens, including when retries happen and
 * this implies that Hook executions are better off being idempotent.
 * 
 *  @author shyam.akirala
 *  @author regunath.balasubramanian
 * 
 */
public interface Hook<T> {

	/**
	 * Executes this Hook asynchronous to State transition
	 * @param events the EventS available on entry or while exiting a State
	 */
	public void execute(Event<T>[] events);

}
