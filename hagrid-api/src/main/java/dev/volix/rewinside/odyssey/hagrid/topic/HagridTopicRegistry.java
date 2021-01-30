package dev.volix.rewinside.odyssey.hagrid.topic;

import dev.volix.rewinside.odyssey.hagrid.serdes.HagridSerdes;

/**
 * @author Tobias Büser
 */
public interface HagridTopicRegistry {

    boolean hasTopic(String topic);

    <T> boolean hasTopic(Class<T> payloadClass);

    <T> HagridTopic<T> getTopic(String topic);

    <T> HagridTopic<T> getTopic(Class<T> payloadClass);

    <T> void registerTopic(String topic, HagridSerdes<T> serdes);

    void unregisterTopic(String topic);

    <T> void unregisterTopic(Class<T> payloadClass);

}
