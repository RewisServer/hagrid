package dev.volix.rewinside.odyssey.hagrid;

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

/**
 * @author Tobias Büser
 */
public abstract class HagridConnectionHandler implements ConnectionHandler {

    private final HagridService service;

    private Status status = Status.IDLE;
    private int retries = 0;
    private long lastSuccess = 0L;
    private long lastFailure = 0L;

    private final Lock statusLock = new ReentrantLock();

    private final ExecutorService threadPool = Executors.newSingleThreadExecutor(new DaemonThreadFactory());
    private ReconnectTask reconnectTask;

    public HagridConnectionHandler(final HagridService service) {
        this.service = service;
    }

    @Override
    public void connect() throws HagridConnectionException {
        this.service.upstream().connect();
        this.service.downstream().connect();

        try {
            this.checkConnection();
            this.handleSuccess();
        } catch (final Exception e) {
            this.handleError(e);
            throw new HagridConnectionException(e);
        }
    }

    @Override
    public void disconnect() {
        this.service.upstream().disconnect();
        this.service.downstream().disconnect();

        this.setStatus(ConnectionHandler.Status.INACTIVE);
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
            if (!(cause instanceof RuntimeException)) return;

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
            final int delayInSeconds = this.service.getConfiguration().getInt(HagridConfig.RECONNECT_DELAY_IN_SECONDS);

            this.reconnectTask = new ReconnectTask(Duration.of(delayInSeconds, ChronoUnit.SECONDS));
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
            if (HagridConnectionHandler.this.status != Status.INACTIVE) return 0;

            try {
                HagridConnectionHandler.this.retries++;
                HagridConnectionHandler.this.reconnect();
            } catch (final HagridConnectionException e) {
                // still not able to connect ..
            }
            return 0;
        }

    }
}
