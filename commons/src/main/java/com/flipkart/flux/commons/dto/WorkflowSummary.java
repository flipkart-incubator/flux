package com.flipkart.flux.commons.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@Getter
public class WorkflowSummary {

    @JsonProperty
    private Map<String, WorkflowVersionStatus> summary;

//    @JsonProperty
//    List<WorkflowInfo> workflowInfos;
//
//
//    @NoArgsConstructor(access = AccessLevel.PACKAGE)
//    static class WorkflowInfo {
//        private String name;
//        private SummaryVersion summaryVersion;
//    }
//
//    @NoArgsConstructor(access = AccessLevel.PACKAGE)
//    static class SummaryVersion {
//        private String version;
//        private List<StatusCount> statusCountList;
//    }
//
//    @NoArgsConstructor(access = AccessLevel.PACKAGE)
//    static class StatusCount {
//        private String status;
//        private Integer count;
//    }

}
