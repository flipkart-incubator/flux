package com.flipkart.flux.examples.benchmark;

import com.flipkart.flux.client.model.Task;

public class ArithmeticOperationsExtended {

    @Task(version = 1, retries = 3, timeout = 1000)
    public EventTypeInteger addE(EventTypeInteger a, EventTypeInteger b) {
        return new EventTypeInteger(a.getValue() + b.getValue());
    }

    @Task(version = 1, retries = 3, timeout = 1000)
    public EventTypeInteger subtractE(EventTypeInteger a, EventTypeInteger b) {
        return new EventTypeInteger(Math.abs(a.getValue() - b.getValue()));
    }

    @Task(version = 1, retries = 3, timeout = 1000)
    public EventTypeInteger incrementE(EventTypeInteger a) {
        return new EventTypeInteger(a.getValue() + 1);
    }

    @Task(version = 1, retries = 3, timeout = 1000)
    public EventTypeInteger decrementE(EventTypeInteger a) {
        return new EventTypeInteger(a.getValue() - 1);
    }

    @Task(version = 1, retries = 3, timeout = 1000)
    public EventTypeInteger multiplyE(EventTypeInteger a, EventTypeInteger b) {
        makePositive(a, b);
        return new EventTypeInteger(a.getValue() * b.getValue());
    }

    @Task(version = 1, retries = 3, timeout = 1000)
    public EventTypeInteger divideE(EventTypeInteger a, EventTypeInteger b) {
        makePositive(a, b);
        if (b.getValue() == 0) {
            return new EventTypeInteger(a.getValue());
        } else {
            return new EventTypeInteger(a.getValue() / b.getValue());
        }
    }

    @Task(version = 1, retries = 3, timeout = 1000)
    public EventTypeInteger modulusE(EventTypeInteger a, EventTypeInteger b) {
        makePositive(a, b);
        return new EventTypeInteger(a.getValue() % b.getValue());
    }

    @Task(version = 1, retries = 3, timeout = 1000)
    public EventTypeInteger leftShiftE(EventTypeInteger a) {
        return new EventTypeInteger(a.getValue() << 1);
    }

    @Task(version = 1, retries = 3, timeout = 1000)
    public EventTypeInteger rightShiftE(EventTypeInteger a) {
        return new EventTypeInteger(a.getValue() >> 1);
    }

    @Task(version = 1, retries = 3, timeout = 1000)
    public EventTypeInteger randomE(EventTypeInteger a) {
        return new EventTypeInteger((int) (Math.random() * a.getValue()));
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
