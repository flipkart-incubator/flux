package com.flipkart.flux;

import com.flipkart.flux.commons.dto.WorkFlowStateSummary;
import com.flipkart.flux.commons.dto.WorkFlowStatesDetail;
import com.flipkart.flux.commons.dto.WorkflowSummary;

import javax.inject.Named;
import javax.inject.Singleton;


/**
 * <code>FluxRunTimeService</code> class acts as entry for talking to DAOs and may be Akka intefaces as well.
 *
 */
@Named
@Singleton
public class FluxRunTimeService {

    public WorkflowSummary getTeamWorkFloWSummary(String teamName) {
        //todo
        return null;
    }

    public WorkFlowStateSummary getWorkflowStateSummary(String teamName, String workflowName, String version, String state) {
        //todo
        return null;
    }


    public WorkFlowStatesDetail getWorkflowStatesDetail(String teamName, String workflowName, String version, String state,
                                                        Integer pageSize, Integer index) {
        //todo
        return null;
    }



}
