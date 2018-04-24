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

package com.flipkart.flux.annotation;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <code>ManagedEnv</code> annotation used in guice bindings to bind the interfaces to classes which are specific to flux managed environment mode.
 * Ex: bind(ExecutableRegistry.class).annotatedWith(ManagedEnv.class).to(TaskExecutableRegistryImpl.class);
 * ExecutableRegistry needed to be bounded to TaskExecutableRegistryImpl in managed environment, in that kind of cases ManagedEnv annotation is used.
 *
 * @author shyam.akirala
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@BindingAnnotation
public @interface ManagedEnv {
}
