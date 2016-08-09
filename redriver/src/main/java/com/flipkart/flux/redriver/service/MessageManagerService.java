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

import com.flipkart.flux.redriver.dao.MessageDao;
import com.flipkart.flux.redriver.model.ScheduledMessage;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Wrapper around the Message Persistence layer.
 * Current implementation provides for a deferred delete to reduce the number of calls made to the database
 * & also guard against race conditions where we have multiple
 */
@Singleton
public class MessageManagerService {
    private final MessageDao messageDao;

    @Inject
    public MessageManagerService(MessageDao messageDao) {
        this.messageDao = messageDao;
    }

    public void saveMessage(ScheduledMessage message) {
        messageDao.save(message);
    }
    public void scheduleForRemoval(ScheduledMessage message) {

    }
}
