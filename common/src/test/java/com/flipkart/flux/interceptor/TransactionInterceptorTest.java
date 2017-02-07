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

import com.flipkart.flux.persistence.DataSourceType;
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransactionInterceptorTest {

    @Test
    public void testTransactionInterceptorWithSessionFactoryContext() {

        /* create a dummy SessionFactoryContext */
        SessionFactory readWriteSf = mock(SessionFactory.class);
        SessionFactory readOnlySf = mock(SessionFactory.class);

        prepareInteractions(readWriteSf);
        prepareInteractions(readOnlySf);

        Map<DataSourceType, SessionFactory> map = new HashMap<>();
        map.put(DataSourceType.READ_WRITE, readWriteSf);
        map.put(DataSourceType.READ_ONLY, readOnlySf);

        SessionFactoryContext context = new SessionFactoryContextImpl(map, DataSourceType.READ_WRITE);

        Injector injector = Guice.createInjector(new TestModule(context, readOnlySf));
        InterceptedClass obj = injector.getInstance(InterceptedClass.class);

        obj.readSome();
        obj.writeSome();
        obj.readSome();
        obj.writeSome();
    }

    private void prepareInteractions(SessionFactory sf) {
        Session mockedSession1 = mock(Session.class);
        Transaction mockedTransaction1 = mock(Transaction.class);

        when(sf.openSession()).thenReturn(mockedSession1);
        when(sf.getCurrentSession()).thenReturn(null, mockedSession1);
        when(mockedSession1.getTransaction()).thenReturn(mockedTransaction1);
    }
}
