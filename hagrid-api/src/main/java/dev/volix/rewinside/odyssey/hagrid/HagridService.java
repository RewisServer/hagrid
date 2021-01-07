package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.lib.grape.Service;
import dev.volix.rewinside.odyssey.hagrid.listener.HagridListenerRegistry;

/**
 * @author Tobias Büser
 */
public interface HagridService extends HagridTopicRegistry, HagridListenerRegistry, Service {

    void initialize();

    default HagridUpstreamWizard wizard() {
        return new HagridUpstreamWizard(upstream(), downstream());
    }

    UpstreamHandler upstream();

    DownstreamHandler downstream();

    default boolean blowjob() {
        return true;
    }

}
