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
import org.mockito.runners.MockitoJUnitRunner;

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
        "id1", "http://10.3.2.3");
    ClientElb clientElb = new ClientElb("id1", "http://10.3.2.3");
    when(clientElbPersistenceService.persistClientElb("id1", clientElbDefinition))
        .thenReturn(clientElb);

    Response response_1 = clientElbResource.createClientElb(
        "id1", "http://10.3.2.3");
    Assertions.assertThat(response_1.getStatus())
        .isEqualTo(Response.Status.CREATED.getStatusCode());

    Response response_2 = clientElbResource.createClientElb("id1", null);
    Assertions.assertThat(response_2.getStatus())
        .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

    Response response_3 = clientElbResource.createClientElb("id1", "http:///10.2");
    Assertions.assertThat(response_3.getStatus())
        .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

    Response response_4 = clientElbResource.createClientElb(null, "http://10.5.4.3");
    Assertions.assertThat(response_4.getStatus())
        .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

    Response response_5 = clientElbResource.createClientElb("id1", "http://10.5.4.3/api");
    Assertions.assertThat(response_5.getStatus())
        .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

    verify(clientElbPersistenceService, times(1)).persistClientElb(
        "id1", clientElbDefinition);
    verifyNoMoreInteractions(clientElbPersistenceService);
  }


  @Test
  public void testFindById() {

    when(clientElbPersistenceService.findByIdClientElb("id2"))
        .thenReturn("http://10.3.2.3");

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

//  @Test
//  public void testUpdateClientElb() {
//
//    Response response_1 = clientElbResource.updateClientElb("id1", null);
//    Assertions.assertThat(response_1.getStatus())
//        .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
//
//    Response response_2 = clientElbResource.updateClientElb(null, "http://10.24.35.3");
//    Assertions.assertThat(response_2.getStatus())
//        .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
//
//    Response response_3 = clientElbResource.updateClientElb(null, "http://10.24.35.3/api");
//    Assertions.assertThat(response_3.getStatus())
//        .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
//
//    doNothing().when(clientElbPersistenceService).updateClientElb("id1", "http://10.24.32.1");
//
//    Response response_4 = clientElbResource.updateClientElb("id1", "http://10.24.32.1");
//    Assertions.assertThat(response_4.getStatus())
//        .isEqualTo(Response.Status.ACCEPTED.getStatusCode());
//
//    verify(clientElbPersistenceService, times(1)).updateClientElb(
//        "id1", "http://10.24.32.1");
//    verifyNoMoreInteractions(clientElbPersistenceService);
//  }
//
//  @Test
//  public void testUpdateClientElb_DefaultElbIdRestriction() {
//    Response response_1 = clientElbResource.updateClientElb("defaultElbId", "http://10.24.32.1");
//    Assertions.assertThat(response_1.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());
//  }
//
//
//  @Test
//  public void testDeleteClientElb() {
//
//    Response response_1 = clientElbResource.deleteClientElb(null);
//    Assertions.assertThat(response_1.getStatus())
//        .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
//
//    doNothing().when(clientElbPersistenceService).deleteClientElb("magic_id");
//    Response response_2 = clientElbResource.deleteClientElb("magic_id");
//    Assertions.assertThat(response_2.getStatus())
//        .isEqualTo(Response.Status.ACCEPTED.getStatusCode());
//
//    verify(clientElbPersistenceService, times(1)).deleteClientElb(
//        "magic_id");
//    verifyNoMoreInteractions(clientElbPersistenceService);
//  }
//
//  @Test
//  public void testDeleteClientElb_DefaultElbIdRestriction() {
//    Response response_1 = clientElbResource.deleteClientElb("defaultElbId");
//    Assertions.assertThat(response_1.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());
//  }
}