package com.flipkart.flux.representation;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.api.EventDefinition;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.persistence.dao.iface.EventsDAO;


/**
 * @author vartika.bhatia
 */

@RunWith(MockitoJUnitRunner.class)
public class EventPersistenceServiceTest {

    @Mock
    ObjectMapper objectMapper;

    @Mock
    EventPersistenceService eventPersistenceService;

    @Mock
    EventsDAO eventsDAO;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        objectMapper = new ObjectMapper();
        eventPersistenceService = new EventPersistenceService(eventsDAO);
    }

    @Test
    public void testConvertEventDefinitionToEvent() throws Exception{

        String eventDefinitionJson = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("event_definition_with_eventSource.json"), "UTF-8");
        EventDefinition eventDefinition = objectMapper.readValue(eventDefinitionJson, EventDefinition.class);
        Event event = eventPersistenceService.convertEventDefinitionToEvent(eventDefinition);
        assertThat(event.getEventSource()).isNotNull();
    }

    @Test
    public void testConvertEventDefinitionToEventNoEventSource() throws Exception{

        String eventDefinitionJson = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("event_definition_without_eventSource.json"), "UTF-8");
        EventDefinition eventDefinition = objectMapper.readValue(eventDefinitionJson, EventDefinition.class);
        Event event = eventPersistenceService.convertEventDefinitionToEvent(eventDefinition);
        assertThat(event.getEventSource()).isNull();
    }

}

