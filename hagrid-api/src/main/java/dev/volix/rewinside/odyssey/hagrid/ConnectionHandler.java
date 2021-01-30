package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.exception.HagridConnectionException;

/**
 * @author Tobias Büser
 */
public interface ConnectionHandler extends Connectible {

    @Override
    default void reconnect() throws HagridConnectionException {
        this.disconnect();
        this.connect();
    }

    default boolean isActive() {
        return this.getStatus() == Status.ACTIVE;
    }

    void handleError(Throwable error);

    void handleSuccess();

    Status getStatus();

    void setStatus(Status status);

    long getLastSuccess();

    long getLastFailure();

    enum Status {

        IDLE,
        ACTIVE,
        INACTIVE,

    }

}
