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
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.flipkart.flux.Constants.METRIC_REGISTRY_NAME;

/**
 * Wrapper around the Message Persistence layer.
 * Current implementation provides for a deferred delete to reduce the number of calls made to the database
 * & also guard against race conditions where we have multiple
 */
@Singleton
public class MessageManagerService implements Initializable {
    private final MessageDao messageDao;
    private final Long batchDeleteInterval;
    private final Integer batchSize;
    private final ConcurrentLinkedQueue<Long> messagesToDelete;
    private static final Logger logger = LoggerFactory.getLogger(MessageManagerService.class);
    private final InstrumentedScheduledExecutorService scheduledExecutorService;

    @Inject
    public MessageManagerService(MessageDao messageDao,
                                 @Named("redriver.batchdelete.intervalms") Integer batchDeleteInterval,
                                 @Named("redriver.batchdelete.batchsize") Integer batchSize) {
        this.messageDao = messageDao;
        this.batchDeleteInterval = Long.valueOf(batchDeleteInterval);
        this.batchSize = batchSize;
        this.messagesToDelete = new ConcurrentLinkedQueue<>();
        scheduledExecutorService =
            new InstrumentedScheduledExecutorService(Executors.newScheduledThreadPool(2), SharedMetricRegistries.getOrCreate(METRIC_REGISTRY_NAME));
    }

    public void saveMessage(ScheduledMessage message) {
        messageDao.save(message);
    }
    public void scheduleForRemoval(Long taskId) {
        this.messagesToDelete.add(taskId);
    }

    public List<ScheduledMessage> retrieveAll() {
        return messageDao.retrieveAll();
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(
                new Thread() {
                    @Override
                    public void run() {
                        scheduledExecutorService.shutdown();
                        try {
                            scheduledExecutorService.awaitTermination(2,TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            logger.error("Could not shutdown scheduled executor service",e);
                        }
                    }
                }
        );
    }

    @Override
    public void initialize() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            int i = 0;
            Long currentMessageIdToDelete = null;
            List<Long> messageIdsToDelete = new ArrayList<>(batchSize);
            do {
                currentMessageIdToDelete = this.messagesToDelete.poll();
                if (currentMessageIdToDelete != null) {
                    messageIdsToDelete.add(currentMessageIdToDelete);
                }
                i++;
            } while (currentMessageIdToDelete != null && i < batchSize);
            if (!messageIdsToDelete.isEmpty()) {
                messageDao.deleteInBatch(messageIdsToDelete);
            }
        }, 0l, batchDeleteInterval, TimeUnit.MILLISECONDS);

        registerShutdownHook();
    }
}
