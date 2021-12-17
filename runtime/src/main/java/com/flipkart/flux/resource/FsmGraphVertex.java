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

package com.flipkart.flux.resource;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Used in visualisation APIs, this Represents a vertex in the graph representation of an fsm
 * Note that we cannot use plain State Names as vertices since multiple states can have the same name
 * @author yogesh.nachnani
 */
public class FsmGraphVertex {

    /* The unique id for a vertex */
    @JsonProperty
    private Long id;
    /* Label to show on the vertex */
    @JsonProperty
    private String label;
    /* Task status*/
    @JsonProperty
    private String status;

    @JsonProperty
    private Long executionVersion;

    /* For Jackson */
    FsmGraphVertex() {
        this(null,null,null, null);
    }

    public FsmGraphVertex(Long id, String label, String status, Long executionVersion) {
        this.id = id;
        this.label = (label == null ? "" : label.trim());
        this.status = status;
        this.executionVersion = executionVersion;
    }

    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FsmGraphVertex that = (FsmGraphVertex) o;

        if (!id.equals(that.id)) return false;
        if (!executionVersion.equals(that.executionVersion)) return false;
        return label.equals(that.label) && status.equals(that.status);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + label.hashCode();
        result = 31 * result + status.hashCode();
        result = 31 * result + executionVersion.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return id + ":" + label+ ":" + status + ":" +executionVersion;
    }
}