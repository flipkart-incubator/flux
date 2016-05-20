package com.flipkart.flux.impl.boot;

import akka.actor.ActorRef;
import com.flipkart.flux.impl.task.registry.EagerInitRouterRegistryImpl;
import com.flipkart.flux.impl.task.registry.LocalRouterConfigurationRegistryImpl;
import com.flipkart.flux.impl.task.registry.RouterRegistry;
import com.flipkart.polyguice.config.ApacheCommonsConfigProvider;
import com.flipkart.polyguice.config.YamlConfiguration;
import com.flipkart.polyguice.core.support.Polyguice;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import org.junit.Ignore;
import org.junit.Test;

public class BootTest {
    @Test
    @Ignore
    public void testBoot() throws Exception {
        final Polyguice polyguice = new Polyguice();
        final ApacheCommonsConfigProvider provider = new ApacheCommonsConfigProvider();
//        new YamlConfiguration()
        provider.location(this.getClass().getClassLoader().getResource("task_runtime_config.yaml").getPath());
        polyguice.registerConfigurationProvider(provider);
//        polyguice.modules(new TaskModule(provider));
//        polyguice.prepare();
        final Injector injector = Guice.createInjector(Stage.PRODUCTION, new TaskModule(provider));
        final LocalRouterConfigurationRegistryImpl routerConfigurationRegistry = injector.getInstance(LocalRouterConfigurationRegistryImpl.class);
        routerConfigurationRegistry.initialize(); // This will be done using polyguice

        final ActorSystemManager actorSystemManager = injector.getInstance(ActorSystemManager.class);
        actorSystemManager.initialize();// This will be done using polyguice in prod
        final EagerInitRouterRegistryImpl routerRegistry = injector.getInstance(EagerInitRouterRegistryImpl.class);
        routerRegistry.initialize(); // Again, polyguice stuff
        Thread.sleep(20000l);
        routerRegistry.getRouter("someRouter").tell("Message for some router", ActorRef.noSender());
        routerRegistry.getRouter("someRouterWithoutConfig").tell("Message for some router with no config", ActorRef.noSender());
        Thread.sleep(5000l);
    }
}