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

import com.flipkart.flux.api.EventDefinition;
import com.flipkart.flux.api.StateDefinition;
import com.flipkart.flux.api.StateMachineDefinition;
import com.flipkart.flux.client.model.Event;
import com.flipkart.flux.client.model.Task;
import com.flipkart.flux.client.model.Workflow;

import javax.inject.Singleton;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Workflow used in <code>WorkflowInterceptorTest</code> to test e2e interception
 */
@Singleton
public class SimpleWorkflowForTest {

    /* A simple workflow that goes about creating tasks and making merry */
    @Workflow(version = 1)
    public void simpleDummyWorkflow(StringEvent someString, IntegerEvent someInteger) {
        final StringEvent newString = simpleStringModifyingTask(someString);
        final IntegerEvent someNewInteger = simpleAdditionTask(someInteger);
        someTaskWithIntegerAndString(newString, someNewInteger);
    }

    public static class StringEvent implements Event {
        private String aString;
        public StringEvent(String aString) {
            this.aString = aString;
        }
    }
    public static class IntegerEvent implements Event {
        private Integer anInteger;
        public IntegerEvent(Integer anInteger) {
            this.anInteger = anInteger;
        }
    }


    /*
        This is a bad workflow. Good workflows don't return anything.
        The runtime will not allow a bad workflow. Such things are just not done, you see
     */
    @Workflow(version = 2)
    public int badWorkflow() {
        return 1;
    }

    @Task(version = 2,retries = 2,timeout = 2000l)
    public StringEvent simpleStringModifyingTask(StringEvent someString) {
        return new StringEvent("randomBs" + someString);
    }

    @Task(version = 1, retries = 2, timeout = 3000l)
    public IntegerEvent simpleAdditionTask(IntegerEvent i) {
        return new IntegerEvent(i.anInteger+2);
    }

    @Task(version = 3, retries = 0, timeout = 1000l)
    public void someTaskWithIntegerAndString(StringEvent someString, IntegerEvent someInteger) {
        //blah
    }

    @Task(version = 1, retries = 2, timeout = 2000l)
    public void badWorkflowWithNonEventParams(String foo) {
        // Doesn't matter. This is a bad workflow that works on strings as parameters. Basically we don't allow
        // any parameters that are not some Subtypes of Event
    }
    /**
     * Needed a place to derive the above workflow's expected definition (<code>StateMachineDefinition</code>)
     * What better place to get the same than from the horse's mouth, eh?
     *
     */
    protected StateMachineDefinition getEquivalentStateMachineDefintion() throws NoSuchMethodException {

        Set<StateDefinition> expectedStateDefs = new HashSet<>();

        final Method simpleStringModifyingTaskMethod = SimpleWorkflowForTest.class.getDeclaredMethod("simpleStringModifyingTask", StringEvent.class);
        final EventDefinition stringModifyingTaskEventDef = new EventDefinition(new MethodId(simpleStringModifyingTaskMethod).getPrefix()+ "_com.flipkart.flux.client.intercept.SimpleWorkflowForTest.StringEvent_arg0",""); //TODO: CHANGE IT
        expectedStateDefs.add(new StateDefinition(2l, "simpleStringModifyingTask", null, null,
            new MethodId(simpleStringModifyingTaskMethod).toString(), null,
            2l, 2000l, Collections.singleton(stringModifyingTaskEventDef)));

        final Method simpleAdditionTaskMethod = SimpleWorkflowForTest.class.getDeclaredMethod("simpleAdditionTask", IntegerEvent.class);
        final EventDefinition simpleAdditionTaskEventDef = new EventDefinition(new MethodId(simpleAdditionTaskMethod).getPrefix() + "_com.flipkart.flux.client.intercept.SimpleWorkflowForTest.IntegerEvent_arg0", ""); //TODO: CHANGE IT
        expectedStateDefs.add(new StateDefinition(1l, "simpleAdditionTask", null, null,
            new MethodId(simpleAdditionTaskMethod).toString(), null,
            2l, 3000l, Collections.singleton(simpleAdditionTaskEventDef)));


        final Method someTaskWithIntegerAndStringMethod = SimpleWorkflowForTest.class.getDeclaredMethod("someTaskWithIntegerAndString", StringEvent.class, IntegerEvent.class);
        final Set<EventDefinition> expectedEventDefsForIntStringTask = new HashSet<>();
        expectedEventDefsForIntStringTask.add(new EventDefinition(new MethodId(someTaskWithIntegerAndStringMethod).getPrefix() + "_com.flipkart.flux.client.intercept.SimpleWorkflowForTest.StringEvent_arg0", "")); //TODO: CHANGE IT
        expectedEventDefsForIntStringTask.add(new EventDefinition(new MethodId(someTaskWithIntegerAndStringMethod).getPrefix() + "_com.flipkart.flux.client.intercept.SimpleWorkflowForTest.IntegerEvent_arg1", "")); //TODO: CHANGE IT
        expectedStateDefs.add(new StateDefinition(3l, "someTaskWithIntegerAndString", null,
            null, new MethodId(someTaskWithIntegerAndStringMethod).toString(), null,
            0l, 1000l, expectedEventDefsForIntStringTask));


        return new StateMachineDefinition("",new MethodId(SimpleWorkflowForTest.class.getDeclaredMethod("simpleDummyWorkflow", StringEvent.class, IntegerEvent.class)).toString(),1l,expectedStateDefs);

    }

}
