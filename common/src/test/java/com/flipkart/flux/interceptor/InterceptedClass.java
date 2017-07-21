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

import com.flipkart.flux.persistence.*;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.hibernate.Session;
import org.junit.Assert;

import javax.transaction.Transactional;

/**
 * Created by gaurav.ashok on 24/11/16.
 */
public class InterceptedClass {

    @Inject
    private SessionFactoryContext context;

    @Inject
    @Named("shardedReadWriteSession")
    private Session shardedReadWriteSession;

    @Inject
    @Named("shardedReadOnlySession")
    private Session shardedReadOnlySession;

    @Inject
    @Named("schedulerSession")
    private Session schedulerSession;

    @Transactional
    @SelectDataSource(type=DataSourceType.READ_WRITE, storage=STORAGE.SHARDED)
    public void verifySessionFactoryAndSessionAndTransactionForShardedMaster(String shardKey) {
        Assert.assertSame(context.getThreadLocalSession(), shardedReadWriteSession);
    }

    @Transactional
    @SelectDataSource(type=DataSourceType.READ_ONLY, storage=STORAGE.SHARDED)
    public void verifySessionFactoryAndSessionAndTransactionForShardedSlave(String shardPrefix) {
        Assert.assertSame(context.getThreadLocalSession(), shardedReadOnlySession);
    }

    @Transactional
    @SelectDataSource(type=DataSourceType.READ_WRITE, storage=STORAGE.SCHEDULER)
    public void verifySessionFactoryAndSessionAndTransactionForRedriverHost() {
        Assert.assertSame(context.getThreadLocalSession(), schedulerSession);
    }
}
