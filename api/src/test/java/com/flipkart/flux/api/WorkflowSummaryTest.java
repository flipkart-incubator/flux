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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class WorkflowSummaryTest {

    private String summaryJson;
    private ObjectMapper objectMapper ;

    @Before
    public void setUp() throws IOException {
        objectMapper = new ObjectMapper();
        summaryJson = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("workflow_summary.json"));
    }

    @Test
    public void shouldParseProperly() throws Exception {
        WorkflowSummary workflowSummary = objectMapper.readValue(summaryJson, WorkflowSummary.class);
        validatePOJO(workflowSummary, "barWF");
        validatePOJO(workflowSummary, "fooWF");

    }

    private void validatePOJO(WorkflowSummary workflowSummary, String key) {
        Map<String, WorkflowVersionStatus> summary = workflowSummary.getSummary();
        Assert.assertTrue(summary.containsKey(key));
        Assert.assertTrue(summary.get(key).getVersionStatus().containsKey("1"));
        Assert.assertTrue(summary.get(key).getVersionStatus().get("1").getStatusCount().get("running") == 1);
        Assert.assertTrue(summary.get(key).getVersionStatus().get("1").getStatusCount().get("errored") == 2);
        Assert.assertTrue(summary.get(key).getVersionStatus().get("1").getStatusCount().get("pending") == 0);
    }
}
