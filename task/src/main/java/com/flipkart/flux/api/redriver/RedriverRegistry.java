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
 *
 */
package com.flipkart.flux.api.redriver;

import com.flipkart.flux.api.core.Task;

/**
 * <code>RedriverRegistry</code> defines behavior for a Flux re-driver registry that executes stalled/zombie {@link Task} instances 
 * including those that missed an execution schedule because of node failure in the Flux cluster.
 * 
 * @author regunath.balasubramanian
 *
 */
public interface RedriverRegistry {

	/**
	 * Registers the specified task with this re-driver registry for restarts from stalled state after the specified
	 * re-driver delay.
	 * @param taskId the Task State identifier
	 * @param redriverDelay the minimum elapsed duration after which restart is to be attempted
	 */
	public void registerTask(Long taskId, long redriverDelay);
	
	/**
	 * Cancels restarts for the specified Task from stalled state.
	 * @param taskId the Task State identifier
	 */
	public void deRegisterTask(Long taskId);
}
