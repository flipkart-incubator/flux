package com.flipkart.flux.commons.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;



import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
public class WorkFlowStatesDetail {

    @Getter
    @JsonProperty
    private List<WorkFlowStateDetail> statesDetail;

}
