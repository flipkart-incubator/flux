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
import java.util.Arrays;

/**
 * Used to uniquely Identify a task's method.
 * There are times when we need to work out individual components of a method name
 * This class provides for easy access to them
 * @author yogesh.nachnani
 */
public class MethodId {
    public static final String UNDERSCORE = "_";
    /* Canonical Name of the class containing the method */
    private String className;
    /* Name of the method that needs to be executed */
    private String methodName;
    /* Ordered list of parameter types used by the method */
    private Class<?>[] parameterTypes;
    /* Return type of the method */
    private String returnType;

    /* Creates a MethodId representation using a given string representation */
    public static MethodId fromIdentifier(String identifier) throws ClassNotFoundException {
        final String[] splitIdentifier = identifier.split(UNDERSCORE);
        final MethodId generatedMethodId = new MethodId();
        try {
            generatedMethodId.className = splitIdentifier[0];
            generatedMethodId.methodName = splitIdentifier[1];
            generatedMethodId.returnType = splitIdentifier[2];
        } catch (ArrayIndexOutOfBoundsException e) {
            /* We have a case of a malformed String identifier */
            throw  new MalformedIdentifierException("Malformed identifier : " + identifier);
        }
        if (splitIdentifier.length > 3 ) {
            generatedMethodId.parameterTypes = new Class<?>[splitIdentifier.length - 3];
            for (int i = 3; i < splitIdentifier.length ; i++) {
                generatedMethodId.parameterTypes[splitIdentifier.length-i-1] = Class.forName(splitIdentifier[i]);
            }
        }
        return generatedMethodId;
    }

    private MethodId() {
    }

    public MethodId(Method method) {
        this.className = method.getDeclaringClass().getCanonicalName();
        this.methodName = method.getName();
        this.parameterTypes = method.getParameterTypes();
        this.returnType = method.getReturnType().getCanonicalName();
    }

    public String getPrefix() {
        return className + UNDERSCORE + methodName;
    }

    /* Accessor methods */
    public String getClassName() {
        return className;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MethodId methodId = (MethodId) o;

        if (className != null ? !className.equals(methodId.className) : methodId.className != null) return false;
        if (methodName != null ? !methodName.equals(methodId.methodName) : methodId.methodName != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(parameterTypes, methodId.parameterTypes)) return false;
        return !(returnType != null ? !returnType.equals(methodId.returnType) : methodId.returnType != null);

    }

    @Override
    public int hashCode() {
        int result = className != null ? className.hashCode() : 0;
        result = 31 * result + (methodName != null ? methodName.hashCode() : 0);
        result = 31 * result + (parameterTypes != null ? Arrays.hashCode(parameterTypes) : 0);
        result = 31 * result + (returnType != null ? returnType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getPrefix());
        stringBuilder.append(UNDERSCORE);
        stringBuilder.append(this.returnType);
        for (Class<?> parameterType : parameterTypes) {
            stringBuilder.append(UNDERSCORE);
            stringBuilder.append(parameterType.getName());
        }
        return stringBuilder.toString();
    }

    public String getMethodName() {
        return this.methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }
}
