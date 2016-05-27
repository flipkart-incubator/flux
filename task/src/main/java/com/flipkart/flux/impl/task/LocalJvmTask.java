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

package com.flipkart.flux.impl.task;

import com.flipkart.flux.client.registry.Executable;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.FluxError;
import javafx.util.Pair;

import java.lang.reflect.Method;

/**
 * A task that can be executed locally within the same JVM
 * @author yogesh.nachnani
 * */
public class LocalJvmTask extends AbstractTask {

    private final Executable toInvoke;

    public LocalJvmTask(Executable toInvoke) {
        this.toInvoke = toInvoke;
    }

    @Override
    public String getName() {
        return this.toInvoke.getName();
    }

    @Override
    public String getTaskGroupName() {
        /* TODO - pull from deployment unit */
        return "flux";
    }

    @Override
    public int getExecutionConcurrency() {
        /* TODO - pull from deployment unit/ client definition */
        return 10;
    }

    @Override
    public int getExecutionTimeout() {
        return (int)toInvoke.getTimeout(); // TODO - fix this. Let all timeouts be in int
    }

    @Override
    public Pair<Event, FluxError> execute(Event[] events) {
        Object[] parameters = new Object[events.length];
        for (int i = 0 ; i < events.length ; i++) {
            parameters[i] = events[i].getEventData();
        }
        final Object returnObject = toInvoke.execute(parameters);
        // TODO - fix this
        return new Pair<>(new Event("foo",returnObject.getClass().getCanonicalName(), Event.EventStatus.triggered,events[0].getStateMachineInstanceId(),returnObject,"managedRuntime"),null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocalJvmTask that = (LocalJvmTask) o;

        return toInvoke.equals(that.toInvoke);

    }

    @Override
    public int hashCode() {
        return toInvoke.hashCode();
    }
}
