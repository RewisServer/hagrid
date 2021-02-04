package dev.volix.rewinside.odyssey.hagrid.topic;

import dev.volix.rewinside.odyssey.hagrid.serdes.HagridSerdes;

/**
 * Represents a registry for {@link HagridTopic}.
 *
 * @author Tobias Büser
 */
public interface HagridTopicRegistry {

    HagridTopicGroup getTopicGroup(String prefix);

    default boolean hasTopicGroup(final String prefix) {
        return this.getTopicGroup(prefix) != null;
    }

    /**
     * Gets a topic with given pattern.
     * If no topic could be found, an error will be thrown.
     *
     * @param pattern The pattern to search for
     *
     * @return The topic, will not be null
     */
    <T> HagridTopic<T> getTopic(String pattern);

    default <T> HagridTopic<T> getTopicOrNull(final String pattern) {
        try {
            return this.getTopic(pattern);
        } catch (final Exception ex) {
            return null;
        }
    }

    default boolean hasTopic(final String pattern) {
        return this.getTopic(pattern) != null;
    }

    /**
     * Registers a topic. If it already exists, it will replace the old one.
     *
     * @param pattern    The pattern of the topic
     * @param serdes     The serdes
     * @param properties Some other options regarding the topic
     *
     * @see HagridTopicGroup#add(HagridTopic)
     */
    void registerTopic(String pattern, HagridSerdes<?> serdes, TopicProperties properties);

    default void registerTopic(final String pattern, final HagridSerdes<?> serdes) {
        this.registerTopic(pattern, serdes, TopicProperties.create().build());
    }

    void unregisterTopic(String pattern);

}
