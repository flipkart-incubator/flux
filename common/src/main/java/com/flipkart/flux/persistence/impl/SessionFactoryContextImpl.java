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
 * for Master(Read-Write) and Slave(Read-Only) Shards, {@link SessionFactory} redriverSessionFactory  as well as shardString to ShardId Mapping for both Slave,Master
 * and uses a thread local to save the SessionFactory that is being used in an ongoing transaction.
 * <p>
 * @author amitkumar.o
 * @author gourav.ashok
 */
public class SessionFactoryContextImpl implements SessionFactoryContext {

    private final ImmutableMap<String, SessionFactory> RWSessionFactoryImmutableMap;
    private final ImmutableMap<String, SessionFactory> ROSessionFactoryImmutableMap;
    private final SessionFactory schedulerSessionFactory;


    private final ThreadLocal<Session> currentSessionFactoryContext = new ThreadLocal<>();

    public SessionFactoryContextImpl(Map<String, SessionFactory> rwSessionFactoryMap, Map<String, SessionFactory> roSessionFactoryMap,
                                     SessionFactory schedulerSessionFactory) {
        this.RWSessionFactoryImmutableMap = ImmutableMap.copyOf(rwSessionFactoryMap);
        this.ROSessionFactoryImmutableMap = ImmutableMap.copyOf(roSessionFactoryMap);
        this.schedulerSessionFactory = schedulerSessionFactory;
    }


    @Override
    public void setSession(Session session) {
        currentSessionFactoryContext.set(session);
    }

    @Override
    public Session getThreadLocalSession() {
        return currentSessionFactoryContext.get();
    }

    @Override
    public SessionFactory getSchedulerSessionFactory() {
        return schedulerSessionFactory;
    }

    @Override
    public SessionFactory getRWSessionFactory(String shardKey) {
        return RWSessionFactoryImmutableMap.get(shardKey);
    }

    @Override
    public SessionFactory getROSessionFactory(String shardKey) {
        return ROSessionFactoryImmutableMap.get(shardKey);
    }

    @Override
    public void clear() {
        currentSessionFactoryContext.remove();
    }
}
