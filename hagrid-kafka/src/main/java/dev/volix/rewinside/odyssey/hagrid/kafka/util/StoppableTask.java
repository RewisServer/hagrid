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
        this.onStop();
    }

    public void stop() {
        this.stopped = true;
    }

    public void onStop() {
        // defaults to do nothing, but you can listen to it to do
        // some cleanup.
    }

    public boolean isRunning() {
        return !this.stopped;
    }

}
