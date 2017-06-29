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
import com.flipkart.flux.persistence.SessionFactoryContext;
import com.flipkart.flux.shard.ShardId;
import com.google.common.collect.ImmutableMap;
import org.hibernate.SessionFactory;

import java.util.Map;

/**
 * A {@link com.flipkart.flux.persistence.SessionFactoryContext} implementation that maintains a map of {@link DataSourceType} to {@link SessionFactory},
 * and uses a thread local to save the SessionFactory that is being used in an ongoing transaction.
 * <p>
 * Created by gaurav.ashok on 23/11/16.
 */
public class SessionFactoryContextImpl implements SessionFactoryContext {

    private final ImmutableMap<ShardId, SessionFactory> RWSessionFactoryImmutableMap;
    private final ImmutableMap<ShardId, SessionFactory> ROSessionFactoryImmutableMap;
    private final ImmutableMap<Character, ShardId> shardKeyToRWShardIdImmutableMap;
    private final ImmutableMap<Character, ShardId> shardKeyToROShardIdImmutableMap;
    private final SessionFactory redriverSessionFactory;


    private final ThreadLocal<SessionFactory> currentSessionFactoryContext = new ThreadLocal<>();

    public SessionFactoryContextImpl(Map<ShardId, SessionFactory> rwSessionFactoryMap, Map<ShardId, SessionFactory> roSessionFactoryMap,
                                     Map<Character, ShardId> shardKeyToRWShardIdMap, Map<Character, ShardId> shardKeyToROShardIdMap,
                                     SessionFactory redriverSessionFactory) {
        this.RWSessionFactoryImmutableMap = ImmutableMap.copyOf(rwSessionFactoryMap);
        this.ROSessionFactoryImmutableMap = ImmutableMap.copyOf(roSessionFactoryMap);
        this.shardKeyToRWShardIdImmutableMap = ImmutableMap.copyOf(shardKeyToRWShardIdMap);
        this.shardKeyToROShardIdImmutableMap = ImmutableMap.copyOf(shardKeyToROShardIdMap);
        this.redriverSessionFactory = redriverSessionFactory;
    }


    @Override
    public void setSessionFactory(SessionFactory sessionFactory) {
        currentSessionFactoryContext.set(sessionFactory);
    }

    @Override
    public SessionFactory getCurrentSessionFactory() {
        return currentSessionFactoryContext.get();
    }

    @Override
    public SessionFactory getRedriverSessionFactory() {
        return redriverSessionFactory;
    }

    @Override
    public SessionFactory getRWSessionFactory(ShardId shardId) {
        return RWSessionFactoryImmutableMap.get(shardId);
    }

    @Override
    public SessionFactory getROSessionFactory(ShardId shardId) {
        return ROSessionFactoryImmutableMap.get(shardId);
    }

    @Override
    public SessionFactory getDefaultSessionFactoryMap(ShardId shardId) {
        return RWSessionFactoryImmutableMap.get(shardId);
    }

    public ShardId getRWShardIdForShardKey(Character shardKey) {
        return shardKeyToRWShardIdImmutableMap.get(shardKey);
    }

    public ShardId getROShardIdForShardKey(Character shardKey) {
        return shardKeyToROShardIdImmutableMap.get(shardKey);
    }

    @Override
    public void clear() {
        currentSessionFactoryContext.remove();
    }
}
