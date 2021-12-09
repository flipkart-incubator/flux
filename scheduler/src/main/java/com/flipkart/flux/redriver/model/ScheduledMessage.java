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
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

/**
 * <code>ScheduledMessage</code> is a message that will be stored in DB for redriver purposes.
 *
 * @author yogesh.nachnani
 */
@Entity
@Table(name = "ScheduledMessages")
@IdClass(ScheduledMessage.ScheduledMessagePK.class)
public class ScheduledMessage implements Serializable {

  @Id private Long taskId;
  @Id private String stateMachineId;
  @Id private Long executionVersion;
  private long scheduledTime;

  /* For Hibernate */
  ScheduledMessage() {}

  public ScheduledMessage(
      Long taskId, String stateMachineId, Long scheduledTime, Long executionVersion) {
    this();
    this.taskId = taskId;
    this.stateMachineId = stateMachineId;
    this.scheduledTime = scheduledTime;
    this.executionVersion = executionVersion;
  }

  public long getScheduledTime() {
    return scheduledTime;
  }

  public String getStateMachineId() {
    return stateMachineId;
  }

  public Long getTaskId() {
    return taskId;
  }

  public long getExecutionVersion() {
    return executionVersion;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ScheduledMessage)) return false;

    ScheduledMessage that = (ScheduledMessage) o;

    if (getScheduledTime() != that.getScheduledTime()) return false;
    if (!getTaskId().equals(that.getTaskId())) return false;
    if (executionVersion != that.getExecutionVersion()) return false;
    return getStateMachineId().equals(that.getStateMachineId());
  }

  @Override
  public int hashCode() {
    int result = getTaskId().hashCode();
    result = 31 * result + getStateMachineId().hashCode();
    result = 31 * result + (int) (getScheduledTime() ^ (getScheduledTime() >>> 32));
    result = 31 * result + executionVersion.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "ScheduledMessage{"
        + "taskId="
        + taskId
        + ", stateMachineId='"
        + stateMachineId
        + '\''
        + ", scheduledTime="
        + scheduledTime
        + ", executionVersion="
        + executionVersion
        + '}';
  }

  /**
   * <code>ScheduledMessagePK</code> is the composite primary key of "ScheduledMessages" table in
   * DB.
   */
  static class ScheduledMessagePK implements Serializable {

    private Long taskId;
    private String stateMachineId;
    private Long executionVersion;

    /** for Hibernate */
    public ScheduledMessagePK() {}

    public ScheduledMessagePK(Long taskId, String stateMachineId, Long executionVersion) {
      this.taskId = taskId;
      this.stateMachineId = stateMachineId;
      this.executionVersion = executionVersion;
    }

    public Long getTaskId() {
      return taskId;
    }

    public void setTaskId(Long taskId) {
      this.taskId = taskId;
    }

    public String getStateMachineId() {
      return stateMachineId;
    }

    public void setStateMachineId(String stateMachineId) {
      this.stateMachineId = stateMachineId;
    }

    public Long getExecutionVersion() {
      return executionVersion;
    }

    public void setExecutionVersion(Long executionVersion) {
      this.executionVersion = executionVersion;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof ScheduledMessagePK)) return false;

      ScheduledMessagePK that = (ScheduledMessagePK) o;

      if (!getTaskId().equals(that.getTaskId())) return false;
      if (!getExecutionVersion().equals(that.getExecutionVersion())) return false;
      return getStateMachineId().equals(that.getStateMachineId());
    }

    @Override
    public int hashCode() {
      int result = getTaskId().hashCode();
      result = 31 * result + getStateMachineId().hashCode();
      result = 31 * result + getExecutionVersion().hashCode();
      return result;
    }
  }
}