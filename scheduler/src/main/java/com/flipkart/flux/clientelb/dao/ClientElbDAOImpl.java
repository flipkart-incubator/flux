/*
 * Copyright 2012-2018, the original author or authors.
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

package com.flipkart.flux.clientelb.dao;

import com.flipkart.flux.clientelb.dao.iface.ClientElbDAO;
import com.flipkart.flux.domain.ClientElb;
import com.flipkart.flux.persistence.SelectDataSource;
import com.flipkart.flux.persistence.SessionFactoryContext;
import com.flipkart.flux.persistence.Storage;
import com.google.inject.name.Named;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

/**
 * <code>ClientElbDAOImpl</code> is an implementation of {@link ClientElbDAO} which uses Hibernate to perform
 *  CRUD operations.
 *
 * @author akif.khan
 */
public class ClientElbDAOImpl implements ClientElbDAO {

    private SessionFactoryContext sessionFactoryContext;

    @Inject
    public ClientElbDAOImpl(@Named("schedulerSessionFactoriesContext") SessionFactoryContext sessionFactoryContext) {
        this.sessionFactoryContext = sessionFactoryContext;
    }

    /**
     * Creates client elb entry in DB and returns saved object
     */
    @Override
    @Transactional
    @SelectDataSource(storage = Storage.SCHEDULER)
    public ClientElb create(ClientElb clientElb) {
        currentSession().save(clientElb);
        return clientElb;
    }

    /**
     * Retrieves Client Elb from DB by it's unique identifier
     */
    @Override
    @Transactional
    @SelectDataSource(storage = Storage.SCHEDULER)
   public  ClientElb findById(String id) {
        Criteria criteria = currentSession().createCriteria(ClientElb.class)
                .add(Restrictions.eq("id", id));
        Object object = criteria.uniqueResult();
        ClientElb castedObject = null;
        if(object != null)
            castedObject = (ClientElb) object;
        else
            castedObject = null;
        return castedObject;
    }

    /**
     * Updates Elb Url of a client in DB
     */
    @Override
    @Transactional
    @SelectDataSource(storage = Storage.SCHEDULER)
    public void updateElbUrl(String clientElbId, String clientElbUrl) {
        Query updateQuery = currentSession().createQuery(
                "update ClientElb set elbUrl = :clientElbUrl where id = :clientElbId");
        updateQuery.setString("clientElbUrl", clientElbUrl.toString());
        updateQuery.setString("clientElbId", clientElbId);
        updateQuery.executeUpdate();
    }

    /**
     * Delete Client Elb entry from DB
     */
    @Override
    @Transactional
    @SelectDataSource(storage = Storage.SCHEDULER)
    public void delete(String id) {
        Query deleteQuery = currentSession().createQuery("delete ClientElb c where c.id=:id");
        deleteQuery.setString("id", id);
        deleteQuery.executeUpdate();
    }

    /**
     * Provides the session which is bound to current thread.
     * @return Session
     */
    public Session currentSession() {
        return sessionFactoryContext.getThreadLocalSession();
    }

}
