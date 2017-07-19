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

        final Map RWSessionFactoryMap = new HashMap<String, SessionFactory>();
        final Map ROSessionFactoryMap = new HashMap<String, SessionFactory>();
        final SessionFactory schedulerSessionFactory;


        /* create a dummy SessionFactoryContext */
        schedulerSessionFactory = mock(SessionFactory.class);

        SessionFactory[] rwSessionFactoriesArray = new SessionFactory[1 << 8];
        Session[] rwSessions = new Session[1 << 8];
        Transaction[] rwTransactions = new Transaction[1 << 8];

        SessionFactory[] roSessionFactoriesArray = new SessionFactory[1 << 8];
        Session[] roSessions = new Session[1 << 8];
        Transaction[] roTransactions = new Transaction[1 << 8];

        Session mockedSchedulerSession = mock(Session.class);
        Transaction mockedSchedulerTransaction = mock(Transaction.class);
        when(schedulerSessionFactory.openSession()).thenReturn(mockedSchedulerSession);
        when(schedulerSessionFactory.getCurrentSession()).thenReturn(null, mockedSchedulerSession);
        when(mockedSchedulerSession.getTransaction()).thenReturn(mockedSchedulerTransaction);

        Map<String, Integer> shardIndexes = new HashMap<>();
        for (Integer i = 0; i < 16; i++)
            for (Integer j = 0; j < 16; j++) {
                // Read - Write Mock Setup
                String shardKey = Integer.toHexString(i) + Integer.toHexString(j);
                SessionFactory masterWriteSF = mock(SessionFactory.class);
                Session mockedShardedReadWriteSession = mock(Session.class);
                Transaction mockedShardedReadWriteTransaction = mock(Transaction.class);
                rwSessionFactoriesArray[i * 16 + j] = masterWriteSF;
                rwSessions[i * 16 + j] = mockedShardedReadWriteSession;
                rwTransactions[i * 16 + j] = mockedShardedReadWriteTransaction;

                RWSessionFactoryMap.put(shardKey, masterWriteSF);
                when(rwSessionFactoriesArray[i * 16 + j].openSession()).thenReturn(rwSessions[i * 16 + j]);
                when(rwSessionFactoriesArray[i * 16 + j].getCurrentSession()).thenReturn(null, rwSessions[i * 16 + j]);
                when(rwSessions[i * 16 + j].getTransaction()).thenReturn(rwTransactions[i * 16 + j]);

                //System.out.println(Integer.toString(i * 16 + j) + " fact " + rwSessionFactoriesArray[i * 16 + j].equals(masterWriteSF) + " ,  sess " + rwSessions[i * 16 + j].equals(mockedShardedReadWriteSession) + " , tra " + rwTransactions[i * 16 + j].equals(mockedShardedReadWriteTransaction));
                // Read Only Mock Set up
                SessionFactory slaveReadSF = mock(SessionFactory.class);
                Session mockedShardedReadOnlySession = mock(Session.class);
                Transaction mockedShardedReadOnlyTransaction = mock(Transaction.class);
                roSessionFactoriesArray[i * 16 + j] = slaveReadSF;
                roSessions[i * 16 + j] = mockedShardedReadOnlySession;
                roTransactions[i * 16 + j] = mockedShardedReadOnlyTransaction;

                ROSessionFactoryMap.put(shardKey, slaveReadSF);
                when(roSessionFactoriesArray[i * 16 + j].openSession()).thenReturn(roSessions[i * 16 + j]);
                when(roSessionFactoriesArray[i * 16 + j].getCurrentSession()).thenReturn(null, roSessions[i * 16 + j]);
                when(roSessions[i * 16 + j].getTransaction()).thenReturn(roTransactions[i * 16 + j]);

                shardIndexes.put(shardKey, i * 16 + j);

            }
        assert RWSessionFactoryMap.size() == (1 << 8);
        assert ROSessionFactoryMap.size() == (1 << 8);
        SessionFactoryContext context = new SessionFactoryContextImpl(RWSessionFactoryMap,
                ROSessionFactoryMap,
                schedulerSessionFactory);
        for (int i = 0; i <= 5000; i++) {
            String random_Shard_Id = UUID.randomUUID().toString();
            String shardKey = CryptHashGenerator.getUniformCryptHash(random_Shard_Id);
            int index = shardIndexes.get(shardKey);
            Injector injector = Guice.createInjector(new TestModule(context, rwSessions[index],
                    roSessions[index],
                    mockedSchedulerSession));
            InterceptedClass obj = injector.getInstance(InterceptedClass.class);

            obj.verifySessionFactoryAndSessionAndTransactionForShardedMaster(random_Shard_Id);
            obj.verifySessionFactoryAndSessionAndTransactionForShardedSlave(shardKey);
            obj.verifySessionFactoryAndSessionAndTransactionForRedriverHost();
        }

    }
}


