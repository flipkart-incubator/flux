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

package com.flipkart.flux.examples.forkjoin;

import com.flipkart.flux.client.model.Task;
import com.flipkart.flux.client.model.Workflow;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Situation: N similar operations need to be executed independent of each other. N is determined only at runtime
 * The result of these N operations needs to be collected and then acted upon.
 * This is called a dynamic fork-join & this class demonstrates how a workflow with a dynamic fork-join can be implemented
 * @author yogesh.nachnani
 */
public class SimpleForkJoinWorkflow {

    private final SimpleTasker simpleTasker;

    @Inject
    public SimpleForkJoinWorkflow(SimpleTasker simpleTasker) {
        this.simpleTasker = simpleTasker;
    }

    @Workflow(version = 1)
    public void generateRandomPermutation(String someInputString) {

        /* The value of n is available only at runtime */
        final int n = someInputString.length();

        List<Character> listOfCharacters = new ArrayList<>();
        for (int i = 0; i < n ; i ++) {
            /* fork returns the character at index i */
            listOfCharacters.add(simpleTasker.fork(someInputString, i));
        }

    /*
     The following method simply concatenates the given list of characters into a message and saves it to DB.
     Note:Since the fork operations are not inter-dependent on each other, they may be started & completed in any order.
     Thus, the final message may be a different permutation of the given input variable "someInputString".
    */
        simpleTasker.join(listOfCharacters);
    }

}
