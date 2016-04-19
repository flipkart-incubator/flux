package com.flipkart.flux;

import com.flipkart.flux.commons.dto.WorkFlowStateSummary;
import com.flipkart.flux.commons.dto.WorkFlowStatesDetail;
import com.flipkart.flux.commons.dto.WorkflowSummary;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.google.inject.Inject;

import javax.inject.Named;


/**
 * <code>FluxRunTimeService</code> class acts as entry for talking to DAOs and may be Akka intefaces as well.
 *
 */
@Named
public class FluxRunTimeService {

    private final StateMachinesDAO stateMachinesDAO;

    @Inject
    public FluxRunTimeService(StateMachinesDAO stateMachinesDAO) {
        this.stateMachinesDAO = stateMachinesDAO;
    }



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
