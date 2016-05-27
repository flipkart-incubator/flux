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
package com.flipkart.flux.controller;

import static com.flipkart.flux.constant.RuntimeConstants.FSM_VIEW;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * <code>FSMVisualizationController</code> is a Spring MVC Controller for FSM visualization
 * 
 * @author regunath.balasubramanian
 */
@Controller
public class FSMVisualizationController {

    /**
     * FSM visualization page
     */
    @RequestMapping(value = {"/fsmview"}, method = RequestMethod.GET)
    public String getFSMViewData(ModelMap model, HttpServletRequest request) {
    	// TODO : Get this data from the Flux API
    	model.addAttribute("adjacencyList", "{" +
				"'Order created': ['Payment Received:Payment Pending', 'Order Packed:Order Confirmed']," +
				"'Payment Received': ['Order Packed:Order Confirmed', 'Order Delivered:Ready For Delivery'],"+
				"'Order Packed': ['Order Shipped:Package Ready'],"+
				"'Order Shipped': ['Payment Received:Payment Pending','Order Delivered:Ready For Delivery'],"+
				"'Order Delivered': []"+
    			"}");
    	return FSM_VIEW;

    }
    
}
