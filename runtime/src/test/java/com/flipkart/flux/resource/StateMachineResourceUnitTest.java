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

package com.flipkart.flux.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.api.StateMachineDefinition;
import com.flipkart.flux.controller.WorkFlowExecutionController;
import com.flipkart.flux.dao.iface.EventsDAO;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.representation.EventPersistenceService;
import com.flipkart.flux.representation.StateMachinePersistenceService;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.Response;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StateMachineResourceUnitTest {

    @Mock
    StateMachinesDAO stateMachinesDAO;

    @Mock
    EventsDAO eventsDAO;

    @Mock
    WorkFlowExecutionController workFlowExecutionController;

    private StateMachinePersistenceService stateMachinePersistenceService;

    private EventPersistenceService eventPersistenceService;

    private ObjectMapper objectMapper;
    private StateMachineResource stateMachineResource;

    @Before
    public void setUp() throws Exception {
        eventPersistenceService = new EventPersistenceService(eventsDAO);
        stateMachinePersistenceService = new StateMachinePersistenceService(stateMachinesDAO,eventPersistenceService);
        objectMapper = new ObjectMapper();
        stateMachineResource = new StateMachineResource(eventsDAO, stateMachinePersistenceService, stateMachinesDAO, workFlowExecutionController);
    }

    @Test
    public void testGetFsmGraphData_shouldWorkForMultipleStatesOfSameName() throws Exception {

//        when(stateMachinesDAO.findById(anyLong())).thenReturn(stateMachine);
//        when(eventsDAO.findTriggeredEventsNamesBySMId(anyLong())).thenReturn(Arrays.asList("given_event","fork_state_1_output_event","fork_state_2_output_event"));
//        final Response fsmGraphData = stateMachineResource.getFsmGraphData(1l);
//        assertThat(fsmGraphData.getEntity()).isEqualTo(IOUtils.toString(this.getClass().getClassLoader().getResource("fork_join_state_machine_graph_data.json")));
    }
}