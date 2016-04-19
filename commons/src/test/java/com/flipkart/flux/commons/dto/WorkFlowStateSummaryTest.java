package com.flipkart.flux.commons.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class WorkFlowStateSummaryTest  {

    private String stateSummary = "{\"stateSummaries\":[{\"summary\":{\"state2\":{\"version\":\"1\",\"count\":9},\"state1\":{\"version\":\"0\",\"count\":12}}},{\"summary\":{\"state2\":{\"version\":\"1\",\"count\":19},\"state1\":{\"version\":\"0\",\"count\":13}}}]}\n";
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Test
    public void shouldParseProperly() throws Exception {
        WorkFlowStateSummary workFlowStateSummary = objectMapper.readValue(stateSummary, WorkFlowStateSummary.class);
        assertThat(workFlowStateSummary.getStateSummaries().get(0).getSummary().get("state2").getCount(), is(9));
    }

}
