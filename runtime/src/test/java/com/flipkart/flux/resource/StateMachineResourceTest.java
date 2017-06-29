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
import com.flipkart.flux.client.FluxClientInterceptorModule;
import com.flipkart.flux.constant.RuntimeConstants;
import com.flipkart.flux.dao.TestWorkflow;
import com.flipkart.flux.dao.iface.EventsDAO;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.dao.iface.StatesDAO;
import com.flipkart.flux.domain.*;
import com.flipkart.flux.eventscheduler.dao.EventSchedulerDao;
import com.flipkart.flux.eventscheduler.model.ScheduledEvent;
import com.flipkart.flux.guice.module.AkkaModule;
import com.flipkart.flux.guice.module.ContainerModule;
import com.flipkart.flux.guice.module.HibernateModule;
import com.flipkart.flux.impl.boot.TaskModule;
import com.flipkart.flux.initializer.OrderedComponentBooter;
import com.flipkart.flux.module.DeploymentUnitTestModule;
import com.flipkart.flux.module.RuntimeTestModule;
import com.flipkart.flux.representation.StateMachinePersistenceService;
import com.flipkart.flux.rule.DbClearRule;
import com.flipkart.flux.runner.GuiceJunit4Runner;
import com.flipkart.flux.runner.Modules;
import com.flipkart.flux.util.TestUtils;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(GuiceJunit4Runner.class)
@Modules({DeploymentUnitTestModule.class,HibernateModule.class,RuntimeTestModule.class,ContainerModule.class,AkkaModule.class,TaskModule.class,FluxClientInterceptorModule.class})
public class StateMachineResourceTest {

    @Inject
    @Rule
    public DbClearRule dbClearRule;

    @Inject
    private StateMachinesDAO stateMachinesDAO;

    @Inject
    private StatesDAO statesDAO;

    @Inject
    private EventsDAO eventsDAO;

    @Inject
    private EventSchedulerDao eventSchedulerDao;

    @Inject
    OrderedComponentBooter orderedComponentBooter;

    @Inject
    StateMachinePersistenceService stateMachinePersistenceService;

    public static final String STATE_MACHINE_RESOURCE_URL = "http://localhost:9998" + RuntimeConstants.API_CONTEXT_PATH + RuntimeConstants.STATE_MACHINE_RESOURCE_RELATIVE_PATH;
    private static final String SLASH = "/";
    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        objectMapper = new ObjectMapper();
    }

    @AfterClass
    public static void afterClass() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateStateMachine() throws Exception {
        String stateMachineDefinitionJson = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("state_machine_definition.json"));
        final HttpResponse<String> response = Unirest.post(STATE_MACHINE_RESOURCE_URL).header("Content-Type","application/json").body(stateMachineDefinitionJson).asString();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        assertThat(stateMachinesDAO.findByName("test_state_machine")).hasSize(1);
        Thread.sleep(1000);
        TestUtils.assertStateMachineEquality(stateMachinesDAO.findByName("test_state_machine").iterator().next(), TestUtils.getStandardTestMachine());
    }

    @Test
    public void testUnsideline() throws Exception {
        String stateMachineDefinitionJson = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("state_machine_definition.json"));
        final HttpResponse<String> smCreationResponse = Unirest.post(STATE_MACHINE_RESOURCE_URL).header("Content-Type","application/json").body(stateMachineDefinitionJson).asString();
        Event event = eventsDAO.findBySMIdAndName(Long.parseLong(smCreationResponse.getBody()), "event0");
        assertThat(event.getStatus()).isEqualTo(Event.EventStatus.pending);

        // ask the task to fail with retriable error.
        TestWorkflow.shouldFail = true;

        try {
            String eventJson = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("event_data.json"));
            final HttpResponse<String> eventPostResponse = Unirest.post(STATE_MACHINE_RESOURCE_URL + SLASH + smCreationResponse.getBody() + "/context/events").header("Content-Type", "application/json").body(eventJson).asString();
            assertThat(eventPostResponse.getStatus()).isEqualTo(Response.Status.ACCEPTED.getStatusCode());
            // give some time to execute
            Thread.sleep(4000);

            //status of state should be sidelined
            Long smId = Long.parseLong(smCreationResponse.getBody());
            State state4 = stateMachinesDAO.findById(smId).getStates().stream().filter(e -> e.getName().equals("test_state4")).findFirst().orElse(null);
            assertThat(state4).isNotNull();
            assertThat(state4.getStatus()).isEqualTo(Status.sidelined);

            TestWorkflow.shouldFail = false;

            // unsideline
            final HttpResponse<String> unsidelineResponse = Unirest.put(STATE_MACHINE_RESOURCE_URL + "/" + smId + "/" + state4.getId() + "/unsideline").asString();
            assertThat(unsidelineResponse.getStatus()).isEqualTo(Response.Status.ACCEPTED.getStatusCode());
            Thread.sleep(2000);

            state4 = stateMachinesDAO.findById(smId).getStates().stream().filter(e -> e.getName().equals("test_state4")).findFirst().orElse(null);
            assertThat(state4).isNotNull();
            assertThat(state4.getStatus()).isEqualTo(Status.completed);
        } finally {
            TestWorkflow.shouldFail = false;
        }
    }

    @Test
    public void testCreateStateMachine_shouldBombDueToDuplicateCorrelationId() throws Exception {
        String stateMachineDefinitionJson = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("state_machine_definition.json"));
        final HttpResponse<String> response = Unirest.post(STATE_MACHINE_RESOURCE_URL).header("Content-Type","application/json").body(stateMachineDefinitionJson).asString();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        assertThat(stateMachinesDAO.findByName("test_state_machine")).hasSize(1);
        final HttpResponse<String> secondResponse = Unirest.post(STATE_MACHINE_RESOURCE_URL).header("Content-Type","application/json").body(stateMachineDefinitionJson).asString();
        assertThat(secondResponse.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testPostEvent() throws Exception {
        String stateMachineDefinitionJson = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("state_machine_definition.json"));
        final HttpResponse<String> smCreationResponse = Unirest.post(STATE_MACHINE_RESOURCE_URL).header("Content-Type","application/json").body(stateMachineDefinitionJson).asString();
        Event event = eventsDAO.findBySMIdAndName(Long.parseLong(smCreationResponse.getBody()), "event0");
        assertThat(event.getStatus()).isEqualTo(Event.EventStatus.pending);

        String eventJson = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("event_data.json"));
        final HttpResponse<String> eventPostResponse = Unirest.post(STATE_MACHINE_RESOURCE_URL+SLASH+smCreationResponse.getBody()+"/context/events").header("Content-Type","application/json").body(eventJson).asString();
        assertThat(eventPostResponse.getStatus()).isEqualTo(Response.Status.ACCEPTED.getStatusCode());
        // give some time to execute
        Thread.sleep(2000);
        event = eventsDAO.findBySMIdAndName(Long.parseLong(smCreationResponse.getBody()), "event0");
        assertThat(event.getStatus()).isEqualTo(Event.EventStatus.triggered);
        assertThat(event).isEqualToIgnoringGivenFields(TestUtils.getStandardTestEvent(), "stateMachineInstanceId", "id", "createdAt", "updatedAt");

        // event3 was waiting on event1, so event3 should also be triggered
        event = eventsDAO.findBySMIdAndName(Long.parseLong(smCreationResponse.getBody()), "event3");
        assertThat(event.getStatus()).isEqualTo(Event.EventStatus.triggered);

        boolean anyNotCompleted = stateMachinesDAO.findById(Long.parseLong(smCreationResponse.getBody())).getStates().stream().anyMatch(e -> !e.getStatus().equals(Status.completed));
        assertThat(anyNotCompleted).isFalse();
    }

    @Test
    public void testPostEvent_withCorrelationId() throws Exception {
        String stateMachineDefinitionJson = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("state_machine_definition.json"));
        final HttpResponse<String> smCreationResponse = Unirest.post(STATE_MACHINE_RESOURCE_URL).header("Content-Type","application/json").body(stateMachineDefinitionJson).asString();
        Event event = eventsDAO.findBySMIdAndName(Long.parseLong(smCreationResponse.getBody()), "event0");
        assertThat(event.getStatus()).isEqualTo(Event.EventStatus.pending);

        String eventJson = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("event_data.json"));
        final HttpResponse<String> eventPostResponse = Unirest.post(STATE_MACHINE_RESOURCE_URL+SLASH+"magic_number_1"+"/context/events?searchField=correlationId").header("Content-Type","application/json").body(eventJson).asString();
        assertThat(eventPostResponse.getStatus()).isEqualTo(Response.Status.ACCEPTED.getStatusCode());
        // give some time to execute
        Thread.sleep(2000);
        event = eventsDAO.findBySMIdAndName(Long.parseLong(smCreationResponse.getBody()), "event0");
        assertThat(event.getStatus()).isEqualTo(Event.EventStatus.triggered);
        assertThat(event).isEqualToIgnoringGivenFields(TestUtils.getStandardTestEvent(), "stateMachineInstanceId", "id", "createdAt", "updatedAt");

        // event3 was waiting on event1, so event3 should also be triggered
        event = eventsDAO.findBySMIdAndName(Long.parseLong(smCreationResponse.getBody()), "event3");
        assertThat(event.getStatus()).isEqualTo(Event.EventStatus.triggered);

        boolean anyNotCompleted = stateMachinesDAO.findById(Long.parseLong(smCreationResponse.getBody())).getStates().stream().anyMatch(e -> !e.getStatus().equals(Status.completed));
        assertThat(anyNotCompleted).isFalse();
    }

    @Test
    public void testPostEvent_againstNonExistingCorrelationId() throws Exception {
        String stateMachineDefinitionJson = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("state_machine_definition.json"));
        final HttpResponse<String> smCreationResponse = Unirest.post(STATE_MACHINE_RESOURCE_URL).header("Content-Type","application/json").body(stateMachineDefinitionJson).asString();
        Event event = eventsDAO.findBySMIdAndName(Long.parseLong(smCreationResponse.getBody()), "event0");
        assertThat(event.getStatus()).isEqualTo(Event.EventStatus.pending);

        String eventJson = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("event_data.json"));
        // state machine with correlationId magic_number_2 does not exist. The following call should bomb
        final HttpResponse<String> eventPostResponse = Unirest.post(STATE_MACHINE_RESOURCE_URL+SLASH+"magic_number_2"+"/context/events?searchField=correlationId").header("Content-Type","application/json").body(eventJson).asString();
        assertThat(eventPostResponse.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testPostScheduledEvent_withoutCorrelationIdTag() throws Exception {
        String stateMachineDefinitionJson = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("state_machine_definition.json"));
        Unirest.post(STATE_MACHINE_RESOURCE_URL).header("Content-Type","application/json").body(stateMachineDefinitionJson).asString();

        String eventJson = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("event_data.json"));

        //request with searchField param missing
        final HttpResponse<String> eventPostResponse = Unirest.post(STATE_MACHINE_RESOURCE_URL+SLASH+"magic_number_1"+"/context/events?triggerTime=123").header("Content-Type","application/json").body(eventJson).asString();
        assertThat(eventPostResponse.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        //request with searchField param having value other than correlationId
        final HttpResponse<String> eventPostResponseWithWrongTag = Unirest.post(STATE_MACHINE_RESOURCE_URL+SLASH+"magic_number_1"+"/context/events?searchField=dummy&triggerTime=123").header("Content-Type","application/json").body(eventJson).asString();
        assertThat(eventPostResponseWithWrongTag.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testPostScheduledEvent_withCorrelationId() throws Exception {
        //create state machine
        String stateMachineDefinitionJson = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("state_machine_definition.json"));
        final HttpResponse<String> smCreationResponse = Unirest.post(STATE_MACHINE_RESOURCE_URL).header("Content-Type","application/json").body(stateMachineDefinitionJson).asString();

        Thread.sleep(100);

        //post an scheduled event
        long triggerTime = (System.currentTimeMillis()/1000)+1;
        String eventJson = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("event_data.json"));
        Unirest.post(STATE_MACHINE_RESOURCE_URL+SLASH+"magic_number_1"+"/context/events?searchField=correlationId&triggerTime="+triggerTime)
                .header("Content-Type", "application/json").body(eventJson).asString();
        Thread.sleep(500);

        //verify event has been saved in DB
        assertThat(eventSchedulerDao.retrieveOldest(1).get(0)).isEqualTo(new ScheduledEvent("magic_number_1", "event0", triggerTime, "{\"name\":\"event0\",\"type\":\"java.lang.String\",\"data\":\"42\",\"eventSource\":null}"));

        //waiting for 7 seconds here, to match Event scheduler thread's initial delay of 10 sec (some boot up time + 7 seconds will surpass 10 sec)
        Thread.sleep(7000);

        //verify that the event has been triggered and scheduled event has been removed from DB
        assertThat(eventsDAO.findBySMIdAndName(Long.parseLong(smCreationResponse.getBody()), "event0").getStatus()).isEqualTo(Event.EventStatus.triggered);
        assertThat(eventSchedulerDao.retrieveOldest(1)).hasSize(0);
    }

    @Test
    public void testPostScheduledEvent_withTriggerTimeInMilliSeconds() throws Exception {
        String stateMachineDefinitionJson = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("state_machine_definition.json"));
        Unirest.post(STATE_MACHINE_RESOURCE_URL).header("Content-Type","application/json").body(stateMachineDefinitionJson).asString();

        Thread.sleep(100);
        long triggerTime = System.currentTimeMillis();
        String eventJson = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("event_data.json"));
        final HttpResponse<String> eventPostResponse = Unirest.post(STATE_MACHINE_RESOURCE_URL+SLASH+"magic_number_1"+"/context/events?searchField=correlationId&triggerTime="+triggerTime)
                .header("Content-Type", "application/json").body(eventJson).asString();

        assertThat(eventSchedulerDao.retrieveOldest(1).get(0).getScheduledTime()).isEqualTo(triggerTime/1000);

    }

    @Test
    public void testFsmGraphCreation() throws Exception {
        final StateMachine stateMachine = stateMachinePersistenceService.createStateMachine(objectMapper.readValue(this.getClass().getClassLoader().getResource("state_machine_definition_fork_join.json"), StateMachineDefinition.class));
        final HttpResponse<String> stringHttpResponse = Unirest.get(STATE_MACHINE_RESOURCE_URL + "/" + stateMachine.getId() + "/fsmdata").header("Content-Type", "application/json").asString();
        assertThat(stringHttpResponse.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        //TODO - we need a better assert here, but since we're using database IDs in the implementation, we cannot simply validate it with a static json
    }

    @Test
    public void testGetErroredStates() throws Exception {
        final StateMachine sm = stateMachinePersistenceService.createStateMachine(objectMapper.readValue(this.getClass().getClassLoader().getResource("state_machine_definition.json"), StateMachineDefinition.class));

        /* mark 1 of the state as errored */
        sm.getStates().stream().findFirst().get().setStatus(Status.errored);
        /* persist */
        final StateMachine firstSM = stateMachinesDAO.create(new StateMachine(sm.getVersion(), sm.getName(), sm.getDescription(), sm.getStates(), "uniqueCorId1"));

        /* change name and persist as 2nd statemachine */
        final String differentSMName = "differentStateMachine";
        final StateMachine secondSM = stateMachinesDAO.create(new StateMachine(sm.getVersion(), differentSMName, sm.getDescription(), sm.getStates(), "uniqueCorId2"));

        /* fetch errored states with name "differentStateMachine" */
        final HttpResponse<String> stringHttpResponse = Unirest.get(STATE_MACHINE_RESOURCE_URL + "/" + differentSMName + "/states/errored?fromSmId=" + firstSM.getId() + "&toSmId=" + (secondSM.getId() + 1)).header("Content-Type", "application/json").asString();

        assertThat(stringHttpResponse.getStatus()).isEqualTo(200);
        assertThat(stringHttpResponse.getBody()).isEqualTo("[[" + secondSM.getId() + "," +
                secondSM.getStates().stream().filter(e -> Status.errored.equals(e.getStatus())).findFirst().get().getId() + "," +
                "\"errored\"]]");
    }

    @Test
    public void testCancelWorkflow() throws Exception {
        final StateMachine sm = stateMachinePersistenceService.createStateMachine(objectMapper.readValue(this.getClass().getClassLoader().getResource("state_machine_definition.json"), StateMachineDefinition.class));
        Long stateMachineId = sm.getId();
        State state = sm.getStates().stream().findFirst().get();
        state.setStatus(Status.running);
        statesDAO.updateState(state);
        Unirest.put(STATE_MACHINE_RESOURCE_URL+SLASH+stateMachineId+"/cancel").asString();

        Thread.sleep(200);
        StateMachine cancelledSM = stateMachinesDAO.findById(stateMachineId);
        assertThat(cancelledSM.getStatus()).isEqualTo(StateMachineStatus.cancelled);

        int cancelledStateCount = 0;
        for(State st : cancelledSM.getStates()) {
            if(st.getStatus() == Status.cancelled)
                cancelledStateCount++;
        }

        // 3 were in initialized state and one in running state before cancel call, after call, all 3 initialized states should be marked as cancelled
        assertThat(cancelledStateCount).isEqualTo(3);
    }

    @Test
    public void testCancelWorkflow_withCorrelationId() throws Exception {
        final StateMachine sm = stateMachinePersistenceService.createStateMachine(objectMapper.readValue(this.getClass().getClassLoader().getResource("state_machine_definition.json"), StateMachineDefinition.class));
        String stateMachineId = sm.getCorrelationId();
        Unirest.put(STATE_MACHINE_RESOURCE_URL+SLASH+stateMachineId+"/cancel?searchField=correlationId").asString();

        Thread.sleep(200);
        StateMachine cancelledSM = stateMachinesDAO.findByCorrelationId(stateMachineId);
        assertThat(cancelledSM.getStatus()).isEqualTo(StateMachineStatus.cancelled);

        cancelledSM.getStates().forEach(st -> {
            assertThat(st.getStatus()).isEqualTo(Status.cancelled);
        });
    }
}