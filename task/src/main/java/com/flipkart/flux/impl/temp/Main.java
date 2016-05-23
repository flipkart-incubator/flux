package com.flipkart.flux.impl.temp;

import akka.actor.*;
import akka.cluster.routing.ClusterRouterPool;
import akka.cluster.routing.ClusterRouterPoolSettings;
import akka.cluster.singleton.ClusterSingletonManager;
import akka.cluster.singleton.ClusterSingletonManagerSettings;
import akka.cluster.singleton.ClusterSingletonProxy;
import akka.cluster.singleton.ClusterSingletonProxySettings;
import akka.remote.routing.RemoteRouterConfig;
import akka.routing.RoundRobinPool;
import com.flipkart.flux.impl.temp.Calculate;
import com.flipkart.flux.impl.temp.Master;
import com.flipkart.flux.impl.temp.Worker;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import kamon.Kamon;
import scala.Option;

public class Main {
	
	public Main() throws Exception {
		Kamon.start();
        Config config = ConfigFactory.parseString(
                "akka.remote.netty.tcp.port=" + 2551).withFallback(
                ConfigFactory.load());
        Config config1 = ConfigFactory.parseString(
                "akka.remote.netty.tcp.port=" + 2552).withFallback(
                ConfigFactory.load());
        Config config2 = ConfigFactory.parseString(
                "akka.remote.netty.tcp.port=" + 2553).withFallback(
                ConfigFactory.load());
        
		ActorSystem system = ActorSystem.create("CalcSystem", config);
		ActorSystem system1 = ActorSystem.create("CalcSystem", config1);
		ActorSystem system2 = ActorSystem.create("CalcSystem", config2);
		
		
		final ClusterSingletonManagerSettings settings =
				  ClusterSingletonManagerSettings.create(system);
		final ClusterSingletonManagerSettings settings1 =
				  ClusterSingletonManagerSettings.create(system1);
		final ClusterSingletonManagerSettings settings2 =
				  ClusterSingletonManagerSettings.create(system2);
		

		Address[] addresses = {
				  AddressFromURIString.parse("akka.tcp://CalcSystem@127.0.0.1:2551"),
				  AddressFromURIString.parse("akka.tcp://CalcSystem@127.0.0.1:2552"),
				  AddressFromURIString.parse("akka.tcp://CalcSystem@127.0.0.1:2553")
				  };
		
		/*
		ActorRef router = system.actorOf(
				ClusterSingletonManager.props(new RemoteRouterConfig(new RoundRobinPool(6), addresses).props(
				    Worker.createWorker(2551)),PoisonPill.getInstance(),settings),"workerRouter");
		*/
		ActorRef router = system.actorOf(
				ClusterSingletonManager.props(new ClusterRouterPool(new RoundRobinPool(2),
				        new ClusterRouterPoolSettings(100, 3,
				                false, Option.empty())).props(
				                		new RemoteRouterConfig(new RoundRobinPool(6), addresses).props(
				            				    Worker.createWorker())),PoisonPill.getInstance(),settings),"workerRouter");

		Thread.sleep(1000);
		
		Address[] addresses1 = {
				  AddressFromURIString.parse("akka.tcp://CalcSystem@127.0.0.1:2551"),
				  AddressFromURIString.parse("akka.tcp://CalcSystem@127.0.0.1:2552"),
				  AddressFromURIString.parse("akka.tcp://CalcSystem@127.0.0.1:2553")
				  };
		
		/*
		ActorRef router1 = system1.actorOf(
				ClusterSingletonManager.props(new RemoteRouterConfig(new RoundRobinPool(6), addresses1).props(
						Worker.createWorker(2552)),PoisonPill.getInstance(),settings1),"workerRouter");

		*/					
		ActorRef router1 = system1.actorOf(
				ClusterSingletonManager.props(new ClusterRouterPool(new RoundRobinPool(2),
				        new ClusterRouterPoolSettings(100, 3,
				                false, Option.empty())).props(
				                		new RemoteRouterConfig(new RoundRobinPool(6), addresses1).props(
				            				    Worker.createWorker())),PoisonPill.getInstance(),settings1),"workerRouter");
		
		ClusterSingletonProxySettings proxySettings =
			    ClusterSingletonProxySettings.create(system1);
		ActorRef routerProxy = system1.actorOf(ClusterSingletonProxy.props("/user/workerRouter",
			    proxySettings), "routerProxy");		
		
		ActorRef master = system1.actorOf(Master.createMaster(routerProxy), "master");
		
		Thread.sleep(1000);
		
		master.tell(new Calculate(), ActorRef.noSender());
        
        Kamon.shutdown();
		
	}
	
	public static void main(String... args) throws Exception {
		new Main();
    }
}

