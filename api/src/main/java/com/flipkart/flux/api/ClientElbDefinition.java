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

package com.flipkart.flux.api;

/**
 * <code>ClientElbDefinition</code> defines a ClientElb details to the system
 *
 *  * @author akif.khan
 */
public class ClientElbDefinition {

    /** Client Elb id*/
    private String id;

    /** Client Elb URL*/
    private String elbUrl;

    /* To be used only by jackson */
    ClientElbDefinition() {
    }

    /** Constructor*/
    public ClientElbDefinition(String id, String elbUrl) {
        this.id = id;
        this.elbUrl = elbUrl;
    }

    /** Accessors/Mutators for member variables*/
    public String getElbUrl() {
        return elbUrl;
    }
    public void setElbUrl(String elbUrl) {
        this.elbUrl = elbUrl;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientElbDefinition)) return false;

        ClientElbDefinition that = (ClientElbDefinition) o;

        if (elbUrl != null ? !elbUrl.equals(that.elbUrl) : that.elbUrl != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = elbUrl != null ? elbUrl.hashCode() : 0;

        return result;
    }

    @Override
    public String toString() {
        return "ClientElbDefinition{" +
                "elbUrl='" + elbUrl + '\'' +
                "id='" + id + '\'' +
                '}';
    }
}
