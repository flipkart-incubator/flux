package com.flipkart.flux.client.registry;

import com.flipkart.flux.client.intercept.MethodId;

import java.lang.reflect.Method;

/**
 * This provides a way for the core runtime to execute client side code
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExecutableImpl that = (ExecutableImpl) o;

        if (timeout != that.timeout) return false;
        if (!toInvoke.equals(that.toInvoke)) return false;
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
        throw new UnsupportedOperationException("not implemented yet");
    }
}
