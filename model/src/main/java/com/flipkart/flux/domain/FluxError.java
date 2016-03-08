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
 * 
 */
public class FluxError extends RuntimeException {
	
	/** The type of error*/
    private ErrorType type;
    /** The error message*/
    private String errorMessage;
    /** The root cause Throwable, if any*/
    private Throwable rootCause;
    /** Enum of errro types*/
    public enum ErrorType {
        runtime,timeout
    }
    
    /** Constructor */
	public FluxError(ErrorType type, String errorMessage, Throwable rootCause) {
		super();
		this.type = type;
		this.errorMessage = errorMessage;
		this.rootCause = rootCause;
	}

	/** Accessor/Mutator methods*/
	public ErrorType getType() {
		return type;
	}
	public void setType(ErrorType type) {
		this.type = type;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public Throwable getRootCause() {
		return rootCause;
	}
	public void setRootCause(Throwable rootCause) {
		this.rootCause = rootCause;
	}
    
}
