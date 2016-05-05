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

package com.flipkart.flux.config;

import com.flipkart.flux.domain.FluxError;

/**
 * The <code>ConfigurationException</code> is sub-type of the {@link FluxError} for use in the configuration modules  
 * 
 * @author regunath.balasubramanian
 */
public class ConfigurationException extends FluxError {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor for ConfigurationException.
	 * @param msg the detail message
	 */
	public ConfigurationException(String msg) {
		super(FluxError.ErrorType.runtime, msg, null);
	}

	/**
	 * Constructor for ConfigurationException.
	 * @param msg the detail message
	 * @param cause the root cause 
	 */
	public ConfigurationException(String msg, Throwable cause) {
		super(FluxError.ErrorType.runtime, msg, cause);
	}
	
}