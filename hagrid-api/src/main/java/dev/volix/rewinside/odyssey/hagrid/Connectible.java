package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.exception.HagridConnectionException;

/**
 * Defines an instance that contains some kind of connection
 * and is able to change its state via methods.
 *
 * @author Tobias Büser
 */
public interface Connectible {

    /**
     * Connects to an external service.
     *
     * @throws HagridConnectionException if the connection fails
     */
    void connect() throws HagridConnectionException;

    /**
     * Disconnects from the external service
     */
    void disconnect();

    /**
     * Reconnects the service. Default is calling the disconnect
     * and connect method in succession.
     *
     * @throws HagridConnectionException if the connection fails after disconnecting
     */
    default void reconnect() throws HagridConnectionException {
        this.disconnect();
        this.connect();
    }

}
