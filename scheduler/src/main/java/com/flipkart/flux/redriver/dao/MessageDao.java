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

import com.flipkart.flux.persistence.SelectDataSource;
import com.flipkart.flux.persistence.SessionFactoryContext;
import com.flipkart.flux.persistence.Storage;
import com.flipkart.flux.redriver.model.ScheduledMessage;
import com.flipkart.flux.redriver.model.SmIdAndTaskIdPairWithExecutionVersion;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

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
    public MessageDao(@Named("schedulerSessionFactoriesContext") SessionFactoryContext sessionFactoryContext) {
        this.sessionFactoryContext = sessionFactoryContext;
    }

    @Transactional
    @SelectDataSource(storage = Storage.SCHEDULER)
    public void save(ScheduledMessage scheduledMessage) {
        currentSession().saveOrUpdate(scheduledMessage);
    }

    @Transactional
    @SelectDataSource(storage = Storage.SCHEDULER)
    public int bulkInsertOrUpdate(List<ScheduledMessage> messages) {
        StringBuilder query = new StringBuilder("insert into ScheduledMessages ( stateMachineId , taskId , " +
                "scheduledTime, executionVersion )  values ");
        messages.forEach(scheduledMessage -> {
            query.append("( \'").append(scheduledMessage.getStateMachineId()).append("\' , ");
            query.append(scheduledMessage.getTaskId()).append(" , ");
            query.append(scheduledMessage.getScheduledTime()).append(",");
            query.append(scheduledMessage.getExecutionVersion()).append("), ");
        });
        query.deleteCharAt(query.length() - 1);
        query.setCharAt(query.length() - 1, ' ');
        query.append("on duplicate key update scheduledTime = values(scheduledTime)");
        // created native SQL query, required full table name.
        final Query insertOrUpdateQuery = currentSession().createSQLQuery(query.toString());
        return insertOrUpdateQuery.executeUpdate();
    }

    @Transactional
    @SelectDataSource(storage = Storage.SCHEDULER)
    public Long redriverCount() {
        return ((Long) currentSession().createQuery("select count(*) from ScheduledMessage").iterate().next());
    }

    /**
     * Retrieves rows offset to offset+rowCount from ScheduledMessages table ordered by scheduledTime ascending.
     *
     * @param offset
     * @param rowCount
     */
    @SuppressWarnings("unchecked")
	@Transactional
    @SelectDataSource(storage = Storage.SCHEDULER)
    public List<ScheduledMessage> retrieveOldest(int offset, int rowCount) {
        return currentSession()
                .createCriteria(ScheduledMessage.class)
                .addOrder(Order.asc("scheduledTime"))
                .add(Restrictions.lt("scheduledTime", System.currentTimeMillis()))
                .setFirstResult(offset)
                .setMaxResults(rowCount)
                .list();
    }

    /**
     * Deletes the corresponding {@link ScheduledMessage}s from ScheduledMessages table in one shot.
     *
     * @param messageIdsToDelete List of {@link ScheduledMessage} Ids
     */
    @Transactional
    @SelectDataSource(storage = Storage.SCHEDULER)
    public int deleteInBatch(List<SmIdAndTaskIdPairWithExecutionVersion> messageIdsToDelete) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("delete from ScheduledMessage where (stateMachineId,taskId, executionVersion) in (");
        messageIdsToDelete.forEach(smIdAndTaskIdPairWithExecutionVersion -> queryBuilder.append("(\'")
                .append(smIdAndTaskIdPairWithExecutionVersion.getSmId())
                .append("\',\'").append(smIdAndTaskIdPairWithExecutionVersion.getTaskId())
                .append("\',\'").append(smIdAndTaskIdPairWithExecutionVersion.getExecutionVersion())
                .append("\'),"));
        queryBuilder.setCharAt(queryBuilder.length() - 1, ')');
        final Query deleteQuery = currentSession().createQuery(queryBuilder.toString());
        return deleteQuery.executeUpdate();
    }

    @Transactional
    @SelectDataSource(storage = Storage.SCHEDULER)
    public void delete(SmIdAndTaskIdPairWithExecutionVersion smIdAndTaskIdPairWithExecutionVersion) {
        Query query = currentSession().createQuery("delete from ScheduledMessage where stateMachineId = :smId and taskId = :taskId and executionVersion =:executionVersion");
        query.setString("smId", smIdAndTaskIdPairWithExecutionVersion.getSmId());
        query.setLong("taskId", smIdAndTaskIdPairWithExecutionVersion.getTaskId());
        query.setLong("executionVersion",smIdAndTaskIdPairWithExecutionVersion.getExecutionVersion());
        query.executeUpdate();
    }

    /**
     * Provides the session which is bound to current thread.
     *
     * @return Session
     */
    private Session currentSession() {
        return sessionFactoryContext.getThreadLocalSession();
    }
}
