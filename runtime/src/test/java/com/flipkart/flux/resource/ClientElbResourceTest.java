package com.flipkart.flux.resource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.flipkart.flux.api.ClientElbDefinition;
import com.flipkart.flux.domain.ClientElb;
import com.flipkart.flux.representation.ClientElbPersistenceService;

/**
 * @author akif.khan
 */
@RunWith(MockitoJUnitRunner.class)
public class ClientElbResourceTest {

  ClientElbPersistenceService clientElbPersistenceService = mock(ClientElbPersistenceService.class);

  ClientElbResource clientElbResource = new ClientElbResource(clientElbPersistenceService);

  @Test
  public void testCreateClientElb() {

    ClientElbDefinition clientElbDefinition = new ClientElbDefinition(
        "id1", "http://127.0.0.1");
    ClientElb clientElb = new ClientElb("id1", "http://127.0.0.1");
    when(clientElbPersistenceService.persistClientElb("id1", clientElbDefinition))
        .thenReturn(clientElb);

    Response response_1 = clientElbResource.createClientElb(
        "id1", "http://127.0.0.1");
    Assertions.assertThat(response_1.getStatus())
        .isEqualTo(Response.Status.CREATED.getStatusCode());

    Response response_2 = clientElbResource.createClientElb("id1", null);
    Assertions.assertThat(response_2.getStatus())
        .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

    Response response_3 = clientElbResource.createClientElb("id1", "http:///127.0.0.1");
    Assertions.assertThat(response_3.getStatus())
        .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

    Response response_4 = clientElbResource.createClientElb(null, "http://127.0.0.1");
    Assertions.assertThat(response_4.getStatus())
        .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

    Response response_5 = clientElbResource.createClientElb("id1", "http://127.0.0.1/api");
    Assertions.assertThat(response_5.getStatus())
        .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

    verify(clientElbPersistenceService, times(1)).persistClientElb(
        "id1", clientElbDefinition);
    verifyNoMoreInteractions(clientElbPersistenceService);
  }


  @Test
  public void testFindById() {

    when(clientElbPersistenceService.findByIdClientElb("id2"))
        .thenReturn("http://127.0.0.1");

    Response response_1 = clientElbResource.findByIdClientElb(null);
    Assertions.assertThat(response_1.getStatus())
        .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

    Response response_2 = clientElbResource.findByIdClientElb("id2");
    Assertions.assertThat(response_2.getStatus()).isEqualTo(Response.Status.FOUND.getStatusCode());

    when(clientElbPersistenceService.findByIdClientElb("id2"))
        .thenReturn(null);

    Response response_3 = clientElbResource.findByIdClientElb("id2");
    Assertions.assertThat(response_3.getStatus())
        .isEqualTo(Response.Status.NOT_FOUND.getStatusCode());

    verify(clientElbPersistenceService, times(2)).findByIdClientElb(
        "id2");
    verifyNoMoreInteractions(clientElbPersistenceService);
  }

}