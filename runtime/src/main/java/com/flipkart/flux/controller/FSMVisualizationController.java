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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

import static com.flipkart.flux.constant.RuntimeConstants.FSM_VIEW;

/**
 * <code>FSMVisualizationController</code> is a Spring MVC Controller for FSM visualization
 * 
 * @author regunath.balasubramanian
 * @author shyam.akirala
 */
@Controller
public class FSMVisualizationController {

    private static final Logger logger = LoggerFactory.getLogger(FSMVisualizationController.class);

    @RequestMapping(value = {"/fsmview"}, method = RequestMethod.GET)
    public String fsmview(HttpServletRequest request) {
        return FSM_VIEW;
    }

}
