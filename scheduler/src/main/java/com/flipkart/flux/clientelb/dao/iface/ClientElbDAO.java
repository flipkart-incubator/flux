/*
 * Copyright 2012-2018, the original author or authors.
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

package com.flipkart.flux.clientelb.dao.iface;

import com.flipkart.flux.domain.ClientElb;

import java.util.List;

/**
 * <code>ClientElbDAO</code> is an interface to perform
 *  CRUD operations on Client Elb DB table
 *
 * @author akif.khan
 */
public interface ClientElbDAO {

    /**
     * Creates client elb entry in DB and returns saved object
     */
    ClientElb create(ClientElb clientElb);

    /**
     * Retrieves Client Elb from DB by it's unique identifier
     */
    ClientElb findById(String clientElbId);

    /**
     * Updates Elb Url of a client in DB
     */
    void updateElbUrl(String clientElbId, String clientElbUrl);

    /**
     * Delete Client Elb entry from DB
     */
    void delete(String clientElbId);
}
