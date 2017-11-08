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

package com.flipkart.flux.client.intercept;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.api.EventData;
import com.flipkart.flux.api.EventDefinition;
import com.flipkart.flux.api.StateDefinition;
import com.flipkart.flux.api.StateMachineDefinition;
import com.flipkart.flux.client.model.*;

import javax.inject.Singleton;
import java.lang.reflect.Method;
import java.util.*;

import static com.flipkart.flux.client.constant.ClientConstants.CLIENT;
import static com.flipkart.flux.client.constant.ClientConstants._VERSION;

/**
 * Workflow used in <code>WorkflowInterceptorTest</code> to test e2e interception
 */
@Singleton
public class SimpleWorkflowForTest {

    public static final String STRING_EVENT_NAME = "Some String Event";
    public static final String INTEGER_EVENT_NAME = "Some Integer Event";

    /* A simple workflow that goes about creating tasks and making merry */
    @Workflow(version = 1)
    public void simpleDummyWorkflow(StringEvent someString, IntegerEvent someInteger) {
        final StringEvent newString = simpleStringModifyingTask(someString);
        final IntegerEvent someNewInteger = simpleAdditionTask(someInteger);
        someTaskWithIntegerAndString(newString, someNewInteger);
    }

    /* A simple workflow that goes about creating tasks and making merry. Sometimes both these fight over whose the merrier */
    @Workflow(version = 1)
    public void simpleDummyWorkflowWithExternalEvent(IntegerEvent someInteger) {
        final StringEvent newString = waitForExternalEvent(null, someInteger);
        final StringEvent anotherString = waitForExternalEvent((StringEvent) null);
        someTaskWithIntegerAndString(newString, someInteger);
    }

    /* A simple workflow that takes in a parameter which carries a correlationId */
    @Workflow(version = 1)
    public void simpleDummyWorkflowWithCorrelationEvent(StringEventWithContext someString,IntegerEvent someInteger) {
        final IntegerEvent someNewInteger = simpleAdditionTask(someInteger);
    }

    /* A simple workflow that takes in variable number of params tasks and making merry */
    @Workflow(version = 1)
    public void simpleDummyWorkflow(StringEvent...stringEvents) {
        final StringEvent newString = simpleStringModifyingTask(stringEvents[0]);
    }

    /*
        This is a bad workflow. Good workflows don't return anything.
        The runtime will not allow a bad workflow. Such things are just not done, you see
     */
    @Workflow(version = 2)
    public int badWorkflow() {
        return 1;
    }

    @Task(version = 1,retries = 2,timeout = 2000l)
    public StringEvent simpleStringModifyingTask(StringEvent someString) {
        return new StringEvent("randomBs" + someString);
    }

    @Task(version = 1, retries = 2, timeout = 2000l)
    public StringEvent waitForExternalEvent(@ExternalEvent("someExternalEvent") StringEvent someString,IntegerEvent integerEvent) {
        return new StringEvent(integerEvent.anInteger.toString() + someString);
    }
    @Task(version = 1, retries = 2, timeout = 2000l)
    public StringEvent waitForExternalEvent(@ExternalEvent("someExternalEvent") StringEvent someString) {
        return new StringEvent(someString.toString());
    }
    @Task(version = 1, retries = 2, timeout = 2000l)
    public StringEvent waitForExternalEvent(@ExternalEvent("someExternalEvent") IntegerEvent integerEvent) {
        return new StringEvent(integerEvent.anInteger.toString());
    }



    @Task(version = 1, retries = 2, timeout = 3000l)
    public IntegerEvent simpleAdditionTask(IntegerEvent i) {
        return new IntegerEvent(i.anInteger+2);
    }

    @Task(version = 1, retries = 0, timeout = 1000l)
    public void someTaskWithIntegerAndString(StringEvent someString, IntegerEvent someInteger) {
        //blah
    }

    @Task(version = 1, retries = 2, timeout = 2000l)
    public void badWorkflowWithNonEventParams(String foo) {
        // Doesn't matter. This is a bad workflow that works on strings as parameters. Basically we don't allow
        // any parameters that are not some Subtypes of Event
    }
    @Task(version = 1, retries = 2, timeout = 2000l)
    public void badWorkflowWithCollectionOfEvents(Collection<Event> foo) {
        // Doesn't matter. This is a bad workflow that has a collection of events as parameter. Basically we don't allow
        // any parameters that are not some Subtypes of Event
    }

    /**
     * Needed a place to derive the above workflow's expected definition (<code>StateMachineDefinition</code>)
     * What better place to get the same than from the horse's mouth, eh?
     *
     * @param stringEvent
     * @param integerEvent
     */
    protected StateMachineDefinition getEquivalentStateMachineDefintion(StringEvent stringEvent, IntegerEvent integerEvent) throws NoSuchMethodException, JsonProcessingException {

        Set<StateDefinition> expectedStateDefs = new HashSet<>();

        final Method simpleStringModifyingTaskMethod = SimpleWorkflowForTest.class.getDeclaredMethod("simpleStringModifyingTask", StringEvent.class);
        final EventDefinition expectedInputForStringModifyingTask = new EventDefinition(STRING_EVENT_NAME+"0","com.flipkart.flux.client.intercept.SimpleWorkflowForTest$StringEvent");
        final EventDefinition outputOfStringModifyingTask = new EventDefinition("com.flipkart.flux.client.intercept.SimpleWorkflowForTest$StringEvent2", "com.flipkart.flux.client.intercept.SimpleWorkflowForTest$StringEvent");
        expectedStateDefs.add(new StateDefinition(1l, "simpleStringModifyingTask", null, null,
            new MethodId(simpleStringModifyingTaskMethod).toString()+_VERSION+"1", null,
            2l, 2000l, Collections.singletonList(expectedInputForStringModifyingTask), outputOfStringModifyingTask));

        final Method simpleAdditionTaskMethod = SimpleWorkflowForTest.class.getDeclaredMethod("simpleAdditionTask", IntegerEvent.class);
        final EventDefinition expectedInputForSimpleAdditionTask = new EventDefinition(INTEGER_EVENT_NAME+"1", "com.flipkart.flux.client.intercept.SimpleWorkflowForTest$IntegerEvent");
        final EventDefinition outputOfSimpleAdditionTask = new EventDefinition("com.flipkart.flux.client.intercept.SimpleWorkflowForTest$IntegerEvent3", "com.flipkart.flux.client.intercept.SimpleWorkflowForTest$IntegerEvent");
        expectedStateDefs.add(new StateDefinition(1l, "simpleAdditionTask", null, null,
            new MethodId(simpleAdditionTaskMethod).toString()+_VERSION+"1", null,
            2l, 3000l, Collections.singletonList(expectedInputForSimpleAdditionTask), outputOfSimpleAdditionTask));


        final Method someTaskWithIntegerAndStringMethod = SimpleWorkflowForTest.class.getDeclaredMethod("someTaskWithIntegerAndString", StringEvent.class, IntegerEvent.class);
        final List<EventDefinition> expectedEventDefsForIntStringTask = new LinkedList<>();

        expectedEventDefsForIntStringTask.add(outputOfStringModifyingTask);
        expectedEventDefsForIntStringTask.add(outputOfSimpleAdditionTask);
        expectedStateDefs.add(new StateDefinition(1l, "someTaskWithIntegerAndString", null,
            null, new MethodId(someTaskWithIntegerAndStringMethod).toString()+_VERSION+"1", null,
            0l, 1000l, expectedEventDefsForIntStringTask, null));

        final ObjectMapper objectMapper = new ObjectMapper();
        Set<EventData> expectedEventData = new HashSet<EventData>() {{
            add(new EventData(STRING_EVENT_NAME+"0","com.flipkart.flux.client.intercept.SimpleWorkflowForTest$StringEvent",objectMapper.writeValueAsString(stringEvent),CLIENT));
            add(new EventData(INTEGER_EVENT_NAME+"1","com.flipkart.flux.client.intercept.SimpleWorkflowForTest$IntegerEvent",objectMapper.writeValueAsString(integerEvent),CLIENT));
        }};
        return new StateMachineDefinition("",new MethodId(SimpleWorkflowForTest.class.getDeclaredMethod("simpleDummyWorkflow", StringEvent.class, IntegerEvent.class)).toString()+"_version1",1l,expectedStateDefs, expectedEventData, null);

    }

    public static class StringEvent implements Event {
        @JsonProperty
        private String aString;

        StringEvent() {
        }

        public StringEvent(String aString) {
            this.aString = aString;
        }

        @Override
        public String name() {
            return STRING_EVENT_NAME;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            StringEvent that = (StringEvent) o;

            return aString.equals(that.aString);

        }

        @Override
        public int hashCode() {
            return aString.hashCode();
        }

        @Override
        public String toString() {
            return "StringEvent{" +
                "aString='" + aString + '\'' +
                '}';
        }
    }

    public static class IntegerEvent implements Event {
        @JsonProperty
        private Integer anInteger;
        IntegerEvent(){

        }
        public IntegerEvent(Integer anInteger) {
            this.anInteger = anInteger;
        }

        @Override
        public String name() {
            return INTEGER_EVENT_NAME;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            IntegerEvent that = (IntegerEvent) o;

            return anInteger.equals(that.anInteger);

        }

        @Override
        public int hashCode() {
            return anInteger.hashCode();
        }

        @Override
        public String toString() {
            return "IntegerEvent{" +
                "anInteger=" + anInteger +
                '}';
        }
    }

    public static class StringEventWithContext extends StringEvent {
        @JsonProperty
        @CorrelationId
        private String contextId;

        public StringEventWithContext(String aString, String contextId) {
            super(aString);
            this.contextId = contextId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            StringEventWithContext that = (StringEventWithContext) o;

            return contextId.equals(that.contextId);

        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + contextId.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "StringEventWithContext{" +
                "contextId='" + contextId + '\'' +
                '}';
        }
    }

}
