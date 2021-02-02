package dev.volix.rewinside.odyssey.hagrid.topic;

import dev.volix.rewinside.odyssey.hagrid.serdes.HagridSerdes;
import java.util.regex.Pattern;

/**
 * @author Tobias Büser
 */
public class HagridTopic<T> implements Comparable<HagridTopic<?>> {

    private final String pattern;
    private final HagridSerdes<T> serdes;

    private final TopicProperties properties;

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
