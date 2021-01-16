package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.lib.grape.Service;
import dev.volix.rewinside.odyssey.hagrid.exception.HagridConnectionException;
import dev.volix.rewinside.odyssey.hagrid.listener.HagridListenerRegistry;

/**
 * @author Tobias Büser
 */
public interface HagridService extends HagridTopicRegistry, HagridListenerRegistry, Service, Connectible {

    @Override
    default void reconnect() throws HagridConnectionException {
        this.disconnect();
        this.connect();
    }

    ConnectionHandler getConnectionHandler();

    default boolean isConnected() {
        return this.getConnectionHandler().isActive();
    }

    default HagridUpstreamWizard wizard() {
        return new HagridUpstreamWizard(this);
    }

    UpstreamHandler upstream();

    DownstreamHandler downstream();

    default boolean blowjob() {
        return true;
    }

}
