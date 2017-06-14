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

package com.flipkart.flux.persistence.impl;

import com.flipkart.flux.persistence.DataSourceType;
import com.flipkart.flux.persistence.ShardedSessionFactoryContext;
import com.google.common.collect.ImmutableMap;
import org.hibernate.SessionFactory;
import org.hibernate.shards.session.ShardedSessionFactory;

import java.util.Map;

/**
 * A {@link com.flipkart.flux.persistence.ShardedSessionFactoryContext} implementation that maintains a map of {@link DataSourceType} to {@link SessionFactory},
 * and uses a thread local to save the SessionFactory that is being used in an ongoing transaction.
 *
 * Created by gaurav.ashok on 23/11/16.
 */
public class ShardedSessionFactoryContextImpl  implements ShardedSessionFactoryContext {

    private final ImmutableMap<DataSourceType, ShardedSessionFactory> sessionFactoryImmutableMap;

    private final DataSourceType defaultDataSourceType;

    private final ThreadLocal<SessionFactory> currentSessionFactory = new ThreadLocal<>();

    public ShardedSessionFactoryContextImpl(Map<DataSourceType, ShardedSessionFactory> sessionFactoryMap, DataSourceType defaultType) {
        this.sessionFactoryImmutableMap = ImmutableMap.copyOf(sessionFactoryMap);
        this.defaultDataSourceType = defaultType;

        assert sessionFactoryImmutableMap.get(defaultDataSourceType) != null :
                "DataSource of type " + defaultDataSourceType.name() + " not configured";
    }

    public ShardedSessionFactoryContextImpl(Map<DataSourceType, ShardedSessionFactory> sessionFactoryMap) {
        this(sessionFactoryMap, DataSourceType.READ_WRITE);
    }

    @Override
    public void setSessionFactory(SessionFactory sessionFactory){
        currentSessionFactory.set(sessionFactory);
    }

    @Override
    public SessionFactory getCurrentSessionFactory() {
        return currentSessionFactory.get();
    }

    @Override
    public ShardedSessionFactory getShardedSessionFactory(DataSourceType type) {
        return sessionFactoryImmutableMap.get(type);
    }

    @Override
    public ShardedSessionFactory getDefaultShardedSessionFactory() {
        return sessionFactoryImmutableMap.get(defaultDataSourceType);
    }

    @Override
    public void useShardedSessionFactory(DataSourceType type) {
        ShardedSessionFactory sessionFactory = sessionFactoryImmutableMap.get(type);
        if(sessionFactory == null) {
            sessionFactory = sessionFactoryImmutableMap.get(defaultDataSourceType);
        }
    }

    @Override
    public void useDefault() {
        useShardedSessionFactory(defaultDataSourceType);
    }

    @Override
    public void clear() {
        currentSessionFactory.remove();
    }
}
