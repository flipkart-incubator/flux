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

package com.flipkart.flux.domain;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * <code>ClientElb</code> represents Client Cluster's Elb details.
 * This entity stores information about Client's Elb URL in MySQL DB table.
 * These Client Elb URLs' are the endpoints of client execution environment.
 *
 *  * @author akif.khan
 */
@Entity
@Table(name = "ClientElb")
public class ClientElb {

    /**
     * Unique identifier of the Client Cluster ELB
     */
    @Id
    private String id;

    /**
     * Elb URL address for this Client Cluster
     */
    private String elbUrl;

    /**
     * Time at which this Client ELB has been created
     */
    private Timestamp createdAt;

	/**
     * Time at which this Client ELB has been last updated
     */
    private Timestamp updatedAt;

    /**
     * Constructors
     */
    protected ClientElb() {
    }

    public ClientElb(String id, String elbUrl) {
        this.id = id;
        this.elbUrl = elbUrl;
    }

    /**
     * Accessor/Mutator methods
     */
    public String getId() {
        return id;
    }
    public String getElbUrl() {
        return elbUrl;
    }
    public void setElbUrl(String elbUrl) {
        this.elbUrl = elbUrl;
    }
    public Timestamp getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}
	public Timestamp getUpdatedAt() {
		return updatedAt;
	}
	public void setUpdatedAt(Timestamp updatedAt) {
		this.updatedAt = updatedAt;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientElb)) return false;

        ClientElb that = (ClientElb) o;

        if (elbUrl != null ? !elbUrl.equals(that.elbUrl) : that.elbUrl != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (elbUrl != null ? elbUrl.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientElb{" +
                " id='" + id + '\'' +
                ", elbUrl='" + elbUrl + '\'' +
                '}';
    }

}
