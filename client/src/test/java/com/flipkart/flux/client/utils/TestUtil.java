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

package com.flipkart.flux.client.utils;

import com.flipkart.flux.client.intercept.SimpleWorkflowForTest;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

/**
 * Dumping ground for all big & small utility/convenience methods used in tests.
 * Anyone writing new tests should ideally refer to this class and find the required convenience methods
 * @author yogesh.nachnani
 */
public class TestUtil {
    public static MethodInvocation dummyInvocation(Method methodToReturn){
        return new MethodInvocation(){
            @Override
            public Object proceed() throws Throwable {
                return null;
            }

            @Override
            public Object getThis() {
                return null;
            }

            @Override
            public AccessibleObject getStaticPart() {
                return null;
            }

            @Override
            public Object[] getArguments() {
                return new Object[0];
            }

            @Override
            public Method getMethod() {
                return methodToReturn;
            }
        };

    }

    public static MethodInvocation dummyInvocation(Method methodToReturn, Object methodOwner) {
        return new MethodInvocation() {
            @Override
            public Method getMethod() {
                return methodToReturn;
            }

            @Override
            public Object[] getArguments() {
                return new Object[0];
            }

            @Override
            public Object proceed() throws Throwable {
                return null;
            }

            @Override
            public Object getThis() {
                return methodOwner;
            }

            @Override
            public AccessibleObject getStaticPart() {
                return null;
            }
        };
    }
}
