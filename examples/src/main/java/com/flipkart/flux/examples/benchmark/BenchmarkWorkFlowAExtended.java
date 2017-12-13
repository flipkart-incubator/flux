package com.flipkart.flux.examples.benchmark;

import com.flipkart.flux.client.model.Workflow;

import javax.inject.Inject;

public class BenchmarkWorkFlowAExtended {

    @Inject
    private ArithmeticOperationsExtended arithmeticOperationsExtended;

    @Workflow(version = 1)
    public void initAExtended(EventTypeInteger x){
        EventTypeInteger first  = arithmeticOperationsExtended.randomE(x);
        EventTypeInteger second = arithmeticOperationsExtended.randomE(x);
        EventTypeInteger third = arithmeticOperationsExtended.addE(first,second);
        EventTypeInteger four = arithmeticOperationsExtended.subtractE(first,second);
        EventTypeInteger fifth = arithmeticOperationsExtended.incrementE(third);
        EventTypeInteger sixth = arithmeticOperationsExtended.decrementE(four);
        EventTypeInteger seventh = arithmeticOperationsExtended.modulusE(fifth,sixth);
        EventTypeInteger eighth = arithmeticOperationsExtended.multiplyE(seventh,sixth);
        EventTypeInteger ninth = arithmeticOperationsExtended.divideE(seventh,eighth);
        EventTypeInteger tenth = arithmeticOperationsExtended.leftShiftE(ninth);
        EventTypeInteger eleventh = arithmeticOperationsExtended.rightShiftE(tenth);

        EventTypeInteger first_1  = arithmeticOperationsExtended.randomE(x);
        EventTypeInteger second_1 = arithmeticOperationsExtended.randomE(x);
        EventTypeInteger third_1 = arithmeticOperationsExtended.addE(first_1,second_1);
        EventTypeInteger four_1 = arithmeticOperationsExtended.subtractE(first_1,second_1);
        EventTypeInteger fifth_1 = arithmeticOperationsExtended.incrementE(third_1);
        EventTypeInteger sixth_1 = arithmeticOperationsExtended.decrementE(four_1);
        EventTypeInteger seventh_1 = arithmeticOperationsExtended.modulusE(fifth_1,sixth_1);
        EventTypeInteger eighth_1 = arithmeticOperationsExtended.multiplyE(seventh_1,sixth_1);
        EventTypeInteger ninth_1 = arithmeticOperationsExtended.divideE(seventh_1,eighth_1);
        EventTypeInteger tenth_1 = arithmeticOperationsExtended.leftShiftE(ninth_1);
        EventTypeInteger eleventh_1 = arithmeticOperationsExtended.rightShiftE(tenth_1);


    }
}
