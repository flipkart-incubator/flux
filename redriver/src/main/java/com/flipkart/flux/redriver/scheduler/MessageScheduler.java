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

package com.flipkart.flux.redriver.scheduler;

import com.flipkart.flux.redriver.dao.MessageDao;
import com.flipkart.flux.redriver.model.ScheduledMessage;
import com.flipkart.flux.redriver.model.ScheduledMessageComparator;
import com.flipkart.flux.redriver.service.MessageManagerService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.PriorityQueue;

/**
 * The scheduler uses an in memory priority queue to prioritise messages by their scheduled time
 * The message with the least scheduled time (i.e, the next message to be picked up) is picked and sent if the current time is
 * greater than or equal to scheduledTime. If not, the scheduler thread sleeps for the difference.
 *
 */
@Singleton
public class MessageScheduler {

    private final PriorityQueue<ScheduledMessage> messages;
    private final MessageManagerService messageManagerService;

    @Inject
    public MessageScheduler(MessageManagerService messageManagerService) {
        this(messageManagerService, new PriorityQueue<>(new ScheduledMessageComparator()));
    }


    MessageScheduler(MessageManagerService messageManagerService, PriorityQueue<ScheduledMessage> scheduledMessages) {
        this.messageManagerService = messageManagerService;
        this.messages = scheduledMessages;
    }

    public void addMessage(ScheduledMessage scheduledMessage) {
        this.messageManagerService.saveMessage(scheduledMessage);
        this.messages.add(scheduledMessage);
    }

    public void removeMessage(String messageId) {
        final Optional<ScheduledMessage> scheduledMessageOptional
            = this.messages.stream().filter((m) -> m.getMessageId().equals(messageId)).findFirst();
        if (scheduledMessageOptional.isPresent()) {
            final ScheduledMessage message = scheduledMessageOptional.get();
            /* It is important that we delete from priority queue first. Its okay even if we the schedule for removal call fails.
                At most we will send a message that was not supposed to be sent but the receiver should be able to handle
                such cases by consulting its own DB.
             */
            this.messages.remove(message);
            this.messageManagerService.scheduleForRemoval(message);
        }
    }

    public void start() {

    }

    public void stop() {

    }
}
