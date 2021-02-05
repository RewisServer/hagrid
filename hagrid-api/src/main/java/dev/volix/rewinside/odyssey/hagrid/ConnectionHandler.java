package dev.volix.rewinside.odyssey.hagrid;

/**
 * The connection handler is for maintaining and changing
 * the actual connection state.
 *
 * @author Tobias Büser
 */
public interface ConnectionHandler extends Connectible {

    /**
     * Checks if the current connection is active.
     *
     * @throws Exception if it is not
     */
    void checkConnection() throws Exception;

    default boolean isActive() {
        return this.getStatus() == Status.ACTIVE;
    }

    /**
     * Handles an error, which could or is affecting the connection state.
     *
     * @param error the error that occured
     */
    void handleError(Throwable error);

    /**
     * Handles success such that the connection is definitely
     * secure at this point.
     */
    void handleSuccess();

    Status getStatus();

    void setStatus(Status status);

    long getLastSuccess();

    long getLastFailure();

    /**
     * Represents the current status of the connection.
     */
    enum Status {

        /**
         * The client is not initialized or not connected
         * for the first time.
         */
        IDLE,

        /**
         * The connection is up and running.
         */
        ACTIVE,

        /**
         * The connection got disconnected by an error
         * or by manually disconnecting.
         */
        INACTIVE,

    }

}
