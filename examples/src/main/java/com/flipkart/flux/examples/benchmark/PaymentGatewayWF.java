package com.flipkart.flux.examples.benchmark;

import com.flipkart.flux.client.model.Workflow;

import javax.inject.Inject;

public class PaymentGatewayWF {

    @Inject
    private PaymentGatewayTasks paymentGatewayTasks;

    @SuppressWarnings("unused")
	@Workflow(version = 4)
    public void initPayment(EventTypeInteger x) {

        EventTypeInteger a = paymentGatewayTasks.paymentInit(x);

        EventTypeInteger b = paymentGatewayTasks.HDFCAuth(a, x);
        EventTypeInteger c = paymentGatewayTasks.HDFCOTPVerify(b);
        EventTypeInteger d = paymentGatewayTasks.HDFCPaymentSuccess(c);
        EventTypeInteger e = paymentGatewayTasks.HDFCPaymentFailed(c);

        EventTypeInteger b_0 = paymentGatewayTasks.SBIAuth(a, x);
        EventTypeInteger c_0 = paymentGatewayTasks.SBIOTPVerify(b_0);
        EventTypeInteger d_0 = paymentGatewayTasks.SBIPaymentSuccess(c_0);
        EventTypeInteger e_0 = paymentGatewayTasks.SBIPaymentFailed(c_0);

        EventTypeInteger b_1 = paymentGatewayTasks.SBIAuth(a, x);
        EventTypeInteger c_1 = paymentGatewayTasks.SBIOTPVerify(b_1);
        EventTypeInteger d_1 = paymentGatewayTasks.SBIPaymentSuccess(c_1);
        EventTypeInteger e_1 = paymentGatewayTasks.SBIPaymentFailed(c_1);

        EventTypeInteger b_2 = paymentGatewayTasks.HDFCAuth(a, x);
        EventTypeInteger c_2 = paymentGatewayTasks.HDFCOTPVerify(b_2);
        EventTypeInteger d_2 = paymentGatewayTasks.HDFCPaymentSuccess(c_2);
        EventTypeInteger e_2 = paymentGatewayTasks.HDFCPaymentFailed(c_2);

        EventTypeInteger b_3 = paymentGatewayTasks.SBIAuth(a, x);
        EventTypeInteger c_3 = paymentGatewayTasks.SBIOTPVerify(b_3);
        EventTypeInteger d_3 = paymentGatewayTasks.SBIPaymentSuccess(c_3);
        EventTypeInteger e_3 = paymentGatewayTasks.SBIPaymentFailed(c_3);

        EventTypeInteger b_4 = paymentGatewayTasks.HDFCAuth(a, x);
        EventTypeInteger c_4 = paymentGatewayTasks.HDFCOTPVerify(b_4);
        EventTypeInteger d_4 = paymentGatewayTasks.HDFCPaymentSuccess(c_4);
        EventTypeInteger e_4 = paymentGatewayTasks.HDFCPaymentFailed(c_4);
    }
}
