package com.flipkart.flux.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.constant.RuntimeConstants;
import com.flipkart.flux.dao.iface.EventsDAO;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.State;
import com.flipkart.flux.initializer.OrderedComponentBooter;
import com.flipkart.flux.rule.DbClearRule;
import com.flipkart.flux.runner.GuiceJunit4Runner;
import com.flipkart.flux.util.TestUtils;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.apache.commons.io.IOUtils;
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

    public static final String STATE_MACHINE_RESOURCE_URL = "http://localhost:9998" + RuntimeConstants.API_CONTEXT_PATH + RuntimeConstants.STATE_MACHINE_RESOURCE_RELATIVE_PATH;
    private static final String SLASH = "/";

    @Test
    public void testCreateStateMachine() throws Exception {
        String stateMachineDefinitionJson = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("state_machine_definition.json"));
        final HttpResponse<String> response = Unirest.post(STATE_MACHINE_RESOURCE_URL).header("Content-Type","application/json").body(stateMachineDefinitionJson).asString();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        assertThat(stateMachinesDAO.findByName("test_state_machine")).hasSize(1);
        TestUtils.assertStateMachineEquality(stateMachinesDAO.findByName("test_state_machine").iterator().next(), TestUtils.getStandardTestMachine());
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
        Set<State> triggeredStates = new ObjectMapper().readValue(eventPostResponse.getBody(), new TypeReference<Set<State>>() {});
        Set<String> triggeredStatesNames = new HashSet<>();
        Iterator iterator = triggeredStates.iterator();
        while(iterator.hasNext())
            triggeredStatesNames.add(((State)iterator.next()).getName());
        Set<String> expectedStatesNames = new HashSet<String>(){{add("test_state2"); add("test_state3");}};
        assertThat(triggeredStatesNames).isEqualTo(expectedStatesNames);
    }

}