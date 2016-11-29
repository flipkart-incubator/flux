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

import com.codahale.metrics.InstrumentedExecutorService;
import com.codahale.metrics.InstrumentedScheduledExecutorService;
import com.codahale.metrics.SharedMetricRegistries;
import com.flipkart.flux.redriver.dao.MessageDao;
import com.flipkart.flux.redriver.model.ScheduledMessage;
import com.flipkart.polyguice.core.Initializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.flipkart.flux.Constants.METRIC_REGISTRY_NAME;

/**
 * Wrapper around the Message Persistence layer.
 * Current implementation provides for a deferred delete to reduce the number of calls made to the database
 * & also guard against race conditions where we have multiple
 *
 * @author gaurav.ashok
 */
@Singleton
public class MessageManagerService implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(MessageManagerService.class);
    private static final String scheduledDeletionSvcName = "redriver-batch-delete-executor-svc";
    private static final String taskRegisterSvcName = "redriver-task-register-executor-svc";

    private final MessageDao messageDao;
    private final Integer batchDeleteInterval;
    private final Integer batchSize;
    private final ConcurrentLinkedQueue<Long> messagesToDelete;
    private final InstrumentedScheduledExecutorService scheduledDeletionService;
    private final InstrumentedExecutorService persistenceExecutorService;

    @Inject
    public MessageManagerService(MessageDao messageDao,
                                 @Named("redriver.noOfPersistenceWorkers") int noOfPersistenceWorkers,
                                 @Named("redriver.batchdelete.intervalms") Integer batchDeleteInterval,
                                 @Named("redriver.batchdelete.batchsize") Integer batchSize) {
        this.messageDao = messageDao;
        this.batchDeleteInterval = batchDeleteInterval;
        this.batchSize = batchSize;
        this.messagesToDelete = new ConcurrentLinkedQueue<>();
        scheduledDeletionService =
            new InstrumentedScheduledExecutorService(Executors.newScheduledThreadPool(2), SharedMetricRegistries.getOrCreate(METRIC_REGISTRY_NAME), scheduledDeletionSvcName);
        persistenceExecutorService =
            new InstrumentedExecutorService(Executors.newFixedThreadPool(noOfPersistenceWorkers), SharedMetricRegistries.getOrCreate(METRIC_REGISTRY_NAME), taskRegisterSvcName);
    }

    public List<ScheduledMessage> retrieveOldest(int offset, int count) {
        return messageDao.retrieveOldest(offset, count);
    }

    public void saveMessage(ScheduledMessage message) {
        persistenceExecutorService.execute(() -> messageDao.save(message));
    }
    public void scheduleForRemoval(Long taskId) {
        messagesToDelete.add(taskId);
    }

    @Override
    public void initialize() {
        scheduledDeletionService.scheduleAtFixedRate(() -> {
            Long currentMessageIdToDelete = null;
            List<Long> messageIdsToDelete = new ArrayList<>(batchSize);

            while(messageIdsToDelete.size() < batchSize && (currentMessageIdToDelete = messagesToDelete.poll()) != null) {
                messageIdsToDelete.add(currentMessageIdToDelete);
            }

            if (!messageIdsToDelete.isEmpty()) {
                messageDao.deleteInBatch(messageIdsToDelete);
            }
        }, 0l, batchDeleteInterval, TimeUnit.MILLISECONDS);

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
                        executorService.awaitTermination(timeout,TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        logger.error(customErrorMsg, e);
                    }
                }
            }
        );
    }
}
