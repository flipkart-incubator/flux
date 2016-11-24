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

import com.flipkart.flux.persistence.DataSourceType;
import com.flipkart.flux.persistence.SelectDataSource;
import com.flipkart.flux.persistence.SessionFactoryContext;
import com.google.inject.Inject;
import org.hibernate.SessionFactory;
import org.junit.Assert;

import javax.transaction.Transactional;

/**
 * Created by gaurav.ashok on 24/11/16.
 */
public class InterceptedClass {

    @Inject
    private SessionFactoryContext context;

    @Inject
    private SessionFactory readOnlySessionFactory;

    @Transactional
    @SelectDataSource(DataSourceType.READ_ONLY)
    public void readSome() {
        /* assert that the current session factory in context is equals to readOnlySessionFactory. */
        Assert.assertEquals(context.getCurrent(), readOnlySessionFactory);
    }

    @Transactional
    public void writeSome() {
        /* assert that the current session factory in context is not null and not equals to readOnlySessionFactory. */
        Assert.assertNotNull(context.getCurrent());
        Assert.assertNotEquals(context.getCurrent(), readOnlySessionFactory);
    }
}
