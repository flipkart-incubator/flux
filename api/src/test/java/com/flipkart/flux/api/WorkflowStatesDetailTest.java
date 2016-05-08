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
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class WorkflowStatesDetailTest {

    private String statesDetail;
    private ObjectMapper objectMapper;


    @Before
    public void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        statesDetail = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("states_detail.json"));
    }

    @Test
    public void shouldParseProperly() throws Exception {
        WorkflowStatesDetail workFlowStatesDetail = objectMapper.readValue(statesDetail, WorkflowStatesDetail.class);
        Assert.assertThat(workFlowStatesDetail.getStatesDetail().get(0).getStatesDetail().get("state1").getEvents().getEventsData().get("eid1").getData(), CoreMatchers.is("jsonData"));

    }

}
