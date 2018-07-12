package com.flipkart.flux.examples.benchmark;

import com.flipkart.flux.client.FluxClientComponentModule;
import com.flipkart.flux.client.FluxClientInterceptorModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class RunBenchMarkWorkFlowA {

    public static void main(String args[]) {
         /* Initialise _your_ module*/
        final Injector injector = Guice.createInjector(new FluxClientComponentModule(), new FluxClientInterceptorModule());

        /* Note that we are using guice aop for now, hence your workflow instances need to use guice */
        BenchmarkWorkFlowA benchmarkWorkFlowA = injector.getInstance(BenchmarkWorkFlowA.class);
        BenchmarkWorkflowLatentSimulation benchmarkWorkFlowLatent = injector.getInstance(BenchmarkWorkflowLatentSimulation.class);
        BenchmarkWorkFlowAExtended benchmarkWorkFlowAExtended = injector.getInstance(BenchmarkWorkFlowAExtended.class);
        PaymentGatewayWF paymentGatewayWF = injector.getInstance(PaymentGatewayWF.class);

        /* Lets invoke our workflow */
        System.out.println("[Main] Starting BenchMarkWorkFlowA  execution");
        for (int i = 1; i <= 10 ; i++) {
            benchmarkWorkFlowA.initA(new EventTypeInteger((int) Math.random() * i));
            benchmarkWorkFlowLatent.initLatent(new EventTypeInteger(i));
            benchmarkWorkFlowAExtended.initAExtended(new EventTypeInteger(i * 10));
            paymentGatewayWF.initPayment(new EventTypeInteger(i));
        }
    }
}