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
import com.flipkart.flux.client.model.Task;
import com.flipkart.flux.client.model.Workflow;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.times;

/**
 * Workflow used in <code>WorkflowInterceptorTest</code> to test e2e interception
 */
@Singleton
public class SimpleWorkflowForTest {

    /* A simple workflow that goes about creating tasks and making merry */
    @Workflow(version = 1)
    public void simpleDummyWorkflow(String someString, Integer someInteger) {
        final String newString = simpleStringModifyingTask(someString);
        final Integer someNewInteger = simpleAdditionTask(someInteger);
        someTaskWithIntegerAndString(newString, someNewInteger);
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
    public String simpleStringModifyingTask(String someString) {
        return "randomBs" + someString;
    }

    @Task(version = 1, retries = 2, timeout = 3000l)
    public Integer simpleAdditionTask(Integer i) {
        return i+2;
    }

    @Task(version = 3, retries = 0, timeout = 1000l)
    public void someTaskWithIntegerAndString(String someString, Integer someInteger) {
        //blah
    }
    /**
     * Needed a place to derive the above workflow's expected definition (<code>StateMachineDefinition</code>)
     * What better place to get the same than from the horse's mouth, eh?
     *
     */
    protected StateMachineDefinition getEquivalentStateMachineDefintion() {

        Set<StateDefinition> expectedStateDefs = new HashSet<>();

        final EventDefinition stringModifyingTaskEventDef = new EventDefinition("com.flipkart.flux.client.intercept.SimpleWorkflowForTest_simpleStringModifyingTask_java.lang.String_arg0");
        expectedStateDefs.add(new StateDefinition(2l, "simpleStringModifyingTask", null, null,
            "com.flipkart.flux.client.intercept.SimpleWorkflowForTest_simpleStringModifyingTask_java.lang.String_java.lang.String", null,
            2l, 2000l, Collections.singleton(stringModifyingTaskEventDef)));

        final EventDefinition simpleAdditionTaskEventDef = new EventDefinition("com.flipkart.flux.client.intercept.SimpleWorkflowForTest_simpleAdditionTask_java.lang.Integer_arg0");
        expectedStateDefs.add(new StateDefinition(1l, "simpleAdditionTask", null, null,
            "com.flipkart.flux.client.intercept.SimpleWorkflowForTest_simpleAdditionTask_java.lang.Integer_java.lang.Integer", null,
            2l, 3000l, Collections.singleton(simpleAdditionTaskEventDef)));


        final Set<EventDefinition> expectedEventDefsForIntStringTask = new HashSet<>();
        expectedEventDefsForIntStringTask.add(new EventDefinition("com.flipkart.flux.client.intercept.SimpleWorkflowForTest_someTaskWithIntegerAndString_java.lang.String_arg0"));
        expectedEventDefsForIntStringTask.add(new EventDefinition("com.flipkart.flux.client.intercept.SimpleWorkflowForTest_someTaskWithIntegerAndString_java.lang.Integer_arg1"));
        expectedStateDefs.add(new StateDefinition(3l, "someTaskWithIntegerAndString", null,
            null, "com.flipkart.flux.client.intercept.SimpleWorkflowForTest_someTaskWithIntegerAndString_void_java.lang.String_java.lang.Integer", null,
            0l, 1000l, expectedEventDefsForIntStringTask));


        return new StateMachineDefinition("","com.flipkart.flux.client.intercept.SimpleWorkflowForTest_simpleDummyWorkflow_void_java.lang.String_java.lang.Integer",1l,expectedStateDefs);

    }

}
