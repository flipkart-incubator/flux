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

package com.flipkart.flux.client.intercept;

import net.sf.cglib.proxy.FixedValue;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Terrible name, but it actually does what it says.
 * This callback will return the string value that was passed to it.
 * Generally, the <code>CallbackFilter</code> would determine which method this callback should be invoked for
 * For usage, see @TaskInterceptor
 *
 * @author yogesh.nachnani
 */
@Singleton
public class ReturnGivenStringCallback implements FixedValue {

    private final String eventName;

    @Inject
    public ReturnGivenStringCallback(String generatedEventName) {
        this.eventName = generatedEventName;
    }

    @Override
    public Object loadObject() throws Exception {
        return this.eventName;
    }
}
