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

package com.flipkart.flux.guice.provider;

import com.flipkart.flux.domain.AuditRecord;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.type.BlobType;
import com.flipkart.flux.type.StoreFQNType;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.hibernate.cfg.Configuration;
import org.yaml.snakeyaml.Yaml;
import javax.inject.Provider;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

/**
 * Provides Hibernate configuration to build {@link org.hibernate.SessionFactory}
 * @author shyam.akirala
 */
public class ConfigurationProvider implements Provider<Configuration> {

    @Override
    public Configuration get() {

        Configuration configuration = new Configuration();
        addAnnotatedClassesAndTypes(configuration);
        configuration.setImplicitNamingStrategy(ImplicitNamingStrategyJpaCompliantImpl.INSTANCE);

        try {
            Properties properties = getHibernateProperties();
            configuration.setProperties(properties);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read Hibernate properties. Exception: "+e.getMessage());
        }

        return configuration;
    }

    /**
     * Adds annotated classes and custom types to passed configuration
     * @param configuration
     */
    private void addAnnotatedClassesAndTypes(Configuration configuration) {
        //register hibernate custom types
        configuration.registerTypeOverride(new BlobType(), new String[]{"BlobType"});
        configuration.registerTypeOverride(new StoreFQNType(), new String[]{"StoreFQNOnly"});

        //add annotated classes to configuration
        configuration.addAnnotatedClass(AuditRecord.class);
        configuration.addAnnotatedClass(Event.class);
        configuration.addAnnotatedClass(State.class);
        configuration.addAnnotatedClass(StateMachine.class);
    }

    /**
     * Returns Hibernate properties from configuration.yml file
     * @return Hibernate properties
     * @throws IOException
     */
    private Properties getHibernateProperties() throws IOException {
        ClassLoader loader = this.getClass().getClassLoader();
        URL url = loader.getResource("packaged/configuration.yml");
        Yaml yaml = new Yaml();
        InputStreamReader reader = new InputStreamReader(url.openStream());
        Map<String, Object> map = (Map<String, Object>) ((Map) yaml.load(reader)).get("Hibernate");

        Properties properties = new Properties();
        for(Map.Entry<String, Object> entry : map.entrySet()) {
            properties.put(entry.getKey(), entry.getValue());
        }

        return properties;
    }

}
