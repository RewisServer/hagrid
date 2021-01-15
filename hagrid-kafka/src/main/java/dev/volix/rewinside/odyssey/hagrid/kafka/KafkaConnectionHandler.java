package dev.volix.rewinside.odyssey.hagrid.kafka;

import dev.volix.rewinside.odyssey.hagrid.ConnectionHandler;
import dev.volix.rewinside.odyssey.hagrid.HagridService;
import dev.volix.rewinside.odyssey.hagrid.exception.HagridConnectionException;
import dev.volix.rewinside.odyssey.hagrid.kafka.util.StoppableTask;
import dev.volix.rewinside.odyssey.hagrid.util.DaemonThreadFactory;
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

    public KafkaConnectionHandler(HagridService service) {
        this.service = service;
    }

    @Override
    public void handleError(Throwable error) {
        if (error instanceof ExecutionException) {
            Throwable cause = error.getCause();
            if (!(cause instanceof TimeoutException)) return;

            // fatal error
            this.lastFailure = System.currentTimeMillis();

            statusLock.lock();
            try {
                if (status == Status.INACTIVE) return;
                this.status = Status.INACTIVE;
            } finally {
                statusLock.unlock();
            }

            if (reconnectTask != null && reconnectTask.isRunning()) return;
            this.reconnectTask = new ReconnectTask(Duration.of(10, ChronoUnit.SECONDS));
            threadPool.execute(this.reconnectTask);
        }
    }

    @Override
    public void handleSuccess() {
        if (retries > 0) this.retries = 0;

        statusLock.lock();
        try {
            if (status != Status.ACTIVE) this.status = Status.ACTIVE;
        } finally {
            statusLock.unlock();
        }

        this.lastSuccess = System.currentTimeMillis();

        if (reconnectTask != null && reconnectTask.isRunning()) {
            reconnectTask.stop();
            this.reconnectTask = null;
        }
    }

    @Override
    public Status getStatus() {
        return this.status;
    }

    @Override
    public void setStatus(Status status) {
        statusLock.lock();
        try {
            this.status = status;
        } finally {
            statusLock.unlock();
        }

        this.retries = 0;
    }

    @Override
    public long getLastSuccess() {
        return lastSuccess;
    }

    @Override
    public long getLastFailure() {
        return lastFailure;
    }

    private class ReconnectTask extends StoppableTask {

        public ReconnectTask(Duration sleepMs) {
            super(sleepMs);
        }

        @Override
        public int execute() {
            if (status != Status.INACTIVE) return 0;

            try {
                retries++;
                service.reconnect();
            } catch (HagridConnectionException e) {
                // still not able to connect ..
            }
            return 0;
        }

    }

}
