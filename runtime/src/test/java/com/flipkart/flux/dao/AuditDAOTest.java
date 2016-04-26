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

package com.flipkart.flux.dao;

import com.flipkart.flux.dao.iface.AuditDAO;
import com.flipkart.flux.domain.AuditRecord;
import com.flipkart.flux.domain.Status;
import com.flipkart.flux.rules.DbClearWithTestSMRule;
import com.flipkart.flux.runner.GuiceJunit4Runner;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

/**
 * <code>AuditDAOTest</code> class tests the functionality of {@link AuditDAO} using JUnit tests.
 * @author shyam.akirala
 * @author kartik.bommepally
 */
@RunWith(GuiceJunit4Runner.class)
public class AuditDAOTest {

    @Inject
    AuditDAO auditDAO;

    @Inject
    @Rule
    public DbClearWithTestSMRule dbClearWithTestSMRule;

    @Before
    public void setup() {}

    @Test
    public void createAuditRecordTest() {
        AuditRecord auditRecord = new AuditRecord(dbClearWithTestSMRule.getStateMachineId(), dbClearWithTestSMRule.getStateId(), 0, Status.completed, null, null);
        Long recordId = auditDAO.create(auditRecord).getId();

        AuditRecord auditRecord1 = auditDAO.findById(recordId);
        Assert.assertEquals(auditRecord, auditRecord1);
    }
}