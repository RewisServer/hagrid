package dev.volix.rewinside.odyssey.hagrid.topic;

import dev.volix.rewinside.odyssey.hagrid.serdes.HagridSerdes;

/**
 * @author Tobias Büser
 */
public interface HagridTopicRegistry {

    HagridTopicGroup getTopicGroup(String prefix);

    default boolean hasTopicGroup(String prefix) {
        return this.getTopicGroup(prefix) != null;
    }

    <T> HagridTopic<T> getTopic(String pattern);

    default <T> HagridTopic<T> getTopicOrNull(String pattern) {
        try {
            return this.getTopic(pattern);
        } catch (Exception ex) {
            return null;
        }
    }

    default boolean hasTopic(final String pattern) {
        return this.getTopic(pattern) != null;
    }

    void registerTopic(String pattern, HagridSerdes<?> serdes);

    void unregisterTopic(String pattern);
    
}
