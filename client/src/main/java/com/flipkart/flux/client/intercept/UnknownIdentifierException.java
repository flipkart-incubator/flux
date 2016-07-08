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

/**
 * Used to denote runtime execution exceptions where a method of a given identifier cannot be instantiated
 * This can happen in race conditions where freshly loaded code in another JVM triggers a workflow and the given
 * classes are yet to be loaded on to this particular JVM.
 * // TODO: Is there a way we can avoid this?
 *
 * @author yogesh.nachnani
 */
public class UnknownIdentifierException extends RuntimeException {
    public UnknownIdentifierException(String message) {
        super(message);
    }
}
