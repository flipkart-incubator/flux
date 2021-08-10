package com.flipkart.flux.examples.benchmark;

import com.flipkart.flux.client.model.Task;

public class LatentArithmeticOperations {
    @Task(version = 1, retries = 3, timeout = 1000)
    public EventTypeInteger addLatent(EventTypeInteger a, EventTypeInteger b) {
        shouldSleep();
        return new EventTypeInteger(a.getValue() + b.getValue());
    }

    @Task(version = 1, retries = 3, timeout = 1000)
    public EventTypeInteger subtractLatent(EventTypeInteger a, EventTypeInteger b) {
        shouldSleep();
        return new EventTypeInteger(Math.abs(a.getValue() - b.getValue()));
    }

    @Task(version = 1, retries = 3, timeout = 1000)
    public EventTypeInteger incrementLatent(EventTypeInteger a) {
        shouldSleep();
        return new EventTypeInteger(a.getValue() + 1);
    }

    @Task(version = 1, retries = 3, timeout = 1000)
    public EventTypeInteger decrementLatent(EventTypeInteger a) {
        shouldSleep();
        return new EventTypeInteger(a.getValue() - 1);
    }

    @Task(version = 1, retries = 3, timeout = 1000)
    public EventTypeInteger multiplyLatent(EventTypeInteger a, EventTypeInteger b) {
        shouldSleep();
        makePositive(a, b);
        return new EventTypeInteger(a.getValue() * b.getValue());
    }

    @Task(version = 1, retries = 3, timeout = 1000)
    public EventTypeInteger divideLatent(EventTypeInteger a, EventTypeInteger b) {
        shouldSleep();
        makePositive(a, b);
        return new EventTypeInteger(a.getValue() / b.getValue());
    }

    @Task(version = 1, retries = 3, timeout = 1000)
    public EventTypeInteger modulusLatent(EventTypeInteger a, EventTypeInteger b) {
        shouldSleep();
        makePositive(a, b);
        return new EventTypeInteger(a.getValue() % b.getValue());
    }

    @Task(version = 1, retries = 3, timeout = 1000)
    public EventTypeInteger leftShiftLatent(EventTypeInteger a) {
        shouldSleep();
        return new EventTypeInteger(a.getValue() << 1);
    }

    @Task(version = 1, retries = 3, timeout = 1000)
    public EventTypeInteger rightShiftLatent(EventTypeInteger a) {
        shouldSleep();
        return new EventTypeInteger(a.getValue() >> 1);
    }

    @Task(version = 1, retries = 3, timeout = 1000)
    public EventTypeInteger randomLatent(EventTypeInteger a) {
        shouldSleep();
        return new EventTypeInteger((int) (Math.random() * a.getValue()));
    }

    public void shouldSleep() {
        int x = (int) (Math.random() * 10000.0);
        System.out.println("x value is " + x);
        try {
            if (x <= 5) {
                Thread.sleep(1100);
            }
        } catch (Exception ex) {
        }
    }

    public void makePositive(EventTypeInteger a, EventTypeInteger b) {
        if (a.getValue() <= 0) {
            a.value *= -1;
        }
        if (b.getValue() <= 0) {
            b.value *= -1;
        }
    }

    public void makePositive(EventTypeInteger a) {
        if (a.getValue() <= 0) {
            a.value *= -1;
        }
    }
}
