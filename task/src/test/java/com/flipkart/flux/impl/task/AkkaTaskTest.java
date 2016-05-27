package com.flipkart.flux.impl.task;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import com.flipkart.flux.client.registry.Executable;
import com.flipkart.flux.client.registry.ExecutableRegistry;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.impl.message.TaskAndEvents;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import scala.concurrent.Future;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AkkaTaskTest {
    @Mock
    ExecutableRegistry executableRegistry;

    private TaskRegistry taskRegistry;

    @Before
    public void setUp() throws Exception {
        taskRegistry = new TaskRegistry(executableRegistry);
    }

    @Test
    public void shouldExecuteABasicTask() throws Exception {
        Config config = ConfigFactory.parseString(
                "akka.remote.netty.tcp.port=" + 2551).withFallback(
                ConfigFactory.load());

        ActorSystem actorSystem = ActorSystem.create("CalcSystem", config);
        final Props props = Props.create(AkkaTask.class);
        final TestActorRef<AkkaTask> ref = TestActorRef.create(actorSystem, props, "testA");
        final AkkaTask actor = ref.underlyingActor();
        Event[] events = new Event[1];
        events[0] = new Event("name", "type", Event.EventStatus.pending, 1L, new Object(), "es");
        TaskAndEvents taskAndEvents = new TaskAndEvents("ti", events);

        int counter=0;
        final TestExecutable givenExecutable = new TestExecutable(counter);
        when(executableRegistry.getTask(anyString())).thenReturn(givenExecutable);

        actor.setTaskRegistry(taskRegistry);

        //this test is just for functionality check of actor. In actual flow, future might not be used and async testing will need to be done.
        final Future<Object> future = akka.pattern.Patterns.ask(ref, taskAndEvents, 3);
        assertTrue(future.isCompleted());
        assertTrue(givenExecutable.getCounter()==counter+1);
    }

    class TestExecutable implements Executable {

        private int counter;

        public TestExecutable(int counter) {
            this.counter = counter;
        }

        @Override
        public String getName() {
            return "test";
        }

        @Override
        public long getTimeout() {
            return 10;
        }

        @Override
        public Object execute(Object[] parameters) {
            return ++counter;
        }

        public int getCounter() {
            return counter;
        }
    }


}
