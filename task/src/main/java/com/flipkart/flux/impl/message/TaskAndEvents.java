package com.flipkart.flux.impl.message;

import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.Task;
import com.flipkart.flux.impl.task.AkkaTask;

/**
 * <code>HookAndEvents</code> is a message that composes a {@link Task} identifier and the {@link Event}S that it processes.
 * Used in invoking the {@link AkkaTask} Actor in akka.
 *
 * @author yogesh.nachnani
 *
 */
public class TaskAndEvents {
    /* The string that uniquely identifies a client task to be executed */
    private String taskIdentifier;
    /* The set of events that have presently unblocked the task and whose data will be utilised during task execution */
    private Event[] events;

    public TaskAndEvents(String taskIdentifier, Event[] events) {
        this.taskIdentifier = taskIdentifier;
        this.events = events;
    }

    public String getTaskIdentifier() {
        return taskIdentifier;
    }

    public Event[] getEvents() {
        return this.events;
    }
}
