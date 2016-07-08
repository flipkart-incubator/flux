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

package com.flipkart.flux.client.model;

import java.lang.annotation.*;

/**
 * This can be used to mark a String field within an <code>Event</code> passed during a workflow invocation.
 * The data of the same field can then be used to post <code>ExternalEvent(s)</code> to the workflow instance at a later
 * stage in its lifecycle.
 * Note1: Ideally, the presence of <code>CorrelationId</code> on two or more fields across a workflow invocation params(i.e
 * either within the same object or across object) should be a failure. However, currently this detection is not implemented
 * yet & the behaviour is undefined
 * Note2: The <code>CorrelationId</code> annotation needs to be present in the actual class of the object passed as workflow invocation parameter
 * and not in any of its superclasse(s).
 * Although it is possible to search the entire class hierarchy for the presence of this annotation, it makes the code
 * harder to reason about and error prone.
 *
 * @author yogesh.nachnani
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CorrelationId {

}
