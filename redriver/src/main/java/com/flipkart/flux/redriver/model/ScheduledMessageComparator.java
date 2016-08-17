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

package com.flipkart.flux.redriver.model;

import java.util.Comparator;

/**
 *
 */
public class ScheduledMessageComparator implements Comparator<ScheduledMessage> {
    @Override
    public int compare(ScheduledMessage o1, ScheduledMessage o2) {
        if(o1.getScheduledTime() < o2.getScheduledTime()) {
            return -1;
        } else {
            return 1;
        }
    }
}
