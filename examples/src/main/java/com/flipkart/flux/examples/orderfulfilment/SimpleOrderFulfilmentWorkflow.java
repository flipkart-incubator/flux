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
 */

package com.flipkart.flux.examples.orderfulfilment;

import com.flipkart.flux.client.model.Promise;
import com.flipkart.flux.client.model.Task;
import com.flipkart.flux.client.model.Workflow;

/**
 * This is the coded example to model the state machine described at
 * https://github.com/flipkart-incubator/flux/wiki/Traditional-State-Machine---Order-Fulfilment
 */
public class SimpleOrderFulfilmentWorkflow {
    OrderManager orderManager = new OrderManager();
    WarehouseService<V> warehouseService = new WarehouseService<V>();
    ShipmentService shipmentService = new ShipmentService();
    PaymentService paymentService = new PaymentService();

    @Workflow(version = 1)
    public void start(OrderData orderData) {

        /* The orderManager's create call is annotated with a Task annotation
        * This means that it can be thought of as an RPC, without having to worry about
        * retries, failures and queueing
        * */

        final Promise<CreatedOrder> createdOrder = orderManager.create(orderData);
        /* Now, we are in the Order Created state of the state machine */

        Promise<ConfirmedOrder> confirmedOrder = processPaymentIfRequired(createdOrder);
        /* With a confirmed order, we can proceed for packing */

        /**
         * The packOrder call is also annotated with a Task annotation.
         * However, it is dependent on a promise that is available only once the orderManager.confirmOrder() call returns a value
         * This value is then passed on to the packOrder method.
         */
        /* Warehouse service can pack a confirmed order */
        final Promise<PackedOrder> readyPackage = warehouseService.packOrder(confirmedOrder);
        /* Shipment service can ship a ready Package */
        final Promise<OrderDeliveryInformation> orderDeliveryInformation = shipmentService.ship(readyPackage);

    }

    @Task(version = 1, timeout = 1000l)
    public Promise<ConfirmedOrder> processPaymentIfRequired(Promise<CreatedOrder> createdOrderPromise) {
        /* Given that we are in the "Order Created" state of the state machine, we decide on how to proceed */
        final CreatedOrder createdOrder = createdOrderPromise.get();
        if (createdOrder.isCod()){
            /* The call to paymentService.processOnlinePayment() is equivalent to raising the "Payment Pending" event. */
            return orderManager.confirmOrder(createdOrderPromise,paymentService.processOnlinePayment(createdOrderPromise));
        } else {
            return orderManager.confirmOrder(createdOrder);
        }
    }
}
