package dev.volix.rewinside.odyssey.hagrid.kafka;

import dev.volix.rewinside.odyssey.hagrid.ConnectionHandler;
import dev.volix.rewinside.odyssey.hagrid.HagridService;
import dev.volix.rewinside.odyssey.hagrid.exception.HagridConnectionException;
import dev.volix.rewinside.odyssey.hagrid.util.DaemonThreadFactory;
import dev.volix.rewinside.odyssey.hagrid.util.StoppableTask;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.kafka.common.errors.TimeoutException;

/**
 * @author Tobias Büser
 */
public class KafkaConnectionHandler implements ConnectionHandler {

    private final HagridService service;
    private Status status = Status.IDLE;

    private int retries = 0;
    private long lastSuccess = 0L;
    private long lastFailure = 0L;

    private final Lock statusLock = new ReentrantLock();

    private final ExecutorService threadPool = Executors.newSingleThreadExecutor(new DaemonThreadFactory());
    private ReconnectTask reconnectTask;

    public KafkaConnectionHandler(final HagridService service) {
        this.service = service;
    }

    @Override
    public boolean isActive() {
        this.statusLock.lock();
        boolean isActive;
        try {
            isActive = this.getStatus() == Status.ACTIVE;
        } finally {
            this.statusLock.unlock();
        }
        return isActive;
    }

    @Override
    public void handleError(final Throwable error) {
        if (error instanceof ExecutionException) {
            final Throwable cause = error.getCause();
            if (!(cause instanceof TimeoutException)) return;

            // fatal error
            this.lastFailure = System.currentTimeMillis();

            this.statusLock.lock();
            try {
                if (this.status == Status.INACTIVE) return;
                this.status = Status.INACTIVE;
            } finally {
                this.statusLock.unlock();
            }

            if (this.reconnectTask != null && this.reconnectTask.isRunning()) return;
            this.reconnectTask = new ReconnectTask(Duration.of(10, ChronoUnit.SECONDS));
            this.threadPool.execute(this.reconnectTask);
        }
    }

    @Override
    public void handleSuccess() {
        if (this.retries > 0) this.retries = 0;

        this.statusLock.lock();
        try {
            if (this.status != Status.ACTIVE) this.status = Status.ACTIVE;
        } finally {
            this.statusLock.unlock();
        }

        this.lastSuccess = System.currentTimeMillis();

        if (this.reconnectTask != null && this.reconnectTask.isRunning()) {
            this.reconnectTask.stop();
            this.reconnectTask = null;
        }
    }

    @Override
    public Status getStatus() {
        return this.status;
    }

    @Override
    public void setStatus(final Status status) {
        this.statusLock.lock();
        try {
            this.status = status;
        } finally {
            this.statusLock.unlock();
        }

        this.retries = 0;
    }

    @Override
    public long getLastSuccess() {
        return this.lastSuccess;
    }

    @Override
    public long getLastFailure() {
        return this.lastFailure;
    }

    private class ReconnectTask extends StoppableTask {

        public ReconnectTask(final Duration sleepMs) {
            super(sleepMs);
        }

        @Override
        public int execute() {
            if (KafkaConnectionHandler.this.status != Status.INACTIVE) return 0;

            try {
                KafkaConnectionHandler.this.retries++;
                KafkaConnectionHandler.this.service.reconnect();
            } catch (final HagridConnectionException e) {
                // still not able to connect ..
            }
            return 0;
        }

    }

}
