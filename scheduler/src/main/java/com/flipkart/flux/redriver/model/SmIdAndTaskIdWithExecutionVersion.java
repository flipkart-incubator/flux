package com.flipkart.flux.redriver.model;

/**
 * Created by amitkumar.o on 31/07/17.
 */
public class SmIdAndTaskIdWithExecutionVersion {
    private String smId;
    private Long taskId;
    private Long executionVersion;

    public SmIdAndTaskIdWithExecutionVersion(String smId, Long taskId, Long executionVersion) {
        this.smId = smId;
        this.taskId = taskId;
        this.executionVersion = executionVersion;
    }

    public String getSmId() {
        return smId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setSmId(String smId) {
        this.smId = smId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
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
        if (!(o instanceof SmIdAndTaskIdWithExecutionVersion)) return false;

        SmIdAndTaskIdWithExecutionVersion that = (SmIdAndTaskIdWithExecutionVersion) o;

        if (!getSmId().equals(that.getSmId())) return false;
        if(!(getExecutionVersion().equals(that.getExecutionVersion()))) return false;
        return getTaskId().equals(that.getTaskId());

    }

    @Override
    public int hashCode() {
        int result = getSmId().hashCode();
        result = 31 * result + getTaskId().hashCode();
        result = 31 * result + getExecutionVersion().hashCode();
        return result;
    }

}
