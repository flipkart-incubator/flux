package com.flipkart.flux.impl.task.registry;

import akka.actor.ActorRef;

/**
 * Looks up and/or provides akka routers
 */
public interface RouterRegistry {
    ActorRef getRouter(String forWorker);
}
