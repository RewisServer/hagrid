package dev.volix.rewinside.odyssey.hagrid.topic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tobias Büser
 */
public class HagridTopicGroup {

    private final String prefix;

    private final Map<String, HagridTopic<?>> topics = new HashMap<>();
    private final List<HagridTopic<?>> sortedTopics = new ArrayList<>();

    public HagridTopicGroup(final HagridTopic<?> topic) {
        final String[] parts = topic.getPattern().split("-");
        if (parts.length == 0) throw new IllegalArgumentException("topic does not contain prefix");

        this.prefix = parts[0];
        this.add(topic);
    }

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

    public void add(final HagridTopic<?> topic) {
        final boolean contains = this.topics.containsKey(topic.getPattern());
        this.topics.put(topic.getPattern(), topic);

        if (!contains) {
            this.sortedTopics.add(topic);
            this.sortedTopics.sort((o1, o2) -> o1.compareTo(o2) * -1);
        }
    }

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
