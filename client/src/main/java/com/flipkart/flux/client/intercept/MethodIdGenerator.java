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

import java.lang.reflect.Method;

/**
 * Generates method identifiers for intercepted methods
 * @author yogesh.nachnani
 */
public class MethodIdGenerator {
    public static final String UNDERSCORE = "_";

    public static String createMethodIdentifier(Method method) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(createMethodIdPrefix(method));
        stringBuilder.append(UNDERSCORE);
        stringBuilder.append(method.getReturnType().getCanonicalName());
        final Class<?>[] parameterTypes = method.getParameterTypes();
        for (Class<?> parameterType : parameterTypes) {
            stringBuilder.append(UNDERSCORE);
            stringBuilder.append(parameterType.getCanonicalName());
        }
        return stringBuilder.toString();
    }

    public static String createMethodIdPrefix(Method method) {
        return method.getDeclaringClass().getCanonicalName() + UNDERSCORE + method.getName();
    }
}
