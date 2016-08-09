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

package com.flipkart.flux.redriver.model;

import java.io.Serializable;

/**
 * A message that will be sent to a particular actor denoted by the url at the scheduled time
 */
public class ScheduledMessage implements Serializable {

    private final String messageId;
    private final String url;
    private final Serializable data;
    private final long scheduledTime;

    public ScheduledMessage(String messageId, Serializable data, Long scheduledTime, String url) {
        this.messageId = messageId;
        this.data = data;
        this.scheduledTime = scheduledTime;
        this.url = url;
    }

    public long getScheduledTime() {
        return scheduledTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScheduledMessage that = (ScheduledMessage) o;

        if (scheduledTime != that.scheduledTime) return false;
        if (!messageId.equals(that.messageId)) return false;
        if (!url.equals(that.url)) return false;
        return !(data != null ? !data.equals(that.data) : that.data != null);

    }

    @Override
    public int hashCode() {
        int result = messageId.hashCode();
        result = 31 * result + url.hashCode();
        result = 31 * result + (data != null ? data.hashCode() : 0);
        result = 31 * result + (int) (scheduledTime ^ (scheduledTime >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "ScheduledMessage{" +
            "data=" + data +
            ", messageId='" + messageId + '\'' +
            ", url='" + url + '\'' +
            ", scheduledTime=" + scheduledTime +
            '}';
    }

    public String getMessageId() {
        return messageId;
    }
}
