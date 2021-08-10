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

package com.flipkart.flux.redriver.service;

import static com.flipkart.flux.Constants.METRIC_REGISTRY_NAME;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.codahale.metrics.InstrumentedExecutorService;
import com.codahale.metrics.InstrumentedScheduledExecutorService;
import com.codahale.metrics.SharedMetricRegistries;
import com.flipkart.flux.redriver.dao.MessageDao;
import com.flipkart.flux.redriver.model.ScheduledMessage;
import com.flipkart.flux.redriver.model.SmIdAndTaskIdPair;
import com.flipkart.polyguice.core.Initializable;

/**
 * Wrapper around the Message Persistence layer.
 * Current implementation provides for a deferred delete to reduce the number of calls made to the database
 * & also guard against race conditions where we have multiple
 *
 * @author gaurav.ashok
 */
@Singleton
public class MessageManagerService implements Initializable {

    private static final Logger logger = LogManager.getLogger(MessageManagerService.class);
    private static final String scheduledDeletionSvcName = "redriver-batch-delete-executor-svc";
    private static final String taskRegisterSvcName = "redriver-task-register-executor-svc";
    private static final String scheduledInsertionSvcName = "redriver-batch-insertion-executer-svc";
    private static final String scheduledDeletionSvcFailureCounter = "scheduled.deletion.failure.counter";
    private final MessageDao messageDao;
    private final Integer batchDeleteInterval;
    private final Integer batchDeleteSize;
    private final Integer batchInsertInterval;
    private final Integer batchInsertSize;
    private final ConcurrentLinkedQueue<SmIdAndTaskIdPair> messagesToDelete;
    private final ConcurrentLinkedQueue<ScheduledMessage> messagesToInsertOrUpdate;
    private final InstrumentedScheduledExecutorService scheduledDeletionService;
    private final InstrumentedScheduledExecutorService scheduledInsertionService;
    private final InstrumentedExecutorService persistenceExecutorService;

    @Inject
    public MessageManagerService(MessageDao messageDao,
                                 @Named("redriver.noOfPersistenceWorkers") int noOfPersistenceWorkers,
                                 @Named("redriver.batchDelete.intervalms") Integer batchDeleteInterval,
                                 @Named("redriver.batchDelete.batchSize") Integer batchDeleteSize,
                                 @Named("redriver.batchInsert.batchSize") Integer batchInsertSize,
                                 @Named("redriver.batchInsert.intervalms") Integer batchInsertInterval) {
        this.messageDao = messageDao;
        this.batchDeleteInterval = batchDeleteInterval;
        this.batchDeleteSize = batchDeleteSize;
        this.batchInsertInterval = batchInsertInterval;
        this.batchInsertSize = batchInsertSize;
        this.messagesToInsertOrUpdate = new ConcurrentLinkedQueue<>();
        this.messagesToDelete = new ConcurrentLinkedQueue<>();
        scheduledInsertionService = new InstrumentedScheduledExecutorService(Executors.newScheduledThreadPool(3),
                SharedMetricRegistries.getOrCreate(METRIC_REGISTRY_NAME), scheduledInsertionSvcName);
        scheduledDeletionService =
                new InstrumentedScheduledExecutorService(Executors.newScheduledThreadPool(2), SharedMetricRegistries.getOrCreate(METRIC_REGISTRY_NAME), scheduledDeletionSvcName);
        persistenceExecutorService =
                new InstrumentedExecutorService(Executors.newFixedThreadPool(noOfPersistenceWorkers), SharedMetricRegistries.getOrCreate(METRIC_REGISTRY_NAME), taskRegisterSvcName);
    }

    public List<ScheduledMessage> retrieveOldest(int offset, int count) {
        logger.info("Retrieving messages from ScheduledMessages offset:{} count:{}", offset, count);
        return messageDao.retrieveOldest(offset, count);
    }

    public void saveMessage(ScheduledMessage message) {
        messagesToInsertOrUpdate.add(message);
        // persistenceExecutorService.execute(() -> messageDao.save(message));
    }

    public void scheduleForRemoval(String stateMachine, Long taskId) {
        // persistenceExecutorService.execute(() -> messageDao.delete(new SmIdAndTaskIdPair(stateMachine, taskId)));
        messagesToDelete.add(new SmIdAndTaskIdPair(stateMachine, taskId));
    }

    @Override
    public void initialize() {
        scheduledDeletionService.scheduleAtFixedRate(() -> {
            try {
                SmIdAndTaskIdPair currentMessageIdToDelete = null;
                List messageIdsToDelete = new ArrayList<SmIdAndTaskIdPair>(batchDeleteSize);
                while (messageIdsToDelete.size() < batchDeleteSize && (currentMessageIdToDelete = messagesToDelete.poll()) != null) {
                    messageIdsToDelete.add(currentMessageIdToDelete);
                }
                if (!messageIdsToDelete.isEmpty()) {
                    logger.info("Running Deletion job, trying deleting {} messages", messageIdsToDelete.size());
                    int rowsAffected = messageDao.deleteInBatch(messageIdsToDelete);
                    logger.info("Actually Deleted {} rows", rowsAffected);
                }
            } catch (Throwable throwable) {
                logger.error("ScheduledDeletion Job failed for Redriver Messages.", throwable);
            }
        }, 0l, batchDeleteInterval, TimeUnit.MILLISECONDS);

        scheduledInsertionService.scheduleAtFixedRate(() -> {
            try {
                ScheduledMessage currentMessageIdToInsert = null;
                List messagesToInsert = new ArrayList<ScheduledMessage>(batchInsertSize);
                while (messagesToInsert.size() < batchInsertSize && (currentMessageIdToInsert = messagesToInsertOrUpdate.poll()) != null) {
                    messagesToInsert.add(currentMessageIdToInsert);
                }
                if (!messagesToInsert.isEmpty()) {
                    logger.info("Running Insertion job, trying inserting {} messages", messagesToInsert.size());
                    int rowsAffected = messageDao.bulkInsertOrUpdate(messagesToInsert);
                    logger.info("Actually Inserted/Updated {} rows", rowsAffected);
                }
            } catch (Throwable throwable) {
                logger.error("ScheduledInsertion Job failed for Redriver Messages.", throwable);
            }
        }, 0l, batchInsertInterval, TimeUnit.MILLISECONDS);
        registerShutdownHook(scheduledInsertionService, 10, "Could not shutdown executorService " + scheduledInsertionService);
        registerShutdownHook(scheduledDeletionService, 2, "Could not shutdown executorService " + scheduledDeletionSvcName);
        registerShutdownHook(persistenceExecutorService, 5, "Error occurred while terminating Redriver's persistence executor service");
    }

    private void registerShutdownHook(ExecutorService executorService, int timeout, String customErrorMsg) {
        Runtime.getRuntime().addShutdownHook(
                new Thread() {
                    @Override
                    public void run() {
                        executorService.shutdown();
                        try {
                            executorService.awaitTermination(timeout, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            logger.error(customErrorMsg, e);
                        }
                    }
                }
        );
    }
}
