package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.lib.grape.Service;

/**
 * @author Tobias Büser
 */
public interface HagridService extends Service {

    ConnectionHandler connection();

    default HagridUpstreamWizard wizard() {
        return new HagridUpstreamWizard(this);
    }

    UpstreamHandler upstream();

    DownstreamHandler downstream();

    CommunicationHandler communication();

    default boolean blowjob() {
        return true;
    }

}
