package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.lib.grape.Service;
import dev.volix.rewinside.odyssey.hagrid.config.PropertiesConfig;
import dev.volix.rewinside.odyssey.hagrid.exception.HagridConnectionException;
import java.util.logging.Logger;

/**
 * @author Tobias Büser
 */
public interface HagridService extends Service {
    
    Logger getLogger();

    PropertiesConfig getConfiguration();

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
