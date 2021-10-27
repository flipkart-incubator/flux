package com.flipkart.flux.client.intercept;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flipkart.flux.client.model.*;

public class SimpleWorkflowForReplayTest {

    public static final String STRING_EVENT_NAME = "Some String Event";
    public static final String INTEGER_EVENT_NAME = "Some Integer Event";

    /* A simple workflow that goes about creating tasks and making merry. Sometimes both these fight over whose the merrier */
    @SuppressWarnings("unused")
	@Workflow(version = 1)
    public void simpleDummyWorkflowWithReplayEvent(SimpleWorkflowForReplayTest.IntegerEvent someInteger) {
        final StringEvent newString = waitForReplayEvent(null, someInteger);
        final StringEvent anotherString = waitForReplayEvent((SimpleWorkflowForReplayTest.StringEvent) null);
        someTaskWithIntegerAndString(newString, someInteger);
    }

    @Task(version = 1, retries = 2, timeout = 2000l, replayable = true)
    public SimpleWorkflowForReplayTest.StringEvent waitForReplayEvent(@ReplayEvent("someReplayEvent") SimpleWorkflowForReplayTest.StringEvent someString, SimpleWorkflowForReplayTest.IntegerEvent integerEvent) {
        return new SimpleWorkflowForReplayTest.StringEvent(integerEvent.anInteger.toString() + someString);
    }

    @Task(version = 1, retries = 2, timeout = 2000l, replayable = true)
    public SimpleWorkflowForReplayTest.StringEvent waitForReplayEvent(@ReplayEvent("someReplayEvent") SimpleWorkflowForReplayTest.StringEvent someString) {
        return new SimpleWorkflowForReplayTest.StringEvent("randomBs" + someString);
    }

    @Task(version = 1, retries = 0, timeout = 1000l)
    public void someTaskWithIntegerAndString(SimpleWorkflowForReplayTest.StringEvent someString, SimpleWorkflowForReplayTest.IntegerEvent someInteger) {
        //blah
    }

    public static class StringEvent implements Event {
        @JsonProperty
        private String aString;

        StringEvent() {
        }

        public StringEvent(String aString) {
            this.aString = aString;
        }

        @Override
        public String name() {
            return STRING_EVENT_NAME;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SimpleWorkflowForReplayTest.StringEvent that = (SimpleWorkflowForReplayTest.StringEvent) o;

            return aString.equals(that.aString);

        }

        @Override
        public int hashCode() {
            return aString.hashCode();
        }

        @Override
        public String toString() {
            return "StringEvent{" +
                    "aString='" + aString + '\'' +
                    '}';
        }
    }

    public static class IntegerEvent implements Event {
        @JsonProperty
        private Integer anInteger;
        IntegerEvent(){

        }
        public IntegerEvent(Integer anInteger) {
            this.anInteger = anInteger;
        }

        @Override
        public String name() {
            return INTEGER_EVENT_NAME;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SimpleWorkflowForReplayTest.IntegerEvent that = (SimpleWorkflowForReplayTest.IntegerEvent) o;

            return anInteger.equals(that.anInteger);

        }

        @Override
        public int hashCode() {
            return anInteger.hashCode();
        }

        @Override
        public String toString() {
            return "IntegerEvent{" +
                    "anInteger=" + anInteger +
                    '}';
        }
    }
}