package dev.volix.rewinside.odyssey.hagrid.kafka;

import dev.volix.rewinside.odyssey.hagrid.ConnectionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @author Tobias Büser
 */
public class KafkaConnectionHandler implements ConnectionHandler {

    private Status status = Status.IDLE;

    private int retries = 0;
    private long lastSuccess = 0L;
    private long lastFailure = 0L;

    @Override
    public void handleError(Throwable error) {
        if (error instanceof ExecutionException) {
            Throwable cause = error.getCause();
            if(!(cause instanceof TimeoutException)) return;

            // fatal error
            this.lastFailure = System.currentTimeMillis();

            if (status != Status.INACTIVE) this.status = Status.INACTIVE;
        }
    }

    @Override
    public void handleSuccess() {
        if (retries > 0) this.retries = 0;
        if (status != Status.ACTIVE) this.status = Status.ACTIVE;

        this.lastSuccess = System.currentTimeMillis();
    }

    @Override
    public Status getStatus() {
        return this.status;
    }

    @Override
    public long getLastSuccess() {
        return lastSuccess;
    }

    @Override
    public long getLastFailure() {
        return lastFailure;
    }

}
