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

import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.Hook;
import com.flipkart.flux.impl.task.AbstractHook;
import com.flipkart.flux.impl.task.AkkaHook;

/**
 * <code>HookAndEvents</code> is a message that composes a {@link Hook} and the {@link Event}S that it processes. 
 * Used in invoking the {@link AkkaHook} Actor in akka.
 * 
 * @author regunath.balasubramanian
 *
 */
public class HookAndEvents {

	/** Member variables for this message*/
	private AbstractHook hook;
	private Event<Object>[] events;
	
	/** Constructor with all member variables */
	public HookAndEvents(AbstractHook hook, Event<Object>[] events) {
		super();
		this.hook = hook;
		this.events = events;
	}

	/** Accessor methods*/
	public AbstractHook getHook() {
		return hook;
	}
	public Event<Object>[] getEvents() {
		return events;
	}
	
}
