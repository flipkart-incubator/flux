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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents edge information for an FsmGraph
 * @author yogesh.nachnani
 */
public class FsmGraphEdge {
    /* The label to show on the edge */
    @JsonProperty
    private String label;
    /* The ids of the vertices the edge is incident on */
    @JsonProperty
    private Set<Long> incidentOn;
    /* The status of the edge (event) , derived from <code>com.flipkart.flux.domain.Event.EventStatus</code> */
    @JsonProperty
    private String status;
    /* Gives the source of the event */
    @JsonProperty
    private String source;
    /*Gives event data details*/
    @JsonProperty
    private String eventData;

    /* For Jackson */
    FsmGraphEdge() {
        this(null,null,null,null);
    }

    public FsmGraphEdge(String label, String status,String source,String eventData) {
        this(new HashSet<>(),label,status,source,eventData);
    }

    public FsmGraphEdge(Set<Long> incidentOn, String label, String status,String source,String eventData) {
        this.incidentOn = incidentOn;
        this.label = (label == null ? "" : label.trim());
        this.status = status;
        this.source = (source == null ? "" : source.trim());
        this.eventData = eventData;
    }

    @JsonIgnore
    public void addOutgoingVertex(Long vertexId) {
        this.incidentOn.add(vertexId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FsmGraphEdge that = (FsmGraphEdge) o;

        if (label != null ? !label.equals(that.label) : that.label != null) return false;
        if (incidentOn != null ? !incidentOn.equals(that.incidentOn) : that.incidentOn != null) return false;
        return !(status != null ? !status.equals(that.status) : that.status != null);

    }

    @Override
    public int hashCode() {
        int result = label != null ? label.hashCode() : 0;
        result = 31 * result + (incidentOn != null ? incidentOn.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FsmGraphEdge{" +
            "incidentOn=" + incidentOn +
            ", label='" + label + '\'' +
            ", status='" + status + '\'' +
            '}';
    }
}
