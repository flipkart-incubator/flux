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
public class WorkflowVersionStatus {

    @JsonProperty
    private Map<String, WorkFlowStatusCount> versionStatus;

}
