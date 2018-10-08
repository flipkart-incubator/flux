package com.flipkart.flux.resource;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.testkit.TestActorRef;
import akka.testkit.TestProbe;
import com.flipkart.flux.MockActorRef;
import com.flipkart.flux.api.core.TaskExecutionMessage;
import com.flipkart.flux.impl.task.registry.RouterRegistry;
import com.flipkart.flux.metrics.iface.MetricsClient;
import com.flipkart.flux.util.TestUtils;
import com.typesafe.config.ConfigFactory;
import org.aspectj.lang.annotation.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ExecutionApiResourceTest {

    @Mock
    RouterRegistry routerRegistry;

    @Mock
    MetricsClient metricsClient;

    static TestActorRef<MockActorRef> mockActor;

    ExecutionApiResource executionApiResource;

    TestProbe testProbe;

    static ActorSystem actorSystem;

    @BeforeClass
    public static  void setUp(){
        actorSystem  = ActorSystem.create("FluxExecutionSystem", ConfigFactory.load("testAkkaActorSystem.conf"));
        mockActor = TestActorRef.create(actorSystem, Props.create(MockActorRef.class));
    }

    @AfterClass
    public static void cleanUp(){
       scala.concurrent.Future<Terminated> x =  actorSystem.terminate();
       while(!x.isCompleted() );
    }

    @Before
    public void beforeEachTest(){
        executionApiResource = new ExecutionApiResource(routerRegistry, metricsClient);
    }

    @AfterClass
    public static void done(){
        mockActor.stop();
        actorSystem.terminate();
    }

    @Test
    public void receiveExecutionMessage_shouldLookupRouterAndSendMessage() throws Exception {
        TaskExecutionMessage msg = TestUtils.getStandardTaskExecutionMessage();
        when(routerRegistry.getRouter(msg.getRouterName())).thenReturn(mockActor);
        executionApiResource.receiveTaskAndExecutionData(msg);
        verify(routerRegistry, times(1)).getRouter(msg.getRouterName());
        mockActor.underlyingActor().assertMessageReceived(msg.getAkkaMessage(), 1);
        verifyNoMoreInteractions(routerRegistry);
    }

}
