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

package com.flipkart.flux.interceptor;

/**
 * Created by gaurav.ashok on 24/11/16.
 */

import com.flipkart.flux.persistence.SessionFactoryContext;
import com.flipkart.flux.persistence.impl.SessionFactoryContextImpl;
import com.flipkart.flux.shard.ShardId;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransactionInterceptorTest {

    @Test
    public void testTransactionInterceptorWithSessionFactoryContext() {

        final Map RWSessionFactoryMap = new HashMap<ShardId, SessionFactory>();
        final Map ROSessionFactoryMap = new HashMap<ShardId, SessionFactory>();
        final Map shardKeyToRWShardIdMap = new HashMap<Character, ShardId>();
        final Map shardKeyToROShardIdMap = new HashMap<Character, ShardId>();
        final SessionFactory redriverSessionFactory;


        /* create a dummy SessionFactoryContext */
        ShardId writeOnlyShardId = new ShardId(1);
        ShardId readOnlyShardId = new ShardId(2);
        SessionFactory masterWriteSF = mock(SessionFactory.class);
        SessionFactory slaveReadSF = mock(SessionFactory.class);
        redriverSessionFactory = mock(SessionFactory.class);
        RWSessionFactoryMap.put(writeOnlyShardId, masterWriteSF);
        ROSessionFactoryMap.put(readOnlyShardId, slaveReadSF);

        for (Integer i = 0; i < 16; i++) {
            shardKeyToRWShardIdMap.put(Integer.toHexString(i).charAt(0), writeOnlyShardId);
            shardKeyToROShardIdMap.put(Integer.toHexString(i).charAt(0), readOnlyShardId);
        }

        SessionFactoryContext context = new SessionFactoryContextImpl(RWSessionFactoryMap,
                ROSessionFactoryMap,
                shardKeyToRWShardIdMap,
                shardKeyToROShardIdMap,
                redriverSessionFactory);

        Session mockedShardedReadWriteSession = mock(Session.class);
        Transaction mockedShardedReadWriteTransaction = mock(Transaction.class);
        when(masterWriteSF.openSession()).thenReturn(mockedShardedReadWriteSession);
        when(masterWriteSF.getCurrentSession()).thenReturn(null, mockedShardedReadWriteSession);
        when(mockedShardedReadWriteSession.getTransaction()).thenReturn(mockedShardedReadWriteTransaction);


        Session mockedShardedReadOnlySession = mock(Session.class);
        Transaction mockedShardedReadOnlyTransaction = mock(Transaction.class);
        when(slaveReadSF.openSession()).thenReturn(mockedShardedReadOnlySession);
        when(slaveReadSF.getCurrentSession()).thenReturn(null, mockedShardedReadOnlySession);
        when(mockedShardedReadOnlySession.getTransaction()).thenReturn(mockedShardedReadOnlyTransaction);

        Session mockedRedriverSession = mock(Session.class);
        Transaction mockedRedriverTransaction = mock(Transaction.class);
        when(redriverSessionFactory.openSession()).thenReturn(mockedRedriverSession);
        when(redriverSessionFactory.getCurrentSession()).thenReturn(null, mockedRedriverSession);
        when(mockedRedriverSession.getTransaction()).thenReturn(mockedRedriverTransaction);


        Injector injector = Guice.createInjector(new TestModule(context, mockedShardedReadWriteSession, mockedShardedReadOnlySession,
                mockedRedriverSession));
        InterceptedClass obj = injector.getInstance(InterceptedClass.class);

        obj.verifySessionFactoryAndSessionAndTransactionForShardedMaster("sample-shard-key");
        obj.verifySessionFactoryAndSessionAndTransactionForShardedSlave(readOnlyShardId);
        obj.verifySessionFactoryAndSessionAndTransactionForRedriverHost();

        // add more tests , check InterceptedClass
    }

}
