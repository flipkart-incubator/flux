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

import net.sf.cglib.proxy.CallbackHelper;
import net.sf.cglib.proxy.NoOp;

import java.lang.reflect.Method;

/**
 * This callback filter helps redirect all name method calls to the given FixedValue callback
 * All other method calls go to the NoOp callback instance
 * This is used by the <code>TaskInterceptor</code> to create proxy objects that would respond with the given FixedValue callback
 * whenever their name() method is called
 *
 * @author yogesh.nachnani
 */
public abstract class ProxyEventCallbackFilter extends CallbackHelper {

    @SuppressWarnings("rawtypes")
	public ProxyEventCallbackFilter(Class<?> superclass, Class[] interfaces) {
        super(superclass, interfaces);
    }


    @Override
    protected Object getCallback(Method method) {
        if(method.getName().equals("name") && method.getReturnType().equals(String.class)) {
            return getNameCallback();
        }
        if (method.getName().equals("getRealClassName") && method.getReturnType().equals(String.class)) {
            return getRealClassName();
        }
        return NoOp.INSTANCE;
    }

    protected abstract ReturnGivenStringCallback getRealClassName();

    protected abstract ReturnGivenStringCallback getNameCallback();
}
