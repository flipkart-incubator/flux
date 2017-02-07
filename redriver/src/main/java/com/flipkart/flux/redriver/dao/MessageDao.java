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

package com.flipkart.flux.redriver.dao;

import com.flipkart.flux.persistence.SessionFactoryContext;
import com.flipkart.flux.redriver.model.ScheduledMessage;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.List;

/**
 * <code>MessageDao</code> handles all Db interactions for {@link ScheduledMessage}(s)
 *
 * @author yogesh.nachnani
 */
@Singleton
public class MessageDao {

    private SessionFactoryContext sessionFactoryContext;

    @Inject
    public MessageDao(@Named("redriverSessionFactoryContext") SessionFactoryContext sessionFactoryContext) {
        this.sessionFactoryContext = sessionFactoryContext;
    }

    @Transactional
    public void save(ScheduledMessage scheduledMessage) {
        currentSession().saveOrUpdate(scheduledMessage);
    }

    /**
     * Retrieves rows offset to offset+rowCount from ScheduledMessages table ordered by scheduledTime ascending.
     * @param offset
     * @param rowCount
     */
    @Transactional
    public List<ScheduledMessage> retrieveOldest(int offset, int rowCount) {
        return currentSession()
                .createCriteria(ScheduledMessage.class)
                .addOrder(Order.asc("scheduledTime"))
                .setFirstResult(offset)
                .setMaxResults(rowCount)
                .list();
    }

    /**
     * Deletes the corresponding {@link ScheduledMessage}s from ScheduledMessages table in one shot.
     * @param messageIdsToDelete List of {@link ScheduledMessage} Ids
     */
    @Transactional
    public void deleteInBatch(List<Long> messageIdsToDelete) {
        final Query deleteQuery = currentSession().createQuery("delete ScheduledMessage s where s.taskId in :msgList ");
        deleteQuery.setParameterList("msgList",messageIdsToDelete);
        deleteQuery.executeUpdate();
    }

    /**
     * Provides the session which is bound to current thread.
     * @return Session
     */
    private Session currentSession() {
        return sessionFactoryContext.getSessionFactory().getCurrentSession();
    }
}
