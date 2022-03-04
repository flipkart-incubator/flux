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

/**
 * The @DAO interface for @AuditRecord entity
 * @author regu.b
 *
 */
public interface AuditDAOV1 extends DAO <AuditRecord> {

	/** The max length for error message in audit data store*/
	public static final int ERROR_MSG_LENGTH_IN_AUDIT = 995;
	
}
