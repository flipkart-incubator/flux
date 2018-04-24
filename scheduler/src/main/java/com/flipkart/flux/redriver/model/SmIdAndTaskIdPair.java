package com.flipkart.flux.redriver.model;

/**
 * Created by amitkumar.o on 31/07/17.
 */
public class SmIdAndTaskIdPair {
    private  String smId;
    private  Long taskId;

    public SmIdAndTaskIdPair(String smId, Long taskId) {
        this.smId = smId;
        this.taskId = taskId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SmIdAndTaskIdPair)) return false;

        SmIdAndTaskIdPair that = (SmIdAndTaskIdPair) o;

        if (!getSmId().equals(that.getSmId())) return false;
        return getTaskId().equals(that.getTaskId());

    }

    @Override
    public int hashCode() {
        int result = getSmId().hashCode();
        result = 31 * result + getTaskId().hashCode();
        return result;
    }
}
