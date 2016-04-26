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
package com.flipkart.flux;

import com.flipkart.flux.commons.dto.WorkflowStateSummary;
import com.flipkart.flux.commons.dto.WorkflowStatesDetail;
import com.flipkart.flux.commons.dto.WorkflowSummary;

import javax.inject.Named;
import javax.inject.Singleton;


/**
 * <code>FluxRunTimeService</code> class acts as entry for talking to DAOs and may be Akka intefaces as well.
 *  @author ashish.bhutani
 */
@Named
@Singleton
public class FluxRuntimeService {

    public WorkflowSummary getTeamWorkfloWSummary(String teamName) {
        //todo
        return null;
    }

    public WorkflowStateSummary getWorkflowStateSummary(String teamName, String workflowName, String version, String state) {
        //todo
        return null;
    }


    public WorkflowStatesDetail getWorkflowStatesDetail(String teamName, String workflowName, String version, String state,
                                                        Integer pageSize, Integer index) {
        //todo
        return null;
    }



}
