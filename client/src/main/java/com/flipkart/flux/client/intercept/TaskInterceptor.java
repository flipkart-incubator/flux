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

import static com.flipkart.flux.client.constant.ClientConstants.CLIENT;
import static com.flipkart.flux.client.constant.ClientConstants.REPLAY_EVENT;
import static com.flipkart.flux.client.constant.ClientConstants._VERSION;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Provider;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.api.EventData;
import com.flipkart.flux.api.EventDefinition;
import com.flipkart.flux.client.guice.annotation.IsolatedEnv;
import com.flipkart.flux.client.model.Event;
import com.flipkart.flux.client.model.ExternalEvent;
import com.flipkart.flux.client.model.ReplayEvent;
import com.flipkart.flux.client.model.Task;
import com.flipkart.flux.client.registry.ExecutableImpl;
import com.flipkart.flux.client.registry.ExecutableRegistry;
import com.flipkart.flux.client.runtime.LocalContext;

import net.sf.cglib.proxy.Enhancer;

/**
 * This intercepts the invocation to <code>@Task</code> methods
 * It executes the actual method in case it has been explicitly invoked by the Flux runtime via RPC
 * Else, it intercepts the call and adds a task+state combination to the current local state machine definition
 *
 * @author yogesh.nachnani
 */
public class TaskInterceptor implements MethodInterceptor {

    @Inject
    private LocalContext localContext;
    @Inject
    @IsolatedEnv
    private ExecutableRegistry executableRegistry;
    @Inject
    private Provider<ObjectMapper> objectMapperProvider;

    private static final Logger logger = LogManager.getLogger(TaskInterceptor.class);


    /* Used to create an empty interceptor in the Guice module. The private members are injected later.
      Guice takes care of creating a complete object
    */
    public TaskInterceptor() {
    }

    /* Protected - since it makes sense to use this constructor only in tests */
    TaskInterceptor(LocalContext localContext, ExecutableRegistry executableRegistry, Provider<ObjectMapper> objectMapperProvider) {
        this.localContext = localContext;
        this.executableRegistry = executableRegistry;
        this.objectMapperProvider = objectMapperProvider;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        checkForBadSignatures(invocation);
        if (!localContext.isWorkflowInterception()) {
            return invocation.proceed();
        }
        final Method method = invocation.getMethod();
        final Task taskAnnotation = method.getAnnotationsByType(Task.class)[0];
        final String taskIdentifier = generateTaskIdentifier(method, taskAnnotation);
        final List<EventDefinition> dependencySet = generateDependencyList(invocation.getArguments(), method.getParameterAnnotations(), method.getParameterTypes());
        final Object proxyReturnObject = createProxyReturnObject(method);
        final EventDefinition outputEventDefintion = generateOutputEventDefintion(proxyReturnObject);

        //throw exception if state machine and state versions doesn't match
        if (localContext.getStateMachineDef() != null && localContext.getStateMachineDef().getVersion() != taskAnnotation.version()) {
            throw new VersionMismatchException("Mismatch between State machine and state versions for State: " + method.getDeclaringClass().getName() + "."
                    + generateStateIdentifier(method) + ". StateMachine version: " + localContext.getStateMachineDef().getVersion() + ". State version: " + taskAnnotation.version());
        }
        /* Contribute to the ongoing state machine definition */
        if (taskAnnotation.isReplayable())
            localContext.registerNewState(taskAnnotation.version(), generateStateIdentifier(method), null, null, taskIdentifier, taskAnnotation.retries(), taskAnnotation.timeout(), taskAnnotation.isReplayable(), dependencySet, outputEventDefintion, taskAnnotation.replayRetries());
        else
            localContext.registerNewState(taskAnnotation.version(), generateStateIdentifier(method), null, null, taskIdentifier, taskAnnotation.retries(), taskAnnotation.timeout(), dependencySet, outputEventDefintion);


        /* Register the task with the executable registry on this jvm */
        executableRegistry.registerTask(taskIdentifier, new ExecutableImpl(invocation.getThis(), invocation.getMethod(), taskAnnotation.timeout()));

        return proxyReturnObject;
    }

    private EventDefinition generateOutputEventDefintion(Object proxyReturnObject) {
        if (proxyReturnObject == null) {
            return null;
        }
        String eventName = ((Event) proxyReturnObject).name();
        String eventType = ((Intercepted) proxyReturnObject).getRealClassName();
        return new EventDefinition(eventName, eventType);
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
        final ProxyEventCallbackFilter filter = new ProxyEventCallbackFilter(method.getReturnType(), new Class[]{Intercepted.class}) {
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
                throw new IllegalSignatureException(new MethodId(method), "Task parameters need to implement the com.flipkart.flux.client.model.Event interface. Found parameter of type" + parameterType + " which does not");
            }
        }
    }

    private List<EventDefinition> generateDependencyList(Object[] arguments, Annotation[][] parameterAnnotations, Class<?>[] parameterTypes) throws JsonProcessingException, Exception {
        List<EventDefinition> eventDefinitions = new LinkedList<>();
        for (int i = 0; i < arguments.length; i++) {
            Object argument = arguments[i];
            Object typeOfEvent = checkForEventAnnotation(parameterAnnotations[i]);
            if (typeOfEvent instanceof ExternalEvent) {
                if (argument != null) {
                    throw new IllegalInvocationException("cannot pass " + argument + " as the parameter is marked as external/replay event");
                }
                checkAndAddEventToEventDefinition(eventDefinitions, typeOfEvent, argument, parameterTypes, i);
                continue;
            } else if (typeOfEvent instanceof ReplayEvent) {
                if (argument != null) {
                    throw new IllegalInvocationException("cannot pass " + argument + " as the parameter is marked as external/replay event");
                }
                checkAndAddEventToEventDefinition(eventDefinitions, typeOfEvent, argument, parameterTypes, i);
                continue;
            }

            if (argument instanceof Intercepted) {
                eventDefinitions.add(new EventDefinition(((Event) argument).name(), ((Intercepted) argument).getRealClassName()));
            } else {
                String eventName = localContext.generateEventName((Event) argument);
                eventDefinitions.add(new EventDefinition(eventName, argument.getClass().getName()));
                localContext.addEvents(new EventData(eventName, argument.getClass().getName(), objectMapperProvider.get().writeValueAsString(argument), CLIENT));
            }
        }
        return eventDefinitions;
    }


    /***
     * Checks whether eventDefinition exists, if yes, add it to eventDefinition.
     * @param eventDefinitions
     * @param eventAnnotation
     * @param argument
     * @param parameterTypes
     * @param argumentIndex
     */
    private void checkAndAddEventToEventDefinition(List<EventDefinition> eventDefinitions, Object eventAnnotation, Object argument, Class<?>[] parameterTypes, int argumentIndex) throws Exception {

        EventDefinition definition = null;
        try {
            definition = new EventDefinition(((ExternalEvent) eventAnnotation).value(), parameterTypes[argumentIndex].getName());
        } catch (ClassCastException e) {
            definition = new EventDefinition(((ReplayEvent) eventAnnotation).value(), parameterTypes[argumentIndex].getName(), REPLAY_EVENT);

        } catch (Exception e){
            logger.error(" Error while adding event to event definition" + e.getMessage(), e);
        } finally {
            if (definition != null) {
                EventDefinition existingDefinition = localContext.checkExistingDefinition(definition);
                if (existingDefinition != null) {
                    eventDefinitions.add(existingDefinition);
                } else {
                    eventDefinitions.add(definition);
                }
            }
        }


    }

    /***
     * checks if the current annotation is of type ExternalEvent/ReplayEvent
     * @param givenParameterAnnotations
     * @return
     */
    @SuppressWarnings("unchecked")
	private <T> T checkForEventAnnotation(Annotation[] givenParameterAnnotations) {
        for (Annotation annotation : givenParameterAnnotations) {
            if (annotation instanceof ExternalEvent) {
                return (T) annotation;
            } else if (annotation instanceof ReplayEvent) {
                return (T) annotation;
            }
        }
        return (T) Optional.empty();
    }

    private String generateStateIdentifier(Method method) {
        return method.getName();
    }

    private String generateTaskIdentifier(Method method, Task task) {
        return new MethodId(method).toString() + _VERSION + task.version();
    }
}