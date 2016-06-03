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

package com.flipkart.flux.client.intercept;

import com.flipkart.flux.api.EventDefinition;
import com.flipkart.flux.client.model.Event;
import com.flipkart.flux.client.model.Task;
import com.flipkart.flux.client.registry.ExecutableImpl;
import com.flipkart.flux.client.registry.ExecutableRegistry;
import com.flipkart.flux.client.runtime.LocalContext;
import net.sf.cglib.proxy.Enhancer;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * This intercepts the invocation to <code>@Task</code> methods
 * It executes the actual method in case it has been explicitly invoked by the Flux runtime via RPC
 * Else, it intercepts the call and adds a task+state combination to the current local state machine definition
 * @author yogesh.nachnani
 */
public class TaskInterceptor implements MethodInterceptor {

    @Inject
    private LocalContext localContext;
    @Inject
    private ExecutableRegistry executableRegistry;

    /* Used to create an empty interceptor in the Guice module. The private members are injected later.
      Guice takes care of creating a complete object
    */
    public TaskInterceptor() {
    }
    /* Protected - since it makes sense to use this constructor only in tests */
    TaskInterceptor(LocalContext localContext,ExecutableRegistry executableRegistry) {
        this.localContext = localContext;
        this.executableRegistry = executableRegistry;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        checkForBadSignatures(invocation);
        if (!localContext.isWorkflowInterception()) {
            return invocation.proceed();
        }
        final Method method = invocation.getMethod();
        final Task taskAnnotation = method.getAnnotationsByType(Task.class)[0];
        final String taskIdentifier = generateTaskIdentifier(method);
        localContext.registerNewState(taskAnnotation.version(), generateStateIdentifier(method), null, null, taskIdentifier, taskAnnotation.retries(), taskAnnotation.timeout(), generateEventDefs(invocation.getArguments()));
        executableRegistry.registerTask(taskIdentifier, new ExecutableImpl(invocation.getThis(), invocation.getMethod(), taskAnnotation.timeout()));
        if (method.getReturnType() == void.class) {
            return null;
        }
        final String eventName = localContext.generateEventName(new Event() {
            @Override
            public String name() {
                return method.getReturnType().getName();
            }
        });
        final ReturnGivenStringCallback eventNameCallback = new ReturnGivenStringCallback(eventName);
        final ReturnGivenStringCallback realClassNameCallback = new ReturnGivenStringCallback(method.getReturnType().getName());
        final ProxyEventCallbackFilter filter = new ProxyEventCallbackFilter(method.getReturnType(),new Class[]{Intercepted.class}) {
            @Override
            protected ReturnGivenStringCallback getRealClassName() {
                return realClassNameCallback;
            }

            @Override
            public ReturnGivenStringCallback getNameCallback() {
                return eventNameCallback;
            }
        };
        return Enhancer.create(method.getReturnType(),new Class[]{Intercepted.class}, filter,filter.getCallbacks());
    }

    private void checkForBadSignatures(MethodInvocation invocation) {
        final Method method = invocation.getMethod();
        final Class<?>[] parameterTypes = method.getParameterTypes();
        for (Class<?> parameterType : parameterTypes) {
            if (!Event.class.isAssignableFrom(parameterType)) {
                throw new IllegalSignatureException(new MethodId(method),"Task parameters need to implement the com.flipkart.flux.client.model.Event interface. Found parameter of type"+parameterType + " which does not");
            }
        }
    }

    private Set<EventDefinition> generateEventDefs(Object[] arguments) {
        Set<EventDefinition> eventDefinitions = new HashSet<>();
        for (Object argument : arguments) {
            if (argument instanceof Intercepted) {
                eventDefinitions.add(new EventDefinition(((Event) argument).name(), ((Intercepted)argument).getRealClassName() ));
            } else {
                eventDefinitions.add(new EventDefinition(localContext.generateEventName((Event)argument), argument.getClass().getName()));
            }
        }
        return eventDefinitions;

    }

    private String generateStateIdentifier(Method method) {
        return method.getName();
    }

    private String generateTaskIdentifier(Method method) {
        return new MethodId(method).toString();
    }
}
