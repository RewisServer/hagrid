package dev.volix.rewinside.odyssey.hagrid.kafka.util;

import java.time.Duration;

/**
 * @author Tobias Büser
 */
public abstract class StoppableTask implements Runnable {

    private volatile boolean stopped = false;

    private Duration sleepMs = Duration.ZERO;

    public StoppableTask(final Duration sleepMs) {
        this.sleepMs = sleepMs;
    }

    public StoppableTask() {
    }

    public abstract int execute();

    @Override
    public void run() {
        while (!this.stopped) {
            final int returnCode = this.execute();
            if (returnCode != 0) break;

            if (this.sleepMs.isZero()) continue;
            try {
                Thread.sleep(this.sleepMs.toMillis());
            } catch (final InterruptedException e) {
                // hopefully you execute it asynchronously though
                Thread.currentThread().interrupt();
            }
        }
    }

    public void stop() {
        this.stopped = true;
    }

    public boolean isRunning() {
        return !this.stopped;
    }

}
