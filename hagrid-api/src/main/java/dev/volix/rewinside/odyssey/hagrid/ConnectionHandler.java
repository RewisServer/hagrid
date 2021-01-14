package dev.volix.rewinside.odyssey.hagrid;

/**
 * @author Tobias Büser
 */
public interface ConnectionHandler {

    Status getStatus();

    default boolean isActive() {
        return this.getStatus() == Status.ACTIVE;
    }

    void handleError(Throwable error);

    void handleSuccess();

    long getLastSuccess();

    long getLastFailure();

    enum Status {

        IDLE,
        ACTIVE,
        INACTIVE,

    }

}
