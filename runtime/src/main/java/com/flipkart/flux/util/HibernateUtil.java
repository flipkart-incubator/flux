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

package com.flipkart.flux.util;

import com.flipkart.flux.domain.*;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * @author shyam.akirala
 */
public class HibernateUtil {
    private static final SessionFactory sessionFactory;

    static {
        try {
            Configuration  configuration = new Configuration().configure("hibernate.cfg.xml");
            addAnnotatedClasses(configuration);
            configuration.setPhysicalNamingStrategy(PhysicalNamingStrategyImpl.INSTANCE);
            sessionFactory = configuration.buildSessionFactory();
        } catch (Throwable ex) {
            System.out.println("Error occurred during session factory init");
            throw new ExceptionInInitializerError(ex);
        }
    }

    private static void addAnnotatedClasses(Configuration configuration) {
        configuration.addAnnotatedClass(AuditRecord.class);
        configuration.addAnnotatedClass(Checkpoint.class);
        configuration.addAnnotatedClass(Event.class);
        configuration.addAnnotatedClass(State.class);
        configuration.addAnnotatedClass(StateMachine.class);
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
