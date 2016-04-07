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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.Task;

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
	
	/**
	 * Gets the Task that can process the specified set of EventS
	 * @param events the EventS to be processed
	 * @return null or Task that can process the specified set of EventS
	 */
	public AbstractTask getTaskForEvents(Event<Object>[] events) {
		return this.eventsToTaskMap.get(TaskRegistry.getEventsKey(events));
	}
	
	/**
	 * Registers the specified Task as one that can process the specified set of EventS
	 * @param task the Task
	 * @param events array of EventS that the Task can process
	 */
	public void registerTask(AbstractTask task, Event<Object>[] events) {
		this.eventsToTaskMap.put(TaskRegistry.getEventsKey(events), task);
	}
	
	/**
	 * Helpemr method to get a key from fully qualified class names of the specified EventS
	 * @param events Event[] array for creating key
	 * @return String representing the EventS
	 */
	public static String getEventsKey(Event<Object>[] events) {
		StringBuilder sb = new StringBuilder();
		for (Event<Object> event : events) {
			sb.append(event.getClass().getName());
			sb.append("_");
		}
		return sb.toString();
	}
	
}
