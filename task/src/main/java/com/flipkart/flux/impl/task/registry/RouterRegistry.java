package com.flipkart.flux.impl.task.registry;

import akka.actor.ActorRef;

/**
 * Interface that allows for akka router lookups
 * @author yogesh.nachnani
 */
public interface RouterRegistry {
    ActorRef getRouter(String forWorker);
}
