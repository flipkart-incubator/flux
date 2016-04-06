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

/**
 * Dummy service in charge of magically shipping the item to the customer
 */
public class ShipmentService {
    private FinalMileService finalMileService = new FinalMileService();
    private PaymentService paymentService = new PaymentService();

    public Promise<OrderDeliveryInformation> ship(Promise<PackedOrder> packedOrderPromise) {
        // Routing it from warehouse to warehouse till it reaches the final delivery hub.
        /* Before final delivery */
        if(packedOrderPromise.get().isCod()) {
            return finalMileService.deliver(packedOrderPromise,paymentService.processCod(packedOrderPromise));
        } else {
            return finalMileService.deliver(packedOrderPromise);
        }
    }
}
