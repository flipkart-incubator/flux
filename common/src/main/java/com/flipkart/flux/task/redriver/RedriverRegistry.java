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
package com.flipkart.flux.task.redriver;

/**
 * <code>RedriverRegistry</code> defines behavior for a Flux re-driver registry that executes stalled/zombie {@see Task} instances
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
	public void registerTask(Long taskId, String stateMachineId, long redriverDelay);
	
	/**
	 * Cancels restarts for the specified Task from stalled state.
	 * @param stateMachineId StateMachineIdentifier
	 * @param taskId task Identifier
	 */
	public void deRegisterTask(String stateMachineId, Long taskId);
	
	/**
	 * Re-drives i.e. re-runs the Task identified by the specified Task Id
	 * @param taskId identifier for the Task to re-drive/re-run 
	 */
	public void redriveTask(String stateMachineId, Long taskId);
}
