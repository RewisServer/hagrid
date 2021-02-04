package dev.volix.rewinside.odyssey.hagrid.topic;

import dev.volix.rewinside.odyssey.hagrid.serdes.HagridSerdes;
import java.util.regex.Pattern;

/**
 * Generally speaking a topic is the main part of a pub/sub mechanism. It defines a channel
 * where packets can be sent into and <b>multiple</b> clients can listen to
 * these packets at the same time.
 * <p>
 * For Hagrid a topic describes a pattern of multiple subtopics that this topic
 * unifies and a {@link HagridSerdes} that handles the serialization and deserialization
 * of existing payloads inside the topics.
 * <p>
 * For the {@link #pattern} we use a regex {@link Pattern} that allows Hagrid to use
 * the mechanism explained above.
 * <p>
 * For example: a topic pattern {@code volix-rewinside-*} contains all topics
 * that matches this pattern, e.g. {@code volix-rewinside-odyssey} and
 * {@code volix-rewinside-worlds}.
 *
 * @author Tobias Büser
 */
public class HagridTopic<T> implements Comparable<HagridTopic<?>> {

    /**
     * The topic pattern used to match subtopics to a topic.
     * <p>
     * For example: {@code volix-rewinside-odyssey-party-*}
     * Important to note is that the aserisk '*' is not allowed
     * to be the prefix of the pattern. It has to be in the middle
     * or at the end.
     */
    private final String pattern;

    /**
     * The serdes handling the deserializing and serializing
     * of the packets in the topic.
     */
    private final HagridSerdes<T> serdes;

    /**
     * Some options regarding the topic.
     */
    private final TopicProperties properties;

    /**
     * The generated regex pattern, so that we don't have to
     * generate it on demand but cache it here.
     */
    private final Pattern regexPattern;

    public HagridTopic(final String pattern, final HagridSerdes<T> serdes, final TopicProperties properties) {
        this.pattern = pattern;
        this.serdes = serdes;
        this.properties = properties;

        this.regexPattern = getTopicAsRegex(this.pattern);
    }

    public HagridTopic(final String pattern, final HagridSerdes<T> serdes) {
        this(pattern, serdes, TopicProperties.create().build());
    }

    public Pattern getRegexPattern() {
        return this.regexPattern;
    }

    public String getPattern() {
        return this.pattern;
    }

    public HagridSerdes<T> getSerdes() {
        return this.serdes;
    }

    public TopicProperties getProperties() {
        return this.properties;
    }

    /**
     * Returns given topic pattern as a regex so that the aserisk operator
     * '*' is applied correctly.
     * Can then be matched against a string to check if another topic
     * is a subtopic of given one.
     *
     * @param topicPattern The pattern.
     *
     * @return The regex pattern.
     */
    public static Pattern getTopicAsRegex(final String topicPattern) {
        String regex = topicPattern.replaceAll("-\\*", "-\\\\w+");
        if (!regex.endsWith("*")) {
            regex += "(?:-\\w+)*";
        }
        return Pattern.compile(regex);
    }

    @Override
    public int compareTo(final HagridTopic<?> o) {
        if (this.pattern.equalsIgnoreCase(o.pattern)) return 0;
        // compares for which is a subtopic of which.

        final String t1 = this.pattern.replaceAll("\\*", "any");
        final String t2 = o.pattern.replaceAll("\\*", "any");

        if (this.regexPattern.matcher(t2).matches()) {
            // we suppose t1 > t2
            if (o.regexPattern.matcher(t1).matches()) {
                // t1 = t2
                return 0;
            }
            return 1;
        } else if (o.regexPattern.matcher(t1).matches()) {
            // t2 > t1
            return -1;
        }
        return 1;
    }

    @Override
    public String toString() {
        return this.pattern + "(" + (this.serdes == null ? "null" : this.serdes.getClass().getSimpleName()) + ")";
    }

}
