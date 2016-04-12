/*
 * Copyright 2012-2016, the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flipkart.flux.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.flipkart.flux.guice.module.ContainerModule;
import com.flipkart.flux.guice.module.FluxServletModule;
import com.google.inject.Singleton;

import javafx.util.Pair;

/**
 * <code>DispatcherServlet</code> is a {@link HttpServlet} that dispatches all API requests to appropriate Flux managed Finite State Machine instances.
 * 
 * For e.g. a POST request containing OrderData payload to a request URI like /api/sfms/SimpleOrderFulfilmentWorkflow/start will trigger the 
 * workflow instance by invoking SimpleOrderFulfilmentWorkflow#start().
 * 
 * @author regunath.balasubramanian
 *
 */
@Singleton
public class DispatcherServlet extends HttpServlet {
	
	/** Default servial version UID*/
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Pair<String,String> entityActionPair = this.parseEntityAction(request);
		// TODO : Dispatch to Flux Akka sub-system
	}
	
	/**
	 * 
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Pair<String,String> entityActionPair = this.parseEntityAction(request);
		// TODO : Dispatch to Flux Akka sub-system
	}
	
	/** 
	 * Helper method to extract the Entity(e.g. "SimpleOrderFulfilmentWorkflow") and Action (e.g "start") parts of the Request URI
	 */
	private Pair<String,String> parseEntityAction(HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		String leadingPrefix = ContainerModule.API_CONTEXT_PATH + FluxServletModule.FSM_SERVLET_PATH + "/";
		String entityAction = requestURI.substring(leadingPrefix.length());
		// remaining part has just the entity name and action identifiers separated by "/"
		String entity = entityAction.substring(0, entityAction.indexOf("/"));
		String action = entityAction.substring(entityAction.indexOf(entity + "/"),entityAction.length());
		if (action.endsWith("/")) {
			action = action.replace("/", "");
		}
		Pair<String,String> entityActionPair = new Pair<String, String>(entity, action);
		return entityActionPair;
	}
	
}
