package com.flipkart.flux.examples.benchmark;

import com.flipkart.flux.client.model.Workflow;

import javax.inject.Inject;

public class BenchmarkWorkFlowA {

    @Inject
    private ArithmeticOperations arithmeticOperations;

    @SuppressWarnings("unused")
	@Workflow(version = 1)
    public void initA(EventTypeInteger x) {
        EventTypeInteger first = arithmeticOperations.random(x);
        EventTypeInteger second = arithmeticOperations.random(x);
        EventTypeInteger third = arithmeticOperations.add(first, second);
        EventTypeInteger four = arithmeticOperations.subtract(first, second);
        EventTypeInteger fifth = arithmeticOperations.increment(third);
        EventTypeInteger sixth = arithmeticOperations.decrement(four);
        EventTypeInteger seventh = arithmeticOperations.modulus(fifth, sixth);
        EventTypeInteger eighth = arithmeticOperations.leftShift(seventh);
        EventTypeInteger ninth = arithmeticOperations.rightShift(seventh);
        EventTypeInteger tenth = arithmeticOperations.add(eighth, ninth);

        EventTypeInteger first_1 = arithmeticOperations.random(x);
        EventTypeInteger second_1 = arithmeticOperations.random(x);
        EventTypeInteger third_1 = arithmeticOperations.add(first_1, second_1);
        EventTypeInteger four_1 = arithmeticOperations.subtract(first_1, second_1);
        EventTypeInteger fifth_1 = arithmeticOperations.increment(third_1);
        EventTypeInteger sixth_1 = arithmeticOperations.decrement(four_1);
        EventTypeInteger seventh_1 = arithmeticOperations.modulus(fifth_1, sixth_1);
        EventTypeInteger eighth_1 = arithmeticOperations.leftShift(seventh_1);
        EventTypeInteger ninth_1 = arithmeticOperations.rightShift(seventh_1);
        EventTypeInteger tenth_1 = arithmeticOperations.add(eighth_1, ninth_1);
    }
}
