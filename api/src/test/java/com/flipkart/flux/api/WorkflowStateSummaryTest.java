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
package com.flipkart.flux.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class WorkflowStateSummaryTest {

    private String stateSummary;
    private ObjectMapper objectMapper ;

    @Before
    public void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        stateSummary = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("state_summary.json"));
    }

    @Test
    public void shouldParseProperly() throws Exception {
        WorkflowStateSummary workFlowStateSummary = objectMapper.readValue(stateSummary, WorkflowStateSummary.class);
        Assert.assertThat(workFlowStateSummary.getStateSummaries().get(0).getSummary().get("state2").getCount(), Is.is(9));
    }

}
