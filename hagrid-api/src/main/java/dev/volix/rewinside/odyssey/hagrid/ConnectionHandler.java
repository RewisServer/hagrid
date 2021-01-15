package dev.volix.rewinside.odyssey.hagrid;

/**
 * @author Tobias Büser
 */
public interface ConnectionHandler {

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
