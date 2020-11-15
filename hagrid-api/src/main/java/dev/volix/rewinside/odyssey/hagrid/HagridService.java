package dev.volix.rewinside.odyssey.hagrid;

/**
 * @author Tobias Büser
 */
public interface HagridService extends HagridTopicRegistry {

    void initialize();

    UpstreamHandler upstream();

    DownstreamHandler downstream();

    default boolean blowjob() {
        return true;
    }

}
