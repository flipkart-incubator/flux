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

import com.flipkart.flux.persistence.SessionFactoryContext;
import com.flipkart.flux.shard.ShardId;
import com.google.common.collect.ImmutableMap;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.Map;

/**
 * A {@link com.flipkart.flux.persistence.SessionFactoryContext} implementation that maintains a map of each  {@link ShardId} to {@link SessionFactory}
 * for Master(Read-Write) and Slave(Read-Only) Shards, {@link SessionFactory} schedulerSessionFactory as well as shardString to ShardId Mapping for both Slave,Master
 * and uses a thread local to save the Session that is being used in an ongoing transaction.
 * <p>
 * @author amitkumar.o
 * @author gourav.ashok
 */
public class SessionFactoryContextImpl implements SessionFactoryContext {

    private final ImmutableMap<ShardId, SessionFactory> RWSessionFactoryImmutableMap;
    private final ImmutableMap<ShardId, SessionFactory> ROSessionFactoryImmutableMap;
    private final ImmutableMap<String, ShardId> shardKeyToShardIdMap;
    private final SessionFactory schedulerSessionFactory;


    private final ThreadLocal<Session> currentSessionContext = new ThreadLocal<>();

    public SessionFactoryContextImpl(Map<ShardId, SessionFactory> rwSessionFactoryMap, Map<ShardId, SessionFactory> roSessionFactoryMap,
                                     Map<String, ShardId> shardKeyToShardIdMap,
                                     SessionFactory schedulerSessionFactory) {
        this.RWSessionFactoryImmutableMap = ImmutableMap.copyOf(rwSessionFactoryMap);
        this.ROSessionFactoryImmutableMap = ImmutableMap.copyOf(roSessionFactoryMap);
        this.shardKeyToShardIdMap = ImmutableMap.copyOf(shardKeyToShardIdMap);
        this.schedulerSessionFactory = schedulerSessionFactory;
    }


    @Override
    public void setThreadLocalSession(Session session) {
        currentSessionContext.set(session);
    }

    @Override
    public Session getThreadLocalSession() {
        return currentSessionContext.get();
    }

    @Override
    public SessionFactory getSchedulerSessionFactory() {
        return schedulerSessionFactory;
    }

    @Override
    public SessionFactory getRWSessionFactory(String shardKey) {
        return RWSessionFactoryImmutableMap.get(shardKeyToShardIdMap.get(shardKey));
    }

    @Override
    public SessionFactory getROSessionFactory(ShardId shardId) {
        return ROSessionFactoryImmutableMap.get(shardId);
    }

    @Override
    public void clear() {
        currentSessionContext.remove();
    }
}
