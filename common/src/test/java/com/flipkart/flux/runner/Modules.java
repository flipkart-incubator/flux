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

package com.flipkart.flux.runner;

import com.flipkart.polyguice.core.support.Polyguice;
import com.google.inject.AbstractModule;

import java.lang.annotation.*;

/**
 * Used to specify the {@link AbstractModule}s to be used while creating the {@link Polyguice} container to be used
 * to inject dependencies in the given test
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Modules {
    Class<? extends AbstractModule>[] orchestrationModules() default {};
    Class<? extends AbstractModule>[] executionModules() default {};
}
