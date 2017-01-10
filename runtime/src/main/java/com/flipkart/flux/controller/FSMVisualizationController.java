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

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static com.flipkart.flux.constant.RuntimeConstants.FSM_VIEW;

/**
 * <code>FSMVisualizationController</code> is a Spring MVC Controller for FSM visualization
 * 
 * @author regunath.balasubramanian
 * @author shyam.akirala
 */
@Controller
public class FSMVisualizationController {

    /* Guice injector which provides flux configuration using com.flipkart.flux.guice.module.ConfigModule */
    private Injector configInjector;

    /* Constructor*/
    public FSMVisualizationController(Injector configInjector) {
        this.configInjector = configInjector;
    }

    @RequestMapping(value = {"/fsmview"}, method = RequestMethod.GET)
    public String fsmview(@RequestParam(value = "fsmid", required = false) String fsmId, ModelMap modelMap, HttpServletRequest request) throws UnknownHostException {
        int fluxApiPort = configInjector.getInstance(Key.get(Integer.class, Names.named("Api.service.port")));
        String fluxApiUrl = "http://" + InetAddress.getLocalHost().getHostAddress() + ":" + fluxApiPort;
        modelMap.addAttribute("flux_api_url", fluxApiUrl);
        modelMap.addAttribute("fsm_id", (fsmId != null ? fsmId : "null"));
        return FSM_VIEW;
    }

}
