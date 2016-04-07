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

package com.flipkart.flux.guice.providers;

import com.flipkart.flux.domain.AuditRecord;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.type.BlobType;
import com.flipkart.flux.type.StoreFQNType;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.hibernate.cfg.Configuration;
import javax.inject.Provider;

/**
 * Provides Hibernate configuration to build {@link org.hibernate.SessionFactory}
 * @author shyam.akirala
 */
public class ConfigurationProvider implements Provider<Configuration> {

    @Override
    public Configuration get() {
        Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
        addAnnotatedClassesAndTypes(configuration);
        configuration.setImplicitNamingStrategy(ImplicitNamingStrategyJpaCompliantImpl.INSTANCE);
        return configuration;
    }

    private static void addAnnotatedClassesAndTypes(Configuration configuration) {
        //register hibernate custom types
        configuration.registerTypeOverride(new BlobType(), new String[]{"BlobType"});
        configuration.registerTypeOverride(new StoreFQNType(), new String[]{"StoreFQNOnly"});

        //add annotated classes to configuration
        configuration.addAnnotatedClass(AuditRecord.class);
        configuration.addAnnotatedClass(Event.class);
        configuration.addAnnotatedClass(State.class);
        configuration.addAnnotatedClass(StateMachine.class);
    }

}
