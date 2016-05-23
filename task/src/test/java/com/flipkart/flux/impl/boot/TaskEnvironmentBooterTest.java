package com.flipkart.flux.impl.boot;

import akka.actor.ActorRef;
import akka.cluster.routing.ClusterRouterPoolSettings;
import com.flipkart.flux.impl.config.DeploymentUnitConfig;
import javafx.util.Pair;
import org.junit.Ignore;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import scala.Option;

import java.net.Inet4Address;
import java.util.Collections;
import java.util.Set;

public class TaskEnvironmentBooterTest {
    @Test
    @Ignore
    public void testBoot() throws Exception {

        final TaskEnvironmentBooter calcSystem = new TaskEnvironmentBooter("FluxSystem", "application.conf", false);
        calcSystem.preInit();

        final Set<Pair<String,ClusterRouterPoolSettings>> singleton = Collections.singleton(new Pair<>(
            "someWorker",new ClusterRouterPoolSettings(100, 3,true, Option.empty())
        ));

        calcSystem.init(singleton.iterator());
        Thread.sleep(5000l);
        System.out.println("Done sleeping once");
//        calcSystem.getProxy("someWorker").tell("You have a new messge", ActorRef.noSender());
//        System.out.println("Sent message, now sleeping");
//        Thread.sleep(20000l);
    }

    @Test
    public void testFoo() throws Exception {
        System.out.println("true = " + Inet4Address.getLocalHost().getHostAddress());
    }

    @Test
    public void testConfig() throws Exception {
        final DeploymentUnitConfig deploymentUnitConfig = new Yaml().loadAs(this.getClass().getClassLoader().getResourceAsStream("task_runtime_config.yaml"), DeploymentUnitConfig.class);
        System.out.println(deploymentUnitConfig);

    }
}