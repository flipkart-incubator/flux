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

package com.flipkart.flux.shard.module;

import com.fasterxml.classmate.AnnotationConfiguration;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.domain.*;
import com.flipkart.flux.guice.module.HibernateModule;
import com.flipkart.flux.resource.StateMachineResource;
import org.hibernate.shards.ShardId;
import org.hibernate.shards.ShardedConfiguration;
import org.hibernate.shards.cfg.ConfigurationToShardConfigurationAdapter;
import org.hibernate.shards.cfg.ShardConfiguration;
import org.hibernate.shards.session.OpenSessionEvent;
import com.flipkart.flux.dao.AuditDAOImpl;
import com.flipkart.flux.dao.EventsDAOImpl;
import com.flipkart.flux.dao.StateMachinesDAOImpl;
import com.flipkart.flux.dao.StatesDAOImpl;
import com.flipkart.flux.dao.iface.AuditDAO;
import com.flipkart.flux.dao.iface.EventsDAO;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.dao.iface.StatesDAO;
import com.flipkart.flux.guice.interceptor.TransactionInterceptor;
import com.flipkart.flux.persistence.DataSourceType;
import com.flipkart.flux.persistence.SessionFactoryContext;
import com.flipkart.flux.persistence.impl.SessionFactoryContextImpl;
import com.flipkart.flux.redriver.dao.MessageDao;
import com.flipkart.flux.type.BlobType;
import com.flipkart.flux.type.ListJsonType;
import com.flipkart.flux.type.StoreFQNType;
import com.flipkart.polyguice.config.YamlConfiguration;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.shards.session.ShardedSessionFactory;
import org.hibernate.shards.strategy.ShardStrategy;
import org.hibernate.shards.strategy.ShardStrategyFactory;
import org.hibernate.shards.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.ConditionalGenericConverter;

import javax.inject.Provider;
import javax.sql.DataSource;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.*;

/**
 * <code>HibernateModule</code> is a Guice {@link AbstractModule} implementation used for wiring SessionFactory, DAO and Interceptor classes.
 *
 * @author amitkumar.o
 */
public class HibernateShardModule extends AbstractModule {
    public static final String FLUX_HIBERNATE_CONFIG_NAME_SPACE = "flux.Hibernate";
    public static final String FLUX_READ_ONLY_HIBERNATE_CONFIG_NAME_SPACE = "fluxReadOnly.Hibernate";

    /**
     * Performs concrete bindings for interfaces
     *
     * @see com.google.inject.AbstractModule#configure()
     */
    @Override
    protected void configure() {
        //bind entity classes
        bind(AuditDAO.class).to(AuditDAOImpl.class).in(Singleton.class);
        bind(EventsDAO.class).to(EventsDAOImpl.class).in(Singleton.class);
        bind(StateMachinesDAO.class).to(StateMachinesDAOImpl.class).in(Singleton.class);
        bind(StatesDAO.class).to(StatesDAOImpl.class).in(Singleton.class);

        //bind Transactional Interceptor to intercept methods which are annotated with javax.transaction.Transactional
        Provider<SessionFactoryContext> provider = getProvider(Key.get(SessionFactoryContext.class, Names.named("fluxSessionFactoryContext")));
        final TransactionInterceptor transactionInterceptor = new TransactionInterceptor(provider);
        // Weird way of getting a package but java.lang.Package.getName(<String>) was no working for some reason.
        // todo [yogesh] dig deeper and fix this ^
        bindInterceptor(Matchers.not(Matchers.inPackage(MessageDao.class.getPackage())),
                Matchers.annotatedWith(Transactional.class), transactionInterceptor);
    }

    /**
     * Creates hibernate configuration from the configuration yaml properties.
     * Since the yaml properties are already flattened in input param <code>yamlConfiguration</code>
     * the method loops over them to selectively pick Hibernate specific properties.
     */
    public ShardConfiguration getConfiguration(YamlConfiguration yamlConfiguration, String host) {
        return new ConfigurationToShardConfigurationAdapter(getConfiguration(yamlConfiguration, FLUX_HIBERNATE_CONFIG_NAME_SPACE, host));
    }

    public ShardConfiguration getReadOnlyConfiguration(YamlConfiguration yamlConfiguration, String host) {
        return new ConfigurationToShardConfigurationAdapter(getConfiguration(yamlConfiguration, FLUX_READ_ONLY_HIBERNATE_CONFIG_NAME_SPACE, host));
    }

    @Provides
    @Singleton
    @Named("fluxHibernateShardConfigurations")
    public List getHibernateShardConfigurations(YamlConfiguration yamlConfiguration) {
        List hibernateShardConfigurations = new LinkedList<Map<DataSourceType, ShardConfiguration>>();
        List<ShardHostsModel> shardModelHosts = new LinkedList<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TypeReference<List<ShardHostsModel>> shardHostModelsList = new TypeReference<List<ShardHostsModel>>() {
            };
            shardModelHosts = objectMapper.readValue(yamlConfiguration.subset("flux.Shard.Config").getProperty("master.slave.pair").toString(), shardHostModelsList);
        } catch (IOException io) {
            throw new RuntimeException(io);
        }
        shardModelHosts.stream().forEach(shardHostsModel -> {
            System.out.println(shardHostsModel.toString());
            ShardConfiguration masterHibernateConfig = getConfiguration(yamlConfiguration, shardHostsModel.getMaster());
            ShardConfiguration slaveHibernateConfig = getReadOnlyConfiguration(yamlConfiguration, shardHostsModel.getSlave());
            Map hibernateShardConfig = new HashMap<DataSourceType, Configuration>();
            hibernateShardConfig.put(DataSourceType.READ_WRITE, masterHibernateConfig);
            hibernateShardConfig.put(DataSourceType.READ_ONLY, slaveHibernateConfig);
            hibernateShardConfigurations.add(hibernateShardConfig);
        });
        return hibernateShardConfigurations;
    }

    @Provides
    @Singleton
    @Named("fluxReadWriteSharedSessionFactory")
    public ShardedSessionFactory getReadWriteShardedSessionFactory(@Named("fluxHibernateShardConfigurations") List<Map<DataSourceType,
            ShardConfiguration>> shardConfigs, YamlConfiguration yamlConfiguration) {
        Configuration protoTypeConfiguration = getConfiguration(yamlConfiguration, FLUX_HIBERNATE_CONFIG_NAME_SPACE, "localhost");
        addAnnotatedClassesAndTypes(protoTypeConfiguration);
        List<ShardConfiguration> shardConfigurations = new LinkedList<ShardConfiguration>();
        shardConfigs.stream().forEach( Map -> {
            shardConfigurations.add(Map.get(DataSourceType.READ_WRITE));
        });
        ShardStrategyFactory shardStrategyFactory = buildShardStrategyFactory();
        ShardedConfiguration shardedConfiguration = new ShardedConfiguration(protoTypeConfiguration, shardConfigurations, shardStrategyFactory);
        return shardedConfiguration.buildShardedSessionFactory();
    }

    @Provides
    @Singleton
    @Named("fluxReadOnlySharedSessionFactory")
    public ShardedSessionFactory getReadOnlyShardedSessionFactory(@Named("fluxHibernateShardConfigurations") List<Map<DataSourceType,
            ShardConfiguration>> shardConfigs, YamlConfiguration yamlConfiguration) {
        Configuration protoTypeConfiguration = getConfiguration(yamlConfiguration, FLUX_HIBERNATE_CONFIG_NAME_SPACE, "localhost");
        addAnnotatedClassesAndTypes(protoTypeConfiguration);
        List<ShardConfiguration> shardConfigurations = new LinkedList<ShardConfiguration>();
        shardConfigs.stream().forEach( Map -> {
            shardConfigurations.add(Map.get(DataSourceType.READ_ONLY));
        });
        ShardStrategyFactory shardStrategyFactory = buildShardStrategyFactory();
        ShardedConfiguration shardedConfiguration = new ShardedConfiguration(protoTypeConfiguration, shardConfigurations, shardStrategyFactory);
        return shardedConfiguration.buildShardedSessionFactory();
    }


    ShardStrategyFactory buildShardStrategyFactory(){
        ShardStrategyFactory shardStrategyFactory = new ShardStrategyFactory() {
            @Override
            public ShardStrategy newShardStrategy(List<ShardId> list) {

            }
        }
    }
    


    @Provides
    @Singleton
    @Named("fluxSessionFactoryContext")
    public SessionFactoryContext getSessionFactoryProvider(@Named("fluxHibernateConfiguration") Configuration configuration,
                                                           @Named("fluxReadOnlyHibernateConfiguration") Configuration readOnlyConfiguration) {
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        SessionFactory readOnlysessionFactory = readOnlyConfiguration.buildSessionFactory();
        Map<DataSourceType, SessionFactory> map = new HashMap<>();
        map.put(DataSourceType.READ_WRITE, sessionFactory);
        map.put(DataSourceType.READ_ONLY, readOnlysessionFactory);
        return new SessionFactoryContextImpl(map, DataSourceType.READ_WRITE);
    }

    /**
     * Adds annotated classes and custom types to passed Hibernate configuration.
     */
    private void addAnnotatedClassesAndTypes(Configuration configuration) {
        //register hibernate custom types
        configuration.registerTypeOverride(new BlobType(), new String[]{"BlobType"});
        configuration.registerTypeOverride(new StoreFQNType(), new String[]{"StoreFQNOnly"});
        configuration.registerTypeOverride(new ListJsonType(), new String[]{"ListJsonType"});

        //add annotated classes to configuration
        configuration.addAnnotatedClass(AuditRecord.class);
        configuration.addAnnotatedClass(Event.class);
        configuration.addAnnotatedClass(State.class);
        configuration.addAnnotatedClass(StateMachine.class);
    }

    private Configuration getConfiguration(YamlConfiguration yamlConfiguration, String prefix, String host) {
        Configuration configuration = new Configuration();
        org.apache.commons.configuration.Configuration hibernateConfig = yamlConfiguration.subset(prefix);
        Iterator<String> propertyKeys = hibernateConfig.getKeys();
        Properties configProperties = new Properties();
        while (propertyKeys.hasNext()) {
            String propertyKey = propertyKeys.next();
            Object propertyValue = hibernateConfig.getProperty(propertyKey);
            configProperties.put(propertyKey, propertyValue);
        }
        configProperties.setProperty("hibernate.connection.url", "jdbc:mysql://" + host + ":3306/flux_sharding");
        configProperties.setProperty("hibernate.session_factory_name", "hibernate_session_factory_" + host);
        configuration.addProperties(configProperties);
        return configuration;
    }

}