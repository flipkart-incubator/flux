/*
 * Copyright 2012-2016, the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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

/**
 * Default strategy of Akka is to restart all the actors when some excpetion happens.
 * That will not suit us. So, supplying a custom one.
 * @author ashish.bhutani
 */


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
