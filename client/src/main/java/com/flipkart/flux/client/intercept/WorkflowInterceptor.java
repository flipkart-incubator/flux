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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.api.EventData;
import com.flipkart.flux.client.model.CorrelationId;
import com.flipkart.flux.client.model.Event;
import com.flipkart.flux.client.model.Workflow;
import com.flipkart.flux.client.runtime.FluxRuntimeConnector;
import com.flipkart.flux.client.runtime.LocalContext;
import com.google.common.annotations.VisibleForTesting;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

import static com.flipkart.flux.client.constant.ClientConstants._VERSION;

/**
 * This intercepts the invocation to <code>@Workflow</code> methods.
 * The interception mechanism helps create a state machine definition that is later submitted to the Flux orchestrator
 * for execution
 * @author yogesh.nachnani
 */
@Singleton
public class WorkflowInterceptor implements MethodInterceptor {

    public static final String CLIENT = "client";
    @Inject
    private LocalContext localContext;
    @Inject
    private Provider<FluxRuntimeConnector> connectorProvider;

    @Inject
    private Provider<ObjectMapper> objectMapperProvider;

    public WorkflowInterceptor() {
    }

    @VisibleForTesting
    public WorkflowInterceptor(LocalContext localContext, Provider<FluxRuntimeConnector> connectorProvider, Provider<ObjectMapper> objectMapperProvider) {
        this();
        this.localContext = localContext;
        this.connectorProvider = connectorProvider;
        this.objectMapperProvider = objectMapperProvider;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        try {
            final Method method = invocation.getMethod();
            final Workflow[] workFlowAnnotations = method.getAnnotationsByType(Workflow.class);
            checkForBadSignatures(invocation);
            final String correlationId = checkForCorrelationId(invocation.getArguments());
            Workflow workflow = workFlowAnnotations[0];
            localContext.registerNew(generateWorkflowIdentifier(method, workflow), workflow.version(), workflow.description(),correlationId);
            registerEventsForArguments(invocation.getArguments());
            invocation.proceed();
            connectorProvider.get().submitNewWorkflow(localContext.getStateMachineDef());
            return null ; // TODO, return a proxy object
        }
        finally {
            this.localContext.reset();
        }
    }

    private String checkForCorrelationId(Object[] arguments) throws IllegalAccessException {
        final String[] correlationId = {null};
        /* Iterate over given arguments to find if there is any argument that has a field marked with <code>CorrelationId</code> */
        for (Object anArgument : arguments) {
            final Field[] allFields = anArgument.getClass().getDeclaredFields();
            /* Search for any field which is of type String and has a CorrelationId annotation */
            final Optional<Field> possibleAnnotatedField = Arrays.stream(allFields).
                filter(field -> String.class.isAssignableFrom(field.getType())).
                filter(field -> field.getAnnotationsByType(CorrelationId.class).length > 0).
                findAny();
            /* If we have a field that matches above criteria, we populate the correlationId variable and break */
            if (possibleAnnotatedField.isPresent()) {
                final Field correlationIdAnnotatedField = possibleAnnotatedField.get();
                final boolean originalAccessibility = correlationIdAnnotatedField.isAccessible();
                if (!originalAccessibility) {
                    correlationIdAnnotatedField.setAccessible(true);
                }
                try {
                    correlationId[0] = (String) correlationIdAnnotatedField.get(anArgument);
                    break;
                } finally {
                    if (!originalAccessibility) {
                        correlationIdAnnotatedField.setAccessible(false);
                    }
                }
            }
        }
        return correlationId[0];
    }

    private void registerEventsForArguments(Object[] arguments) throws JsonProcessingException {
        if (arguments.length == 0) {
            return;
        }
        final int lengthOfArguments = getRealLengthOfArguments(arguments);
        EventData[] eventDatas = new EventData[lengthOfArguments];
        int i = 0;
        for(Object anArgument : arguments) {
            /* We traverse the array of events passed as an argument and derive EventData from each element */
            if (anArgument.getClass().isArray()) {
                Object[] objects = (Object[]) anArgument;
                for (Object anObjectArrayMember : objects) {
                    addToEventDataArray(eventDatas, i, anObjectArrayMember);
                    i++;
                }

            } else { /* regular Event object as an argument */
                addToEventDataArray(eventDatas, i, anArgument);
                i++;
            }
        }
        localContext.addEvents(eventDatas);
    }

    private void addToEventDataArray(EventData[] eventDatas, int i, Object anObject) throws JsonProcessingException {
        final String eventName = localContext.generateEventName((Event) anObject);
        eventDatas[i] = new EventData(eventName, anObject.getClass().getName(),
                                      objectMapperProvider.get().writeValueAsString(anObject), CLIENT);
    }


    private int getRealLengthOfArguments(Object[] arguments) {
        int len = 0;
        for (Object anArgument : arguments) {
            if(anArgument.getClass().isArray()) {
                Object[] objects = (Object[]) anArgument;
                len += objects.length;
            } else {
                len++;
            }
        }
        return len;
    }


    private void checkForBadSignatures(MethodInvocation invocation) {
        Method method = invocation.getMethod();
        final Class<?> returnType = method.getReturnType();
        if (!returnType.equals(void.class)) {
            throw new IllegalSignatureException(new MethodId(method),"A workflow method can only return void");
        }
        final Class<?>[] parameterTypes = method.getParameterTypes();
        for (Class<?> aParamType : parameterTypes) {
            if (!Event.class.isAssignableFrom(aParamType) && !aParamType.isArray()) {
                throw new IllegalSignatureException(new MethodId(method), "Parameter types should implement the Event interface. Collections of events are also not allowed");
            }
        }
    }

    private String generateWorkflowIdentifier(Method method, Workflow workflow) {
        return new MethodId(method).toString() + _VERSION + workflow.version();
    }

}
