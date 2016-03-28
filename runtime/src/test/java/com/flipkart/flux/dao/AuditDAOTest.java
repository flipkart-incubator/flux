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
import junit.framework.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.List;

/**
 * @author shyam.akirala
 */
public class AuditDAOTest {

    @Test
    public void createAuditRecordTest() {
        AuditDAO auditDAO = new AuditDAOImpl();
        AuditRecord auditRecord = new AuditRecord("test_state_machine_name", "test_state_machine_instance_id", 10L, 0, Status.initialized, new Date(), null);
        auditDAO.create(auditRecord);

        List<AuditRecord> records = auditDAO.find("test_state_machine_instance_id");
        Assert.assertNotNull(records);
    }

    @Test
    public void setStateEndTimeTest() {
        AuditDAO auditDAO = new AuditDAOImpl();
        AuditRecord auditRecord = auditDAO.find("test_state_machine_instance_id", 10L, 0);
        Date date = new Date();
        auditRecord.setStateEndTime(date);
        auditDAO.update(auditRecord);

        AuditRecord savedRecord = auditDAO.find("test_state_machine_instance_id", 10L, 0);
        Assert.assertEquals(date, savedRecord.getStateEndTime());
    }

}
