package dev.volix.rewinside.odyssey.hagrid.kafka.util;

import java.time.Duration;

/**
 * @author Tobias Büser
 */
public abstract class StoppableTask implements Runnable {

    private volatile boolean stopped = false;

    private Duration sleepMs = Duration.ZERO;

    public StoppableTask(Duration sleepMs) {
        this.sleepMs = sleepMs;
    }

    public StoppableTask() {
    }

    public abstract int execute();

    @Override
    public void run() {
        while(!stopped) {
            int returnCode = this.execute();
            if(returnCode != 0) break;

            if(sleepMs.isZero()) continue;
            try {
                Thread.sleep(sleepMs.toMillis());
            } catch (InterruptedException e) {
                // hopefully you execute it asynchronously though
                Thread.currentThread().interrupt();
            }
        }
    }

    public void stop() {
        this.stopped = true;
    }

}
