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
package com.flipkart.flux.impl.message;

import java.io.Serializable;

/**
 * Data holder for Task Redriver message 
 * @author regunath.balasubramanian
 */
public class TaskRedriverDetails implements Serializable {
	
	/** Permitted actions*/
	public static enum RegisterAction {
		Register, Deregister, Redrive
	}

	/** Member variables*/
	private Long taskId;
	private long redriverDelay;
	private TaskRedriverDetails.RegisterAction action;
	
	/** Constructors*/
	public TaskRedriverDetails(Long taskId, TaskRedriverDetails.RegisterAction action) {
		super();
		this.taskId = taskId;
		this.action = action;
	}
	public TaskRedriverDetails(Long taskId, long redriverDelay, TaskRedriverDetails.RegisterAction action) {
		this(taskId, action);
		this.redriverDelay = redriverDelay;
	}
	
	/** Accessors*/
	public Long getTaskId() {
		return taskId;
	}
	public long getRedriverDelay() {
		return redriverDelay;
	}
	public TaskRedriverDetails.RegisterAction getAction() {
		return action;
	}
	
}
