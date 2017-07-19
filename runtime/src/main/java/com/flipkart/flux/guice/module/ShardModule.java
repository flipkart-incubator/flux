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

package com.flipkart.flux.guice.module;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.flux.api.core.FluxError;
import com.flipkart.flux.dao.AuditDAOImpl;
import com.flipkart.flux.dao.EventsDAOImpl;
import com.flipkart.flux.dao.StateMachinesDAOImpl;
import com.flipkart.flux.dao.StatesDAOImpl;
import com.flipkart.flux.dao.iface.AuditDAO;
import com.flipkart.flux.dao.iface.EventsDAO;
import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.dao.iface.StatesDAO;
import com.flipkart.flux.domain.AuditRecord;
import com.flipkart.flux.domain.Event;
import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.guice.interceptor.TransactionInterceptor;
import com.flipkart.flux.persistence.SessionFactoryContext;
import com.flipkart.flux.persistence.impl.SessionFactoryContextImpl;
import com.flipkart.flux.redriver.dao.MessageDao;
import com.flipkart.flux.shard.MasterSlavePairList;
import com.flipkart.flux.shard.ShardId;
import com.flipkart.flux.shard.ShardPairModel;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * <code>ShardModule</code> is a Guice {@link AbstractModule} implementation used for wiring SessionFactory, DAO and Interceptor classes for the shards.
 *
 * @author amitkumar.o
 */
public class ShardModule extends AbstractModule {

    public static final String FLUX_HIBERNATE_SHARD_CONFIG_NAME_SPACE = "flux.Shard.Pair.Config";
    public static final String FLUX_HIBERNATE_CONFIG_NAME_SPACE = "flux.Hibernate";
    public static final String FLUX_READ_ONLY_HIBERNATE_CONFIG_NAME_SPACE = "fluxReadOnly.Hibernate";
    public static final String DB_PREFIX = "fluxShard_";
    private static final Logger logger = LoggerFactory.getLogger(ShardModule.class);

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
        Provider<SessionFactoryContext> provider = getProvider(Key.get(SessionFactoryContext.class, Names.named("fluxSessionFactoriesContext")));
        final TransactionInterceptor transactionInterceptor = new TransactionInterceptor(provider);
        // Weird way of getting a package but java.lang.Package.getName(<String>) was no working for some reason.
        // todo [yogesh] dig deeper and fix this ^
        bindInterceptor(Matchers.not(Matchers.inPackage(MessageDao.class.getPackage())),
                Matchers.annotatedWith(Transactional.class), transactionInterceptor);
    }

    @Provides
    @Singleton
    @Named("fluxMasterSlavePairList")
    public MasterSlavePairList getFluxMasterSlavePairList(YamlConfiguration yamlConfiguration) {
        String json = yamlConfiguration.getProperty(FLUX_HIBERNATE_SHARD_CONFIG_NAME_SPACE).toString();

        ObjectMapper objectMapper = new ObjectMapper();
        MasterSlavePairList masterSlavePairList;
        try {
            masterSlavePairList = objectMapper.readValue(json, MasterSlavePairList.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new FluxError(FluxError.ErrorType.runtime, "Not able to read Master Slave Config from Config File", e.getCause());
        }
        return masterSlavePairList;
    }

    /**
     * Provides a immutable map with Shard id to Shard Host Model mapping.
     *
     * @param masterSlavePairList
     * @return
     */

    @Provides
    @Singleton
    @Named("fluxShardIdToShardPairMap")
    public Map<ShardId, ShardPairModel> getFluxRWShardIdToShardMapping(@Named("fluxMasterSlavePairList") MasterSlavePairList masterSlavePairList) {
        Map shardIdToShardHostModelMap = new HashMap<ShardId, ShardPairModel>();
        masterSlavePairList.getShardPairModelList().forEach(masterSlavePair -> {
            shardIdToShardHostModelMap.put(masterSlavePair.getShardId(), masterSlavePair);
        });
        assert masterSlavePairList.getShardPairModelList().size() == shardIdToShardHostModelMap.size();
        return shardIdToShardHostModelMap;
    }

    /**
     * Provides a  map with Shard Key (Character) to ShardHostModel Mapping for shards only.
     *
     * @param masterSlavePairList
     * @return
     */
    @Provides
    @Singleton
    @Named("fluxShardKeyToShardIdMap")
    public Map<String, ShardId> getFluxRWShardKeyToShardIdMapping(@Named("fluxMasterSlavePairList") MasterSlavePairList masterSlavePairList) {
        Map shardKeyToShardIdMap = new HashMap<String, ShardId>();
        masterSlavePairList.getShardPairModelList().forEach(masterSlavePair -> {
            String startKey = masterSlavePair.getStartKey();
            String endKey = masterSlavePair.getEndKey();
            for (int i = 0; i < 16; i++)
                for (int j = 0; j < 16; j++) {
                    String shardPrefix = Integer.toHexString(i) + Integer.toHexString(j);
                    if (shardPrefix.compareTo(startKey) >= 0 && shardPrefix.compareTo(endKey) <= 0) {
                        shardKeyToShardIdMap.put(shardPrefix, masterSlavePair.getShardId());
                    }
                }
        });
        if (shardKeyToShardIdMap.size() != (1 << 8)) {
            throw new RuntimeException("No. of shardKeys should be 16*16, currently it is " +
                    Integer.toString(shardKeyToShardIdMap.size()));
        }
        return shardKeyToShardIdMap;
    }


    /**
     * Creates hibernate configuration from the configuration yaml properties.
     * Since the yaml properties are already flattened in input param <code>yamlConfiguration</code>
     * the method loops over them to selectively pick Hibernate specific properties.
     */
    public Configuration getRWConfiguration(YamlConfiguration yamlConfiguration, String host, String dbNameSuffix) {
        return getConfiguration(yamlConfiguration, FLUX_HIBERNATE_CONFIG_NAME_SPACE, host, dbNameSuffix);
    }

    public Configuration getROConfiguration(YamlConfiguration yamlConfiguration, String host, String dbNameSuffix) {
        return getConfiguration(yamlConfiguration, FLUX_READ_ONLY_HIBERNATE_CONFIG_NAME_SPACE, host, dbNameSuffix);
    }

    @Provides
    @Singleton
    @Named("fluxROSessionFactoriesMap")
    public Map<String, SessionFactory> getFluxROSessionFactoryMap(@Named("fluxShardKeyToShardIdMap") Map<String, ShardId>
                                                                          fluxShardKeyToShardIdMap,
                                                                  @Named("fluxShardIdToShardPairMap") Map<ShardId, ShardPairModel>
                                                                          fluxShardIdToShardPairMap,
                                                                  YamlConfiguration yamlConfiguration) {
        Map fluxROSessionFactories = new HashMap<String, SessionFactory>();
        fluxShardKeyToShardIdMap.entrySet().forEach(shardKeyToShardIdMapping -> {
            String shardKey = shardKeyToShardIdMapping.getKey();
            ShardId shardId = shardKeyToShardIdMapping.getValue();
            ShardPairModel shardPairModel = fluxShardIdToShardPairMap.get(shardId);
            Configuration conf = getROConfiguration(yamlConfiguration, shardPairModel.getSlaveIp(), shardKey);
            fluxROSessionFactories.put(shardKey, conf.buildSessionFactory());
        });
        if (fluxROSessionFactories.size() != (1 << 8)) {
            throw new RuntimeException("No. of RW Session Factories should be 16*16, currently it is " +
                    Integer.toString(fluxROSessionFactories.size()));
        }
        return fluxROSessionFactories;
    }

    @Provides
    @Singleton
    @Named("fluxRWSessionFactoriesMap")
    public Map<String, SessionFactory> getFluxRWSessionFactoryMap(@Named("fluxShardKeyToShardIdMap") Map<String, ShardId>
                                                                          fluxShardKeyToShardIdMap,
                                                                  @Named("fluxShardIdToShardPairMap") Map<ShardId, ShardPairModel>
                                                                          fluxShardIdToShardPairMap,
                                                                  YamlConfiguration yamlConfiguration) {
        Map fluxRWSessionFactories = new HashMap<String, SessionFactory>();
        fluxShardKeyToShardIdMap.entrySet().forEach(shardKeyToShardIdMapping -> {
            String shardKey = shardKeyToShardIdMapping.getKey();
            ShardId shardId = shardKeyToShardIdMapping.getValue();
            ShardPairModel shardPairModel = fluxShardIdToShardPairMap.get(shardId);
            Configuration conf = getRWConfiguration(yamlConfiguration, shardPairModel.getMasterIp(), shardKey);
            fluxRWSessionFactories.put(shardKey, conf.buildSessionFactory());
        });
        if (fluxRWSessionFactories.size() != (1 << 8)) {
            throw new RuntimeException("No. of RW Session Factories should be 16*16, currently it is " +
                    Integer.toString(fluxRWSessionFactories.size()));
        }
        return fluxRWSessionFactories;
    }

    @Provides
    @Singleton
    @Named("fluxSessionFactoriesContext")
    public SessionFactoryContext getSessionFactoryProvider(@Named("fluxRWSessionFactoriesMap") Map<String, SessionFactory> fluxRWSessionFactoriesMap,
                                                           @Named("fluxROSessionFactoriesMap") Map<String, SessionFactory> fluxROSessionFactoriesMap,
                                                           @Named("schedulerSessionFactory") SessionFactory schedulerSessionFactory
    ) {

        return new SessionFactoryContextImpl(fluxRWSessionFactoriesMap, fluxROSessionFactoriesMap, schedulerSessionFactory);
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

    private Configuration getConfiguration(YamlConfiguration yamlConfiguration, String prefix, String host, String dbNameSuffix) {
        Configuration configuration = new Configuration();
        addAnnotatedClassesAndTypes(configuration);
        org.apache.commons.configuration.Configuration hibernateConfig = yamlConfiguration.subset(prefix);
        Iterator<String> propertyKeys = hibernateConfig.getKeys();
        Properties configProperties = new Properties();
        while (propertyKeys.hasNext()) {
            String propertyKey = propertyKeys.next();
            Object propertyValue = hibernateConfig.getProperty(propertyKey);
            configProperties.put(propertyKey, propertyValue);
        }
        configProperties.setProperty("hibernate.connection.url", "jdbc:mysql://" + host + ":3306/" + DB_PREFIX + dbNameSuffix);
        configuration.addProperties(configProperties);
        return configuration;
    }
}