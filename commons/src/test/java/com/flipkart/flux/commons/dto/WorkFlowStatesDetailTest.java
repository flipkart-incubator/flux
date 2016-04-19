package com.flipkart.flux.commons.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class WorkFlowStatesDetailTest {

    private String statesDetail = "{\"statesDetail\":[{\"stateDetail\":{\"state2\":{\"version\":\"1\",\"status\":\"status1\",\"reTried\":3,\"events\":{\"eventData\":{\"eid2\":{\"status\":\"state1\",\"data\":\"jsonData\"},\"eid1\":{\"status\":\"state1\",\"data\":\"jsonData\"}}}},\"state1\":{\"version\":\"1\",\"status\":\"status1\",\"reTried\":3,\"events\":{\"eventData\":{\"eid2\":{\"status\":\"state1\",\"data\":\"jsonData\"},\"eid1\":{\"status\":\"state1\",\"data\":\"jsonData\"}}}}}}]}\n";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void shouldParseProperly() throws Exception {
        WorkFlowStatesDetail workFlowStatesDetail = objectMapper.readValue(statesDetail, WorkFlowStatesDetail.class);
        assertThat(workFlowStatesDetail.getStatesDetail().get(0).getStateDetail().get("state1").getEvents().getEventData().get("eid1").getData(), is("jsonData"));

    }

}
