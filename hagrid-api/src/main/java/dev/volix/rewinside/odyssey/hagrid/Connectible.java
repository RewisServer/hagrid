package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.exception.HagridConnectionException;

/**
 * @author Tobias Büser
 */
public interface Connectible {

    void connect() throws HagridConnectionException;

    void disconnect();

    default void reconnect() throws HagridConnectionException {
        this.disconnect();
        this.connect();
    }

}
