package com.flipkart.flux.examples.benchmark;

import com.flipkart.flux.client.model.Task;

public class PaymentGatewayTasks {

    @Task(version = 4, retries = 3, timeout = 1000)
    public EventTypeInteger paymentInit(EventTypeInteger a) {
        /*
        Even value: Bank0 "SBI"
        odd value: Bank1 "HDFC"
         */
        if (a.getValue() % 2 == 0) {
            return new EventTypeInteger(0);
        } else {
            return new EventTypeInteger(1);
        }
    }

    @Task(version = 4, retries = 3, timeout = 1000)
    public EventTypeInteger SBIAuth(EventTypeInteger a, EventTypeInteger b) {
        /*
        Random computation to increase task compute usage
         */
        long p = 10000;
        long q = 20000;
        long r = p * q;
        p = q + (r * (long) Math.random());

        return new EventTypeInteger((int) ((int) Math.random() * a.getValue() + b.getValue() - p));
    }

    @Task(version = 4, retries = 3, timeout = 1000)
    public EventTypeInteger HDFCAuth(EventTypeInteger a, EventTypeInteger b) {
        /*
        Random computation to increase task compute usage
         */
        long p = 10000;
        long q = 20000;
        long r = p * q;
        p = q + (r * (long) Math.random());

        return new EventTypeInteger((int) ((int) Math.random() * a.getValue() + b.getValue() - p));
    }

    @Task(version = 4, retries = 3, timeout = 1000)
    public EventTypeInteger SBIOTPVerify(EventTypeInteger a) {
        /*
        Random computation to increase task compute usage
         */
        long p = 10000;
        long q = 20000;
        long r = p * q;
        p = p + (r * (long) Math.random());

        return new EventTypeInteger((int) Math.random() + a.getValue());
    }

    @Task(version = 4, retries = 3, timeout = 1000)
    public EventTypeInteger HDFCOTPVerify(EventTypeInteger a) {
        /*
        Random computation to increase task compute usage
         */
        long p = 10000;
        long q = 20000;
        long r = p * q;
        p = p + (r * (long) Math.random());

        return new EventTypeInteger((int) Math.random() + a.getValue());
    }

    @Task(version = 4, retries = 3, timeout = 1000)
    public EventTypeInteger SBIPaymentSuccess(EventTypeInteger a) {
        return new EventTypeInteger(a.getValue() * (int) Math.random());
    }

    @Task(version = 4, retries = 3, timeout = 1000)
    public EventTypeInteger HDFCPaymentSuccess(EventTypeInteger a) {
        return new EventTypeInteger(a.getValue() * (int) Math.random());
    }

    @Task(version = 4, retries = 3, timeout = 1000)
    public EventTypeInteger SBIPaymentFailed(EventTypeInteger a) {
        return new EventTypeInteger(0);
    }

    @Task(version = 4, retries = 3, timeout = 1000)
    public EventTypeInteger HDFCPaymentFailed(EventTypeInteger a) {
        return new EventTypeInteger(0);
    }
}
