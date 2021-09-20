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

import com.flipkart.flux.persistence.CryptHashGenerator;
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
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransactionInterceptorTest {

    @Test
    public void testTransactionInterceptorWithSessionFactoryContext() {

        final Map<ShardId, SessionFactory> RWSessionFactoryMap = new HashMap<ShardId, SessionFactory>();
        final Map<ShardId, SessionFactory> ROSessionFactoryMap = new HashMap<ShardId, SessionFactory>();
        final Map<String, ShardId> shardStringToShardIdMap = new HashMap<String, ShardId>();
        final SessionFactory schedulerSessionFactory;


        /* create a dummy SessionFactoryContext */
        schedulerSessionFactory = mock(SessionFactory.class);
        ShardId [] shardIds =new ShardId[4];
        SessionFactory[] rwSessionFactoriesArray = new SessionFactory[4];
        Session[] rwSessions = new Session[4];
        Transaction[] rwTransactions = new Transaction[4];

        SessionFactory[] roSessionFactoriesArray = new SessionFactory[4];
        Session[] roSessions = new Session[4];
        Transaction[] roTransactions = new Transaction[4];

        Session mockedSchedulerSession = mock(Session.class);
        Transaction mockedSchedulerTransaction = mock(Transaction.class);
        when(schedulerSessionFactory.openSession()).thenReturn(mockedSchedulerSession);
        when(schedulerSessionFactory.getCurrentSession()).thenReturn(null, mockedSchedulerSession);
        when(mockedSchedulerSession.getTransaction()).thenReturn(mockedSchedulerTransaction);

        for (Integer i = 0; i < 4; i++) {
            // Read - Write Mock Setup
            shardIds[i] = new ShardId(i);
            SessionFactory masterWriteSF = mock(SessionFactory.class);
            Session mockedShardedReadWriteSession = mock(Session.class);
            Transaction mockedShardedReadWriteTransaction = mock(Transaction.class);
            rwSessionFactoriesArray[i] = masterWriteSF;
            rwSessions[i] = mockedShardedReadWriteSession;
            rwTransactions[i] = mockedShardedReadWriteTransaction;

            RWSessionFactoryMap.put(shardIds[i], masterWriteSF);
            when(rwSessionFactoriesArray[i].openSession()).thenReturn(rwSessions[i]);
            when(rwSessionFactoriesArray[i].getCurrentSession()).thenReturn(null, rwSessions[i]);
            when(rwSessions[i].getTransaction()).thenReturn(rwTransactions[i]);

            // Read Only Mock Set up
            SessionFactory slaveReadSF = mock(SessionFactory.class);
            Session mockedShardedReadOnlySession = mock(Session.class);
            Transaction mockedShardedReadOnlyTransaction = mock(Transaction.class);
            roSessionFactoriesArray[i] = slaveReadSF;
            roSessions[i] = mockedShardedReadOnlySession;
            roTransactions[i] = mockedShardedReadOnlyTransaction;

            ROSessionFactoryMap.put(shardIds[i], slaveReadSF);
            when(roSessionFactoriesArray[i].openSession()).thenReturn(roSessions[i]);
            when(roSessionFactoriesArray[i].getCurrentSession()).thenReturn(null, roSessions[i]);
            when(roSessions[i].getTransaction()).thenReturn(roTransactions[i]);

        }

        assert RWSessionFactoryMap.size() == (1 << 2);
        assert ROSessionFactoryMap.size() == (1 << 2);
        for (int i = 0; i < 16; i++)
            for (int j = 0; j < 16; j++) {
                String dbNameSuffix = Integer.toHexString(i) + Integer.toHexString(j);
                shardStringToShardIdMap.put(dbNameSuffix, shardIds[i/4]);
            }
        SessionFactoryContext context = new SessionFactoryContextImpl(RWSessionFactoryMap,
                ROSessionFactoryMap,
                shardStringToShardIdMap,
                schedulerSessionFactory);

        for (int i = 0; i <= 1000; i++) {
            String random_uuid = UUID.randomUUID().toString();
            String shardKey = CryptHashGenerator.getUniformCryptHash(random_uuid);
            ShardId shardId = (ShardId) shardStringToShardIdMap.get(shardKey);
            Injector injector = Guice.createInjector(new TestModule(context, rwSessions[shardId.getShardId()],
                    roSessions[shardId.getShardId()],
                    mockedSchedulerSession));
            InterceptedClass obj = injector.getInstance(InterceptedClass.class);

            obj.verifySessionFactoryAndSessionAndTransactionForShardedMaster(random_uuid);
            obj.verifySessionFactoryAndSessionAndTransactionForShardedSlave(shardId);
            obj.verifySessionFactoryAndSessionAndTransactionForRedriverHost();
        }

    }
}


