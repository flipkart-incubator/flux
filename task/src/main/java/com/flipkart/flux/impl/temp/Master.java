package com.flipkart.flux.impl.temp;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;

import java.util.ArrayList;

public class Master extends UntypedActor {
	
	public ActorRef router;

	private long messages = 100000;
	private long processed = 0;
	private final Time time = new Time();
	private ArrayList list = new ArrayList();

	
	public Master(ActorRef router) {
		this.router = router;
	}

	@Override
	public void onReceive(Object message) {
		if (message instanceof Calculate) {
			time.start();
			processMessages();
		} else if (message instanceof Result) {
			list.add(((Result) message).getFactorial());
			//System.out.println("Received message. Size of results : " + list.size());
			if (list.size() == messages)
				end();
		} else if (message instanceof Terminated) {
			/*
			router = router.removeRoutee(((Terminated) message).actor());
			ActorRef r = getContext().actorOf(Props.create(Worker.class));
			getContext().watch(r);
			router = router.addRoutee(new ActorRefRoutee(r));
			*/
		} else {
			unhandled(message);
		}
	}

	private void processMessages() {
		for (int i = 0; i < messages; i++) {
			//System.out.println("Sending message to router. Message no. " + i);
			router.tell(new Work(), getSelf());
			try {
				Thread.currentThread().sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void end() {
		time.end();
		System.out.println("Done: " + time.elapsedTimeMilliseconds());
		//getContext().system().shutdown();
	}

	public static Props createMaster(ActorRef router) {
		return Props.create(Master.class, router);
	}
}