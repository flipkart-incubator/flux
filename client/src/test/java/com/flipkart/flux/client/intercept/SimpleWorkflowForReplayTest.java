package com.flipkart.flux.client.intercept;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flipkart.flux.client.model.*;

/**
 * Workflow used test e2e interception using replayEvent
 */
public class SimpleWorkflowForReplayTest {

    public static final String STRING_REPLAY_EVENT_NAME = "Some String Event";
    public static final String INTEGER_REPLAY_EVENT_NAME = "Some Integer Event";

    /* A simple workflow that goes about creating tasks and making merry. */
    @SuppressWarnings("unused")
	@Workflow(version = 1)
    public void simpleDummyWorkflowWithReplayEvent(IntegerEvent someInteger) {
        final StringEvent newString = waitForReplayEvent(null, someInteger);
        final StringEvent anotherString = waitForReplayEvent((StringEvent) null);
        final IntegerEvent anotherInteger = simpleAdditionTask(someInteger);
        final StringEvent someString = simpleStringModifyingTask(anotherString);
        someTaskWithIntegerAndString(newString, someInteger);
        someTaskWithIntegerAndString(someString, anotherInteger);
        waitForReplayEvent((StringEvent) null);
    }

    @Task(version = 1, retries = 2, timeout = 2000l, isReplayable = true)
    public StringEvent waitForReplayEvent(@ReplayEvent("someReplayEvent") StringEvent someString, IntegerEvent integerEvent) {
        return new StringEvent(integerEvent.anInteger.toString() + someString);
    }

    @Task(version = 1, retries = 2, timeout = 2000l, isReplayable = true)
    public StringEvent waitForReplayEvent(@ReplayEvent("someReplayEvent") StringEvent someString) {
        return new StringEvent("randomBs" + someString);
    }

    @Task(version = 1, retries = 0, timeout = 1000l)
    public void someTaskWithIntegerAndString(StringEvent someString, IntegerEvent someInteger) {
        //blah
    }

    @Task(version = 1, retries = 2, timeout = 3000l)
    public IntegerEvent simpleAdditionTask(IntegerEvent i) {
        return new IntegerEvent(i.anInteger + 2);
    }

    @Task(version = 1, retries = 2, timeout = 2000l)
    public StringEvent simpleStringModifyingTask(StringEvent someString) {
        return new StringEvent("randomBs" + someString);
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
            return STRING_REPLAY_EVENT_NAME;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            StringEvent that = (StringEvent) o;

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
            return INTEGER_REPLAY_EVENT_NAME;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            IntegerEvent that = (IntegerEvent) o;

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