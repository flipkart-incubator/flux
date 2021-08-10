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
 *
 */

package com.flipkart.flux.client.registry;

import com.flipkart.flux.client.intercept.MethodId;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This provides a way for the core runtime to execute client side code
 *
 * @author yogesh.nachnani
 */
public class ExecutableImpl implements Executable {

    private final Method toInvoke;
    private final Object singletonMethodOwner;
    private final long timeout;

    public ExecutableImpl(Object singletonMethodOwner, Method toInvoke, long timeout) {
        this.singletonMethodOwner = singletonMethodOwner;
        this.toInvoke = toInvoke;
        this.timeout = timeout;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ExecutableImpl that = (ExecutableImpl) o;

        if (timeout != that.timeout) {
            return false;
        }
        if (!toInvoke.equals(that.toInvoke)) {
            return false;
        }
        return singletonMethodOwner.equals(that.singletonMethodOwner);
    }

    @Override
    public int hashCode() {
        int result = toInvoke.hashCode();
        result = 31 * result + singletonMethodOwner.hashCode();
        result = 31 * result + (int) (timeout ^ (timeout >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Executable{" +
                "singletonMethodOwner=" + singletonMethodOwner +
                ", toInvoke=" + toInvoke +
                '}';
    }

    public String getName() {
        return new MethodId(toInvoke).getMethodName();
    }

    public long getTimeout() {
        return timeout;
    }

    @Override
    public Object execute(Object[] parameters) {
        try {
            return toInvoke.invoke(singletonMethodOwner, parameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return toInvoke.getParameterTypes();
    }
}
