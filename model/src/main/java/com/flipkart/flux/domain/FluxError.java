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
 */

package com.flipkart.flux.domain;

/**
 * <code>FluxError</code> defines an error in the Flux runtime.
 * 
 * @author Yogesh
 * @author regunath.balasubramanian
 * @author shyam.akirala
 * 
 */
public class FluxError extends RuntimeException {
	
	/** Default serial version UID */
	private static final long serialVersionUID = 1L;
	
	/** The type of error*/
    private ErrorType type;
    
    /** Enum of errro types*/
    public enum ErrorType {
        runtime,timeout
    }
    
    /** Constructor */
	public FluxError(ErrorType type, String errorMessage, Throwable rootCause) {
		super(errorMessage, rootCause);
		this.type = type;
	}

	/** Accessor/Mutator methods*/
	public ErrorType getType() {
		return type;
	}
	public void setType(ErrorType type) {
		this.type = type;
	}
    
}
