package com.flipkart.flux.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.api.StateMachineDefinition;
import com.flipkart.flux.constant.RuntimeConstants;
import com.flipkart.flux.dao.iface.EventsDAO;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.initializer.OrderedComponentBooter;
import com.flipkart.flux.representation.StateMachinePersistenceService;
import com.flipkart.flux.rule.DbClearRule;
import com.flipkart.flux.runner.GuiceJunit4Runner;
import com.flipkart.flux.util.TestUtils;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(GuiceJunit4Runner.class)
public class StateMachineResourceTest {

    @Inject
    @Rule
    public DbClearRule dbClearRule;

    @Inject
    private StateMachinesDAO stateMachinesDAO;

    @Inject
    private EventsDAO eventsDAO;

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

    @Test
    public void testCreateStateMachine() throws Exception {
        String stateMachineDefinitionJson = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("state_machine_definition.json"));
        final HttpResponse<String> response = Unirest.post(STATE_MACHINE_RESOURCE_URL).header("Content-Type","application/json").body(stateMachineDefinitionJson).asString();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        assertThat(stateMachinesDAO.findByName("test_state_machine")).hasSize(1);
        TestUtils.assertStateMachineEquality(stateMachinesDAO.findByName("test_state_machine").iterator().next(), TestUtils.getStandardTestMachine());
    }

    @Test
    public void testCreateStateMachine_shouldBombDueToDuplicateCorrelationId() throws Exception {
        String stateMachineDefinitionJson = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("state_machine_definition.json"));
        final HttpResponse<String> response = Unirest.post(STATE_MACHINE_RESOURCE_URL).header("Content-Type","application/json").body(stateMachineDefinitionJson).asString();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        assertThat(stateMachinesDAO.findByName("test_state_machine")).hasSize(1);
        final HttpResponse<String> secondResponse = Unirest.post(STATE_MACHINE_RESOURCE_URL).header("Content-Type","application/json").body(stateMachineDefinitionJson).asString();
        assertThat(secondResponse.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()); // TODO - change it to 4xx
    }

    @Test
    public void testPostEvent() throws Exception {
        String stateMachineDefinitionJson = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("state_machine_definition.json"));
        final HttpResponse<String> smCreationResponse = Unirest.post(STATE_MACHINE_RESOURCE_URL).header("Content-Type","application/json").body(stateMachineDefinitionJson).asString();
        Event event = eventsDAO.findBySMIdAndName(Long.parseLong(smCreationResponse.getBody()), "event1");
        assertThat(event.getStatus()).isEqualTo(Event.EventStatus.pending);

        String eventJson = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("event_data.json"));
        final HttpResponse<String> eventPostResponse = Unirest.post(STATE_MACHINE_RESOURCE_URL+SLASH+smCreationResponse.getBody()+"/context/events").header("Content-Type","application/json").body(eventJson).asString();
        assertThat(eventPostResponse.getStatus()).isEqualTo(Response.Status.ACCEPTED.getStatusCode());
        event = eventsDAO.findBySMIdAndName(Long.parseLong(smCreationResponse.getBody()), "event1");
        assertThat(event.getStatus()).isEqualTo(Event.EventStatus.triggered);
        assertThat(event).isEqualToIgnoringGivenFields(TestUtils.getStandardTestEvent(), "stateMachineInstanceId", "id", "createdAt", "updatedAt");
        // TODO - assert the status of expected triggered states (after we start storing state statuses)
    }

    @Test
    public void testPostEvent_withCorrelationId() throws Exception {
        String stateMachineDefinitionJson = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("state_machine_definition.json"));
        final HttpResponse<String> smCreationResponse = Unirest.post(STATE_MACHINE_RESOURCE_URL).header("Content-Type","application/json").body(stateMachineDefinitionJson).asString();
        Event event = eventsDAO.findBySMIdAndName(Long.parseLong(smCreationResponse.getBody()), "event1");
        assertThat(event.getStatus()).isEqualTo(Event.EventStatus.pending);

        String eventJson = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("event_data.json"));
        final HttpResponse<String> eventPostResponse = Unirest.post(STATE_MACHINE_RESOURCE_URL+SLASH+"magic_number_1"+"/context/events?searchField=correlationId").header("Content-Type","application/json").body(eventJson).asString();
        assertThat(eventPostResponse.getStatus()).isEqualTo(Response.Status.ACCEPTED.getStatusCode());
        event = eventsDAO.findBySMIdAndName(Long.parseLong(smCreationResponse.getBody()), "event1");
        assertThat(event.getStatus()).isEqualTo(Event.EventStatus.triggered);
        assertThat(event).isEqualToIgnoringGivenFields(TestUtils.getStandardTestEvent(), "stateMachineInstanceId", "id", "createdAt", "updatedAt");
        // TODO - assert the status of expected triggered states (after we start storing state statuses)
    }

    @Test
    public void testPostEvent_againstNonExistingCorrelationId() throws Exception {
        String stateMachineDefinitionJson = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("state_machine_definition.json"));
        final HttpResponse<String> smCreationResponse = Unirest.post(STATE_MACHINE_RESOURCE_URL).header("Content-Type","application/json").body(stateMachineDefinitionJson).asString();
        Event event = eventsDAO.findBySMIdAndName(Long.parseLong(smCreationResponse.getBody()), "event1");
        assertThat(event.getStatus()).isEqualTo(Event.EventStatus.pending);

        String eventJson = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("event_data.json"));
        // state machine with correlationId magic_number_2 does not exist. The following call should bomb
        final HttpResponse<String> eventPostResponse = Unirest.post(STATE_MACHINE_RESOURCE_URL+SLASH+"magic_number_2"+"/context/events?searchField=correlationId").header("Content-Type","application/json").body(eventJson).asString();
        assertThat(eventPostResponse.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());// TODO - change to 404

    }

    @Test
    public void testFsmGraphCreation() throws Exception {
        final StateMachine stateMachine = stateMachinePersistenceService.createStateMachine(objectMapper.readValue(this.getClass().getClassLoader().getResource("state_machine_definition_fork_join.json"), StateMachineDefinition.class));
        final HttpResponse<String> stringHttpResponse = Unirest.get(STATE_MACHINE_RESOURCE_URL + "/" + stateMachine.getId() + "/fsmdata").header("Content-Type", "application/json").asString();
        assertThat(stringHttpResponse.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        //TODO - we need a better assert here, but since we're using database IDs in the implementation, we cannot simply validate it with a static json
    }
}