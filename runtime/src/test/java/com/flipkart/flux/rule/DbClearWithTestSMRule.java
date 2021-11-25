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

package com.flipkart.flux.rule;

import com.flipkart.flux.domain.StateMachine;
import org.junit.rules.ExternalResource;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * <code>DbClearWithTestSMRule</code> is a Junit Rule which clears db tables before running a test and also creates a test state machine for testing purpose.
 * @author shyam.akirala
 */
@Singleton
public class DbClearWithTestSMRule extends ExternalResource {

    private DbClearRule dbClearRule;

    private TestSMRule testSMRule;

    private TestReplayableSMRule testReplayableSMRule;

    @Inject
    public DbClearWithTestSMRule(DbClearRule dbClearRule, TestSMRule testSMRule, TestReplayableSMRule
            testReplayableSMRule) {
        this.dbClearRule = dbClearRule;
        this.testSMRule = testSMRule;
        this.testReplayableSMRule = testReplayableSMRule;
    }

    @Override
    protected void before() throws Throwable{
        //clear db
        dbClearRule.before();

        //create state machine for test purpose
        testSMRule.before();

        //create state machine with replayable state for test purpose
        testReplayableSMRule.before();
    }

    /** Returns test state machine*/
    public StateMachine getStateMachine() {
        return this.testSMRule.getStateMachine();
    }

    /** Returns test state machine with replayable state */
    public StateMachine getStateMachineWithReplayableState() {
        return this.testReplayableSMRule.getStateMachineWithReplayableState();
    }

    /** Returns test state machine with multiple replayable states */
    public StateMachine getStateMachineWithMultipleReplayableStates() {
        return this.testReplayableSMRule.getStateMachine2WithReplayableState();
    }
}
