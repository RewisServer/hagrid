package dev.volix.rewinside.odyssey.hagrid.topic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The topic group is a wrapper for a sorted list of topics to
 * better determine which registered topic fits best for a
 * given topic name.
 * <p>
 * For example if I have a topic {@code volix-party} and I want to know
 * which of the registered topics (e.g. {@code volix-*}, {@code volix-party-*}, ..)
 * fits best, we can easily use {@link #getMostFitting(String)}.
 *
 * @author Tobias Büser
 */
public class HagridTopicGroup {

    /**
     * As every topic begins with a fixed set of alphanumerical characters
     * we can determine a prefix from that, so that we can map
     * this group to a key.
     * <p>
     * e.g.: For {@code volix-rewinside-odyssey} the prefix would be {@code volix}.
     */
    private final String prefix;

    private final Map<String, HagridTopic<?>> topics = new HashMap<>();
    private final List<HagridTopic<?>> sortedTopics = new ArrayList<>();

    public HagridTopicGroup(final HagridTopic<?> topic) {
        final String[] parts = topic.getPattern().split("-");
        if (parts.length == 0) throw new IllegalArgumentException("topic does not contain prefix");

        this.prefix = parts[0];
        this.add(topic);
    }

    /**
     * @param pattern The topic pattern.
     *
     * @return A registered topic only, if the given pattern is exactly like
     * the pattern the topic got registered with.
     */
    public HagridTopic<?> getTopicExactly(final String pattern) {
        return this.topics.get(pattern);
    }

    /**
     * @param topicPattern The topic pattern to check
     *
     * @return A registered topic that matches given pattern the most. This means
     * that we do not choose the most abstract, but the less abstract one which
     * still matches the topic.
     */
    public HagridTopic<?> getMostFitting(final String topicPattern) {
        if (this.topics.containsKey(topicPattern)) {
            return this.topics.get(topicPattern);
        }

        final HagridTopic<?> toCheckTopic = new HagridTopic<>(topicPattern, null);
        for (int i = 0; i < this.sortedTopics.size(); i++) {
            final HagridTopic<?> sortedTopic = this.sortedTopics.get(i);

            if (toCheckTopic.compareTo(sortedTopic) > 0) {
                // tocheck is greater and therefore more abstract
                // get the previous one, because we reached the leaf
                if (i == 0) return null;
                return this.sortedTopics.get(i - 1);
            } else if (i == this.sortedTopics.size() - 1) {
                // otherwise if we reached the end, we return as well
                return sortedTopic;
            }
        }
        return null;
    }

    /**
     * Adds a new topic to the map and sorts an internal list
     * with {@link HagridTopic#compareTo(HagridTopic)}.
     * If a topic like this already exists, it will get overriden.
     *
     * @param topic The topic to register
     */
    public void add(final HagridTopic<?> topic) {
        final boolean contains = this.topics.containsKey(topic.getPattern());
        this.topics.put(topic.getPattern(), topic);

        if (!contains) {
            this.sortedTopics.add(topic);
            this.sortedTopics.sort((o1, o2) -> o1.compareTo(o2) * -1);
        }
    }

    /**
     * Removes given topic and sorts the internal list if a topic
     * to unregister has been found.
     *
     * @param topicPattern The pattern of the topic to unregister.
     */
    public void remove(final String topicPattern) {
        final HagridTopic<?> topic = this.topics.remove(topicPattern);

        if (topic != null) {
            this.sortedTopics.remove(topic);
            this.sortedTopics.sort((o1, o2) -> o1.compareTo(o2) * -1);
        }
    }

    public void remove(final HagridTopic<?> topic) {
        this.remove(topic.getPattern());
    }

    public String getPrefix() {
        return this.prefix;
    }

    public List<HagridTopic<?>> getSortedTopics() {
        return this.sortedTopics;
    }

}
