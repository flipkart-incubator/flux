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
package com.flipkart.flux.impl.task;

import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.Task;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <code>TaskRegistry</code> maintains an in-memory registry of {@link Task} and their {@link Event} mappings. This registry is usually populated during Flux startup by inspecting
 * deployment units containing Task definitions and the events that trigger them
 * 
 * @author regunath.balasubramanian
 *
 */
public class TaskRegistry {

    /** Map storing the mapping of a EventS to TaskS */
	private Map<String,AbstractTask> eventsToTaskMap = new ConcurrentHashMap<String, AbstractTask>();

    /** Map storing the mapping of a Task to pre execution HookS */
	private Map<String,List<AbstractHook>> taskToPreExecHookMap = new ConcurrentHashMap<String, List<AbstractHook>>();

    /** Map storing the mapping of a Task to post execution HookS */
	private Map<String,List<AbstractHook>> taskToPostExecHookMap = new ConcurrentHashMap<String, List<AbstractHook>>();
	
	/**
	 * Gets the Task that can process the specified set of EventS
	 * @param events the EventS to be processed
	 * @return null or Task that can process the specified set of EventS
	 */
	public AbstractTask getTaskForEvents(Event[] events) {
		return this.eventsToTaskMap.get(TaskRegistry.getEventsKey(events));
	}
	
	/**
	 * Registers the specified Task as one that can process the specified set of EventS
	 * @param task the Task
	 * @param events array of EventS that the Task can process
	 */
	public void registerTask(AbstractTask task, Event[] events) {
		this.eventsToTaskMap.put(TaskRegistry.getEventsKey(events), task);
	}

	/**
	 * Gets the List of HookS to be invoked pre-execution of the specified Task
	 * @param task the Task pending execution
	 * @return null or List of HookS that are to be executed pre-execution of the specified Task
	 */
	public List<AbstractHook> getPreExecHooks(Task task) {
		return this.taskToPreExecHookMap.get(task.getClass().getName());
	}

	/**
	 * Gets the List of HookS to be invoked post-execution of the specified Task
	 * @param task the Task that has been executed
	 * @return null or List of HookS that are to be executed post-execution of the specified Task
	 */
	public List<AbstractHook> getPostExecHooks(Task task) {
		return this.taskToPostExecHookMap.get(task.getClass().getName());
	}
	
	/**
	 * Registers the specified List of HookS for pre-execution of the specified Task
	 * @param task the Task to register against
	 * @param hooks List of HookS for execution
	 */
	public void registerPreExecHooks(AbstractTask task, List<AbstractHook> hooks) {
		this.taskToPreExecHookMap.put(task.getClass().getName(), hooks);
	}

	/**
	 * Registers the specified List of HookS for post-execution of the specified Task
	 * @param task the Task to register against
	 * @param hooks List of HookS for execution
	 */
	public void registerPostExecHooks(AbstractTask task, List<AbstractHook> hooks) {
		this.taskToPostExecHookMap.put(task.getClass().getName(), hooks);
	}
	
	/**
	 * Helper method to get a key from fully qualified class names of the specified EventS
	 * @param events Event[] array for creating key
	 * @return String representing the EventS
	 */
	public static String getEventsKey(Event[] events) {
		StringBuilder sb = new StringBuilder();
		for (Event event : events) {
			sb.append(event.getClass().getName());
			sb.append("_");
		}
		return sb.toString();
	}
	
}
