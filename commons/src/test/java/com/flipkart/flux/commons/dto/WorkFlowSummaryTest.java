package com.flipkart.flux.commons.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertTrue;

public class WorkFlowSummaryTest  {

    private String summaryJson;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        summaryJson = "{\"summary\":{\"barWF\":{\"versionStatus\":{\"1\":{\"statusCount\":{\"running\":1,\"pending\":0,\"errored\":2}}}},\"fooWF\":{\"versionStatus\":{\"1\":{\"statusCount\":{\"running\":1,\"pending\":0,\"errored\":2}}}}}}\n";
    }

    @Test
    public void shouldParseProperly() throws Exception {
        WorkflowSummary workflowSummary = objectMapper.readValue(summaryJson, WorkflowSummary.class);
        validatePOJO(workflowSummary, "barWF");
        validatePOJO(workflowSummary, "fooWF");

    }

    private void validatePOJO(WorkflowSummary workflowSummary, String key) {
        Map<String, WorkflowVersionStatus> summary = workflowSummary.getSummary();
        assertTrue(summary.containsKey(key));
        assertTrue(summary.get(key).getVersionStatus().containsKey("1"));
        assertTrue(summary.get(key).getVersionStatus().get("1").getStatusCount().get("running")==1);
        assertTrue(summary.get(key).getVersionStatus().get("1").getStatusCount().get("errored")==2);
        assertTrue(summary.get(key).getVersionStatus().get("1").getStatusCount().get("pending")==0);
    }
}
