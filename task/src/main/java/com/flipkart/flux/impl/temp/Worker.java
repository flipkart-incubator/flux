package com.flipkart.flux.impl.temp;

import java.math.BigInteger;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;

import akka.actor.ActorRef;
import akka.actor.Address;
import akka.actor.AddressFromURIString;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.remote.routing.RemoteRouterConfig;
import akka.routing.RoundRobinPool;
import scala.collection.mutable.ArraySeq;

/**
 * //TODO - this class needs to go
 * Prints the received message. If the message is of type work, then this also calculates the factorial of
 * a number that is close to Regu's heart
 */
public class Worker extends UntypedActor {
	
	String systemPort;
	public Worker() {
		this.systemPort = this.getContext().system().settings().config().getString("akka.remote.netty.tcp.port");
		System.out.println("Created the Worker at ActorSystem : " + systemPort);
	}

	@Override
	public void onReceive(Object message) {
		System.out.println("Received Work " +message);
		if (Work.class.isAssignableFrom(message.getClass())) {
			new TaskExecutor(this).execute();
		}
	}

	
	class TaskExecutor extends HystrixCommand<BigInteger> {

		protected TaskExecutor(Worker worker) {
	        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("Worker"))
	                .andCommandKey(HystrixCommandKey.Factory.asKey("CalculateFactorial_" + worker.systemPort))
	                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("Worker-TP"))
	                .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter().withCoreSize(100))
	                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter().withExecutionIsolationThreadTimeoutInMilliseconds(500)));
		}

		@Override
		protected BigInteger run() throws Exception {
			BigInteger fact = BigInteger.valueOf(1);
			for (int i = 1; i <= 739; i++)
				fact = fact.multiply(BigInteger.valueOf(i));
			return fact;
		}
	}
}
