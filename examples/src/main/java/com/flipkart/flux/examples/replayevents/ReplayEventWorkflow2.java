package com.flipkart.flux.examples.replayevents;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flipkart.flux.client.model.Event;
import com.flipkart.flux.client.model.ReplayEvent;
import com.flipkart.flux.client.model.Task;
import com.flipkart.flux.client.model.Workflow;

/**
 * Workflow to demonstrate replay Event trigger
 *
 * @author akif.khan
 */
public class ReplayEventWorkflow2 {

    @Workflow(version = 1)
    public void create(StartEvent startEvent) {
        IntegerEvent e1 = t1(startEvent);
        IntegerEvent e2 = t2(e1);
        IntegerEvent e5 = t5(null, e1);
        IntegerEvent e3 = t3(e2, e5);
        IntegerEvent e4 = t4(e3);
        IntegerEvent e6 = t6(e5);
        t7(e4, e6);
        IntegerEvent e8 = t8(e1);
        IntegerEvent e9 = t9(null, e5, e8);
        t10(e9);
    }

    @Task(version = 1, retries = 0, timeout = 1000L)
    public IntegerEvent t1(StartEvent e0) {
        return new IntegerEvent(1);
    }

    @Task(version = 1, retries = 0, timeout = 1000L)
    public IntegerEvent t2(IntegerEvent e1) {
        return new IntegerEvent(2);
    }

    @Task(version = 1, retries = 0, timeout = 5000L)
    public IntegerEvent t3(IntegerEvent e2, IntegerEvent e5) {
        try {
            Thread.sleep(4500);
        } catch (Exception e) {
            System.out.println("interrupted");
            e.printStackTrace();
        }
        return new IntegerEvent(e2.getValue() + e5.getValue());
    }

    @Task(version = 1, retries = 0, timeout = 1000L)
    public IntegerEvent t4(IntegerEvent e3) {
        return new IntegerEvent(4);
    }

    @Task(version = 1, retries = 0, timeout = 1000L, isReplayable = true)
    public IntegerEvent t5(@ReplayEvent("RE1") IntegerEvent re1, IntegerEvent e1) {
        if (re1 != null) {
            return new IntegerEvent(re1.getValue());
        } else {
            return new IntegerEvent(10);
        }
    }

    @Task(version = 1, retries = 0, timeout = 1000L)
    public IntegerEvent t6(IntegerEvent e5) {
        return new IntegerEvent(e5.getValue());
    }

    @Task(version = 1, retries = 0, timeout = 400L)
    public void t7(IntegerEvent e4, IntegerEvent e6) {
        if (e6.getValue() == 10) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println("interrupted");
                e.printStackTrace();
            }
        }
    }

    @Task(version = 1, retries = 0, timeout = 1000L)
    public IntegerEvent t8(IntegerEvent e1) {
        return new IntegerEvent(8);
    }

    @Task(version = 1, retries = 0, timeout = 1000L, isReplayable = true, replayRetries = 3)
    public IntegerEvent t9(@ReplayEvent("RE2") IntegerEvent re2, IntegerEvent e5, IntegerEvent e8) {
        if (re2 != null) {
            return new IntegerEvent(re2.getValue());
        } else {
            return new IntegerEvent(18);
        }
    }

    @Task(version = 1, retries = 0, timeout = 500L)
    public void t10(IntegerEvent e9) {
        if (e9.getValue() == 18) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println("interrupted");
                e.printStackTrace();
            }
        }
    }
}

class IntegerEvent implements Event {
    @JsonProperty
    Integer value;

    IntegerEvent() {
    }

    public IntegerEvent(Integer x) {
        value = x;
    }

    public Integer getValue() {
        return value;
    }
}
