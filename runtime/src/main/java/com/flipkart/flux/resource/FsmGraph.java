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
import com.flipkart.flux.domain.AuditRecord;

import java.util.*;

/**
 * Used in visualisation APIs, this representation makes it easier to draw a diagramatic representation of the SM.
 * This is basically an adjacency list, where vertex is a <code>FsmGraphVertext</code> and we have
 * <code>FsmGraphEdge</code> to represent Edges. An (FsmGraphVertex,FsmGraphEdge) mapping means that the FsmGraphEdge is
 * the output of the given FsmGraphVertex node and is incident on the nodes that it contains
 * Note: We could have chosen to use a simple Map instead of this encapsulation to represent graph data, however it helps to
 * keep this representation since we may want to send additional details in the future (say audit trails)
 * @author yogesh.nachnani
 */
public class FsmGraph {

    /* The adjacency-list representation of a graph */
    @JsonProperty
    private Map<FsmGraphVertex,FsmGraphEdge> fsmGraphData;
    @JsonProperty
    private Set<FsmGraphEdge> initStateEdges;
    @JsonProperty
    private List<AuditRecord> auditData;

    @JsonProperty
    private List<Long> erroredStateIds;

    @JsonProperty
    private Long stateMachineId;

    @JsonProperty
    private String correlationId;

    @JsonProperty
    private Long fsmVersion;

    @JsonProperty
    private String fsmName;

    public FsmGraph(){
        this.fsmGraphData = new HashMap<>();
        this.initStateEdges = new HashSet<>();
    }

    @JsonIgnore
    public void addVertex(FsmGraphVertex vertex, FsmGraphEdge fsmGraphEdge) {
        if (!this.fsmGraphData.containsKey(vertex)) {
            this.fsmGraphData.put(vertex,fsmGraphEdge);
        }
    }
    @JsonIgnore
    public void addOutgoingEdge(FsmGraphVertex from, Long to) {
        this.fsmGraphData.get(from).addOutgoingVertex(to);
    }

    @JsonIgnore
    public void addInitStateEdge(FsmGraphEdge edge) {
        this.initStateEdges.add(edge);
    }

    public void setAuditData(List<AuditRecord> auditData) {
        this.auditData = auditData;
    }

    public void setStateMachineId(Long stateMachineId) {
        this.stateMachineId = stateMachineId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public void setFsmVersion(Long fsmVersion) {
        this.fsmVersion = fsmVersion;
    }

    public void setFsmName(String fsmName) {
        this.fsmName = fsmName;
    }

    public void setErroredStateIds(List<Long> erroredStateIds) {
        this.erroredStateIds = erroredStateIds;
    }

    public List<Long> getErroredStateIds() {
        return erroredStateIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FsmGraph fsmGraph = (FsmGraph) o;

        return !(fsmGraphData != null ? !fsmGraphData.equals(fsmGraph.fsmGraphData) : fsmGraph.fsmGraphData != null);

    }

    @Override
    public int hashCode() {
        return fsmGraphData != null ? fsmGraphData.hashCode() : 0;

    }

    @Override
    public String toString() {
        return "FsmGraph{" +
            "fsmGraphData=" + fsmGraphData +
            '}';
    }


}
