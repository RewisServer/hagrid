package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.lib.grape.Service;
import dev.volix.rewinside.odyssey.hagrid.exception.HagridConnectionException;

/**
 * @author Tobias Büser
 */
public interface HagridService extends Service {

    default HagridWizard wizard() {
        return new HagridWizard(this);
    }

    ConnectionHandler connection();

    default void connect() throws HagridConnectionException {
        this.connection().connect();
    }

    default void disconnect() {
        this.connection().disconnect();
    }

    UpstreamHandler upstream();

    DownstreamHandler downstream();

    CommunicationHandler communication();

    default boolean blowjob() {
        return true;
    }

}
