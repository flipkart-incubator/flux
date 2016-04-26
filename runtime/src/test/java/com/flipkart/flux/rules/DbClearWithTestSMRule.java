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

package com.flipkart.flux.rules;

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

    @Inject
    public DbClearWithTestSMRule(DbClearRule dbClearRule, TestSMRule testSMRule) {
        this.dbClearRule = dbClearRule;
        this.testSMRule = testSMRule;
    }

    @Override
    protected void before() throws Throwable{
        //clear db
        dbClearRule.before();

        //create state machine for test purpose
        testSMRule.before();
    }

    /** Returns test state machine instance id*/
    public String getStateMachineId() {
        return this.testSMRule.getStateMachineId();
    }

    /** Returns test state id*/
    public String getStateId() {
        return this.testSMRule.getStateId();
    }

}
