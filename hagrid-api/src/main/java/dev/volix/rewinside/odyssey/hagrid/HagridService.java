package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.lib.grape.Service;

/**
 * @author Tobias Büser
 */
public interface HagridService extends Service {

    default HagridWizard wizard() {
        return new HagridWizard(this);
    }

    ConnectionHandler connection();

    UpstreamHandler upstream();

    DownstreamHandler downstream();

    CommunicationHandler communication();

    default boolean blowjob() {
        return true;
    }

}
