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

import com.flipkart.flux.redriver.model.ScheduledMessage;
import com.flipkart.flux.redriver.model.ScheduledMessageComparator;
import com.flipkart.flux.redriver.service.MessageManagerService;
import com.flipkart.flux.task.redriver.RedriverRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * The scheduler uses an in memory priority queue to prioritise messages by their scheduled time
 * The message with the least scheduled time (i.e, the next message to be picked up) is picked and sent if the current time is
 * greater than or equal to scheduledTime. If not, the scheduler thread sleeps for the difference.
 */
@Singleton
public class MessageScheduler {

    private final PriorityQueue<ScheduledMessage> messages;
    private final MessageManagerService messageManagerService;
    private static final Logger logger = LoggerFactory.getLogger(MessageScheduler.class);
    private final RedriverRegistry redriverRegistry;
    private SchedulerThread schedulerThread;
    private ExecutorService persistenceExecutorService;
    private int noOfPersistenceWorkers;

    @Inject
    public MessageScheduler(MessageManagerService messageManagerService, RedriverRegistry redriverRegistry, @Named("redriver.noOfPersistenceWorkers") int noOfPersistenceWorkers) {
        this(messageManagerService, new PriorityQueue<>(new ScheduledMessageComparator()), redriverRegistry, noOfPersistenceWorkers);
    }

    MessageScheduler(MessageManagerService messageManagerService, PriorityQueue<ScheduledMessage> scheduledMessages, RedriverRegistry redriverRegistry, int noOfPersistenceWorkers) {
        this.messageManagerService = messageManagerService;
        this.messages = scheduledMessages;
        schedulerThread = new SchedulerThread();
        this.redriverRegistry = redriverRegistry;
        this.noOfPersistenceWorkers = noOfPersistenceWorkers;
        persistenceExecutorService = Executors.newFixedThreadPool(noOfPersistenceWorkers);
    }

    public void addMessage(ScheduledMessage scheduledMessage) {
        persistenceExecutorService.execute(new PersistenceWorker(scheduledMessage));
        this.messages.add(scheduledMessage);
        this.schedulerThread.resumeJobExecution();
    }

    public void removeMessage(Long taskId) {
        try {
        /* It is important that we delete from priority queue first. Its okay even if we the schedule for removal call fails.
            At most we will send a message that was not supposed to be sent but the receiver should be able to handle
            such cases by consulting its own DB.
        */
            this.messages.removeIf((m) -> m.getTaskId().equals(taskId));
        } catch (ConcurrentModificationException ex) {
            logger.warn("Removing from priority queue failed due to concurrent modification exception. Ignoring it!");
        }
        this.messageManagerService.scheduleForRemoval(taskId);
    }

    public void start() {
        if (schedulerThread.getState() == Thread.State.NEW) {
            synchronized (this) {
                if(schedulerThread.getState() == Thread.State.NEW) {
                    new RetrieveThread().start();
                    schedulerThread.start();
                }
            }
        } else if (schedulerThread.getState() == Thread.State.TERMINATED) {
            synchronized (this) {
                if (schedulerThread.getState() == Thread.State.TERMINATED) {
                    logger.info("Scheduler thread is in Terminated state. Starting a new Scheduler thread.");
                    persistenceExecutorService = Executors.newFixedThreadPool(noOfPersistenceWorkers);
                    schedulerThread = new SchedulerThread();
                    new RetrieveThread().start();
                    schedulerThread.start();
                }
            }
        } else {
            logger.warn("Scheduler thread start request discarded. Scheduler thread's current state: {}", schedulerThread.getState());
        }
    }

    public void stop() {
        schedulerThread.halt();
        persistenceExecutorService.shutdown();
        try {
            persistenceExecutorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("Error occurred while terminating Redriver's persistence executor service. Error: " + e.getMessage());
        }
    }

    /**
     * Retrieves all messages from ScheduledMessages table and adds them to priority queue
     */
    private class RetrieveThread extends Thread {
        @Override
        public void run() {
            List<ScheduledMessage> scheduledMessagesInDB = messageManagerService.retrieveAll();
            messages.addAll(scheduledMessagesInDB);
        }
    }

    private class SchedulerThread extends Thread {
        private Boolean halted = false;
        private Boolean paused = true;

        private final Object lock = new Object();

        @Override
        public void run() {
            while (true) {
                ensureNotPaused();

                synchronized (lock) {
                    if (halted) {
                        return;
                    }
                }
                try {
                    _run();
                } catch (RuntimeException e) {
                    logger.error("Encountered exception during execution. Error: " + e.getMessage(), e);
                } catch (InterruptedException e) {
                    /* The thread is interrupted, best to bail now. */
                    logger.error("We were interrupted", e);
                }
            }
        }

        private void _run() throws InterruptedException {
            final ScheduledMessage highestPriorityMessage = messages.peek();

            if (highestPriorityMessage != null) {
                if (highestPriorityMessage.shouldRunNow()) {
                    /*
                        Now, it may so happen that the "messageToSend" is different from the earlier highestPriority message
                        This is okay since this message is perhaps added at a later instance and is of even higher priority
                        than the previous highestPriorityMessage and hence is eligible to run by transitivity
                     */
                    final ScheduledMessage messageToSend = messages.poll();
                    redriverRegistry.redriveTask(messageToSend.getTaskId());
                    messageManagerService.scheduleForRemoval(messageToSend.getTaskId());
                } else {
                    Long timeLeft = highestPriorityMessage.timeLeftToRun();
                    if (timeLeft > 0) {
                        logger.info("Next job run only at {}", new Date(highestPriorityMessage.getScheduledTime()));
                        sleep(timeLeft);
                    }
                }
            } else {
                pauseJobExecution();
            }
        }

        private void ensureNotPaused() {
            try {
                synchronized (lock) {
                    while (paused && !halted) {
                        logger.info("Paused waiting for resume");
                        lock.wait();
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        private void sleep(Long timeLeft) throws InterruptedException {
            synchronized (lock) {
                lock.wait(timeLeft);
            }
        }

        public void pauseJobExecution() {
            synchronized (lock) {
                logger.info("Pausing job execution");
                paused = true;
                lock.notifyAll();
            }
        }

        public void resumeJobExecution() {
            synchronized (lock) {
                logger.info("Resuming job execution");
                paused = false;
                lock.notifyAll();
            }
        }

        public void halt() {
            synchronized (lock) {
                logger.info("Halting job execution");
                halted = true;
                lock.notifyAll();
            }
        }

        public void notifyNewJob() {
            synchronized (lock) {
                lock.notifyAll();
            }
        }
    }

    /**
     * Worker thread used to persist {@link ScheduledMessage} in DB
     */
    private class PersistenceWorker implements Runnable {

        ScheduledMessage scheduledMessage;

        PersistenceWorker(ScheduledMessage scheduledMessage) {
            this.scheduledMessage = scheduledMessage;
        }

        @Override
        public void run() {
            messageManagerService.saveMessage(scheduledMessage);
        }
    }
}
