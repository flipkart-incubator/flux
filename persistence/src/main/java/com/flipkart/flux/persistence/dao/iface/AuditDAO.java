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

package com.flipkart.flux.persistence.dao.iface;

import com.flipkart.flux.domain.AuditRecord;
import org.hibernate.Session;

import java.util.List;

/**
 * <code>AuditDAO</code> interface provides methods to perform CR operations on {@link AuditRecord}
 *
 * @author shyam.akirala
 */
public interface AuditDAO {

	public static final int ERROR_MSG_LENGTH_IN_AUDIT = 995;
	
    /**
     * Retrieves all the audit logs which belongs to a particular state machine
     */
    List<AuditRecord> findBySMInstanceId(String stateMachineInstanceId);

    /**
     * Creates Audit record and returns the saved object
     */
    AuditRecord create(String stateMachineId, AuditRecord auditRecord);

    /**
     * Retrieves Audit record by it's unique identifier
     */
    public AuditRecord findById(String stateMachineId, Long id);

    /**
     * Creates Audit record using the parameter session and returns the saved object
     */
    AuditRecord create_NonTransactional(AuditRecord auditRecord, Session session);
    
    /**
     * Enable persisting  audit record to database.
     */
    void enableAuditRecord(boolean value);
}