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

public class SimpleOrderFulfilmentWorkflow<V extends PackedOrder> {
	
    OrderManager orderManager = new OrderManager();
    WarehouseService<V> warehouseService = new WarehouseService<V>();
    ShipmentService shipmentService = new ShipmentService();

    @Workflow(version = 1)
    public void start(OrderData orderData) {

        /* The orderManager's create call is annotated with a Task annotation
        * This means that it can be thought of as an RPC, without having to worry about
        * retries, failures and queueing
        * */
        final Promise<CreatedOrder> createdOrderPromise = orderManager.create(orderData);

        if (!isCod(createdOrderPromise)) {
            // process payment first
        }
        final Promise<PackedOrder> packedOrderPromise;

        /**
         * The packOrder call is also annotated with a Task annotation.
         * However, it is dependent on a promise that is delivered only once orderManager.create() call returns a value
         * This value is then passed on to the packOrder method.
         * Essentially, we've created a State transition from create() -> packOrder()
         */
        packedOrderPromise = warehouseService.packOrder(createdOrderPromise);
        shipmentService.ship(packedOrderPromise);
    }

    @Task(version = 1, timeout = 1000l)
    public boolean isCod(Promise<CreatedOrder> createdOrderPromise) {
        // reade the createdOrder information and determine if it is COD or no
        return createdOrderPromise.get().isCod();
    }
}
