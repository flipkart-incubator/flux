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

package com.flipkart.flux.client.runner;

import com.flipkart.flux.client.FluxClientSpyModuleForTests;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import java.io.IOException;

// TODO: Enhance and add to polyguice

/**
 * Provides guice injection capabilities to a test class instance
 *
 * @author yogesh.nachnani
 */
public class GuiceJunit4Runner extends BlockJUnit4ClassRunner {
    private static final Injector injector = Guice.createInjector(Stage.PRODUCTION, new FluxClientSpyModuleForTests());;

    /**
     * Creates a BlockJUnit4ClassRunner to run {@code klass}
     *
     * @param klass
     * @throws InitializationError if the test class is malformed.
     */
    public GuiceJunit4Runner(Class<?> klass) throws InitializationError, IOException {
        super(klass);
    }

    @Override
    protected Object createTest() throws Exception {
        final Object testInstance = super.createTest();
        injector.injectMembers(testInstance);
        return testInstance;
    }
}
