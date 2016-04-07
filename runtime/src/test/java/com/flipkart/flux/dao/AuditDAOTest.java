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

import com.flipkart.flux.guice.modules.HibernateModule;
import com.flipkart.flux.dao.iface.AuditDAO;
import com.flipkart.flux.domain.AuditRecord;
import com.flipkart.flux.domain.Status;
import com.google.inject.Guice;
import com.google.inject.Injector;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import java.util.List;

/**
 * @author shyam.akirala
 */
public class AuditDAOTest {

    private Injector injector;

    @Before
    public void setup() {
        injector = Guice.createInjector(new HibernateModule());
    }

    @Test
    public void createAuditRecordTest() {
        AuditDAO auditDAO = injector.getInstance(AuditDAO.class);
        AuditRecord auditRecord = new AuditRecord("test_state_machine_instance_id", "abcd-xyz", 0, Status.running, null, null);
        auditDAO.create(auditRecord);

        List<AuditRecord> records = auditDAO.findBySMInstanceId("test_state_machine_instance_id");
        Assert.assertNotNull(records);
    }
}