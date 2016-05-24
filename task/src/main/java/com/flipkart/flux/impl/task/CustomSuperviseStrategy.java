package com.flipkart.flux.impl.task;

import akka.actor.OneForOneStrategy;
import akka.actor.SupervisorStrategy;
import akka.japi.Function;
import com.flipkart.flux.impl.excpetion.TaskResumableException;
import com.google.inject.Inject;
import scala.concurrent.duration.Duration;

import static akka.actor.SupervisorStrategy.escalate;
import static akka.actor.SupervisorStrategy.restart;
import static akka.actor.SupervisorStrategy.resume;
import static akka.actor.SupervisorStrategy.stop;


public class CustomSuperviseStrategy {

    //todo fine tune these exceptions
    private Function function = t -> {
        if (t instanceof TaskResumableException) {
            return resume();
        } else if (t instanceof NullPointerException) {
            return restart();
        } else if (t instanceof IllegalArgumentException) {
            return stop();
        } else {
            return escalate();
        }
    };

    private final SupervisorStrategy strategy;

    @Inject
    public CustomSuperviseStrategy(int maxNrOfRetries) {
        strategy = new OneForOneStrategy(maxNrOfRetries, Duration.Inf(),  function);
    }

    public SupervisorStrategy getStrategy() {
        return strategy;
    }
}
