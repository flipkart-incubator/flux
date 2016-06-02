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

import com.flipkart.flux.api.EventData;
import com.flipkart.flux.client.model.Event;
import com.flipkart.flux.client.model.Workflow;
import com.flipkart.flux.client.runtime.FluxRuntimeConnector;
import com.flipkart.flux.client.runtime.LocalContext;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Method;

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
    private FluxRuntimeConnector connector;

    public WorkflowInterceptor() {
    }

    public WorkflowInterceptor(LocalContext localContext, FluxRuntimeConnector connector) {
        this.localContext = localContext;
        this.connector = connector;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        try {
            final Method method = invocation.getMethod();
            final Workflow[] workFlowAnnotations = method.getAnnotationsByType(Workflow.class);
            checkForBadSignatures(method);
            localContext.registerNew(new MethodId(method).toString(), workFlowAnnotations[0].version(), workFlowAnnotations[0].description());
            registerEventsForArguments(invocation.getArguments());
            invocation.proceed();
            connector.submitNewWorkflow(localContext.getStateMachineDef());
            return null ; // TODO, return a proxy object
        }
        finally {
            this.localContext.reset();
        }
    }

    private void registerEventsForArguments(Object[] arguments) {
        if (arguments.length == 0) {
            return;
        }
        EventData[] eventDatas = new EventData[arguments.length];
        for (int i = 0; i < arguments.length ; i++) {
            final Object anArgument = arguments[i];
            Event anArgumentAsEvent = (Event) anArgument; // This is safe since we have already checked for bad signatures
            final String eventName = localContext.generateEventName(anArgumentAsEvent);
            eventDatas[i] = new EventData(eventName, anArgument.getClass().getName(), anArgument,CLIENT);
        }
        localContext.addEvents(eventDatas);
    }

    private void checkForBadSignatures(Method method) {
        final Class<?> returnType = method.getReturnType();
        if (!returnType.equals(void.class)) {
            throw new IllegalSignatureException(new MethodId(method),"A workflow method can only return void");
        }
    }

}
