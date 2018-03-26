package com.flipkart.flux.examples.benchmark;

import com.flipkart.flux.client.model.Workflow;

import javax.inject.Inject;

public class BenchmarkWorkflowLatentSimulation {

    @Inject
    private LatentArithmeticOperations latentArithmeticOperations;

    @Workflow(version = 1)
    public void initLatent(EventTypeInteger x) {
        EventTypeInteger first = latentArithmeticOperations.randomLatent(x);
        EventTypeInteger second = latentArithmeticOperations.randomLatent(x);
        EventTypeInteger third = latentArithmeticOperations.addLatent(first, second);
        EventTypeInteger four = latentArithmeticOperations.subtractLatent(first, second);
        EventTypeInteger fifth = latentArithmeticOperations.incrementLatent(third);
        EventTypeInteger sixth = latentArithmeticOperations.decrementLatent(four);
        EventTypeInteger seventh = latentArithmeticOperations.modulusLatent(fifth, sixth);
        EventTypeInteger eighth = latentArithmeticOperations.leftShiftLatent(seventh);
        EventTypeInteger ninth = latentArithmeticOperations.rightShiftLatent(seventh);
        EventTypeInteger tenth = latentArithmeticOperations.addLatent(eighth, ninth);


        EventTypeInteger first_1 = latentArithmeticOperations.randomLatent(x);
        EventTypeInteger second_1 = latentArithmeticOperations.randomLatent(x);
        EventTypeInteger third_1 = latentArithmeticOperations.addLatent(first_1, second_1);
        EventTypeInteger four_1 = latentArithmeticOperations.subtractLatent(first_1, second_1);
        EventTypeInteger fifth_1 = latentArithmeticOperations.incrementLatent(third_1);
        EventTypeInteger sixth_1 = latentArithmeticOperations.decrementLatent(four_1);
        EventTypeInteger seventh_1 = latentArithmeticOperations.modulusLatent(fifth_1, sixth_1);
        EventTypeInteger eighth_1 = latentArithmeticOperations.leftShiftLatent(seventh_1);
        EventTypeInteger ninth_1 = latentArithmeticOperations.rightShiftLatent(seventh_1);
        EventTypeInteger tenth_1 = latentArithmeticOperations.addLatent(eighth_1, ninth_1);

    }

}
