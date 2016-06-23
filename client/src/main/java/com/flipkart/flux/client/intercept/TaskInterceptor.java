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
import com.flipkart.flux.client.model.ExternalEvent;
import com.flipkart.flux.client.model.Task;
import com.flipkart.flux.client.registry.ExecutableImpl;
import com.flipkart.flux.client.registry.ExecutableRegistry;
import com.flipkart.flux.client.runtime.LocalContext;
import net.sf.cglib.proxy.Enhancer;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
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
        final Set<EventDefinition> dependencySet = generateDependencySet(invocation.getArguments(),method.getParameterAnnotations(),method.getParameterTypes());
        final Object proxyReturnObject = createProxyReturnObject(method);
        final EventDefinition outputEventDefintion = generateOutputEventDefintion(proxyReturnObject);
        /* Contribute to the ongoing state machine definition */
        localContext.registerNewState(taskAnnotation.version(), generateStateIdentifier(method), null, null, taskIdentifier, taskAnnotation.retries(), taskAnnotation.timeout(), dependencySet, outputEventDefintion);
        /* Register the task with the executable registry on this jvm */
        executableRegistry.registerTask(taskIdentifier, new ExecutableImpl(invocation.getThis(), invocation.getMethod(), taskAnnotation.timeout()));

        return proxyReturnObject;
    }

    private EventDefinition generateOutputEventDefintion(Object proxyReturnObject) {
        if (proxyReturnObject == null) {
            return  null;
        }
        String eventName = ((Event) proxyReturnObject).name();
        String eventType = ((Intercepted) proxyReturnObject).getRealClassName();
        return new EventDefinition(eventName,eventType);
    }

    private Object createProxyReturnObject(final Method method) {
        if (method.getReturnType() == void.class) {
            return null;
        }
        /* The method is expected to return _something_, so we create a proxy for it */
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
        return Enhancer.create(method.getReturnType(), new Class[]{Intercepted.class}, filter, filter.getCallbacks());
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

    private Set<EventDefinition> generateDependencySet(Object[] arguments, Annotation[][] parameterAnnotations, Class<?>[] parameterTypes) {
        Set<EventDefinition> eventDefinitions = new HashSet<>();
        for (int i = 0; i < arguments.length ; i++) {
            Object argument = arguments[i];
            ExternalEvent externalEventAnnotation = checkForExternalEventAnnotation(parameterAnnotations[i]);
            if (externalEventAnnotation != null) {
                if (argument != null) {
                    throw new IllegalInvocationException("cannot pass" + argument + " as the parameter is marked an external event");
                }
                final EventDefinition definition = new EventDefinition(externalEventAnnotation.value(), parameterTypes[i].getName());
                EventDefinition existingDefinition = localContext.checkExistingDefinition(definition);
                if (existingDefinition != null) {
                    eventDefinitions.add(existingDefinition);
                } else {
                    eventDefinitions.add(definition);
                }
                continue;
            }
            if (argument instanceof Intercepted) {
                eventDefinitions.add(new EventDefinition(((Event) argument).name(), ((Intercepted)argument).getRealClassName() ));
            } else {
                eventDefinitions.add(new EventDefinition(localContext.generateEventName((Event)argument), argument.getClass().getName()));
            }
        }
        return eventDefinitions;

    }

    private ExternalEvent checkForExternalEventAnnotation(Annotation[] givenParameterAnnotations) {
        for (Annotation annotation : givenParameterAnnotations) {
            if (annotation instanceof ExternalEvent) {
                return (ExternalEvent) annotation;
            }
        }
        return null;
    }

    private String generateStateIdentifier(Method method) {
        return method.getName();
    }

    private String generateTaskIdentifier(Method method) {
        return new MethodId(method).toString();
    }
}
