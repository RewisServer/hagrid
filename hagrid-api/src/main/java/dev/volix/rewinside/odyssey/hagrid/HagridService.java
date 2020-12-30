package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.lib.grape.Service;

/**
 * @author Tobias Büser
 */
public interface HagridService extends HagridTopicRegistry, Service {

    void initialize();

    default HagridDuplexWizard wizard() {
        return new HagridDuplexWizard(upstream(), downstream());
    }

    UpstreamHandler upstream();

    DownstreamHandler downstream();

    default boolean blowjob() {
        return true;
    }

}
