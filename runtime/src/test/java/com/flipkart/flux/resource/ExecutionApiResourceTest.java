package com.flipkart.flux.resource;

import akka.actor.ActorRef;
import com.flipkart.flux.api.core.TaskExecutionMessage;
import com.flipkart.flux.impl.task.registry.RouterRegistry;
import com.flipkart.flux.metrics.iface.MetricsClient;
import com.flipkart.flux.util.TestUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExecutionApiResourceTest {

    @Mock
    RouterRegistry routerRegistry;

    @Mock
    MetricsClient metricsClient;

    @Mock
    ActorRef actor;

    ExecutionApiResource executionApiResource;

    @BeforeClass
    public void setUp(){
        executionApiResource = new ExecutionApiResource(routerRegistry, metricsClient);
    }


    @Test
    public void shouldLookForRouterInRegistry() throws Exception{
        TaskExecutionMessage msg = TestUtils.getStandardTaskExecutionMessage();
        when(routerRegistry.getRouter(msg.getRouterName())).thenReturn(actor);
        executionApiResource.receiveTaskAndExecutionData(msg);
        verify(routerRegistry).getRouter(msg.getRouterName());
        verify(actor).tell(msg, ActorRef.noSender());
        verifyNoMoreInteractions(routerRegistry);
        verifyNoMoreInteractions(actor);

    }


}
