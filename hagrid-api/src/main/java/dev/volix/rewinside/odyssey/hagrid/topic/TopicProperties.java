package dev.volix.rewinside.odyssey.hagrid.topic;

/**
 * Some properties to closer define what happens when
 * registering and running the topic.
 *
 * @author Tobias Büser
 */
public class TopicProperties {

    /**
     * Will be used to determine if the topic should be
     * in a seperate thread, so that this topic
     * can be consumed in parallel.
     * <p>
     * It is incumbent upon the implementation to ensure that
     * or to throw an error.
     */
    private final boolean shouldRunInParallel;

    private TopicProperties(final boolean shouldRunInParallel) {
        this.shouldRunInParallel = shouldRunInParallel;
    }

    public static TopicProperties.Builder create() {
        return new Builder();
    }

    public boolean shouldRunInParallel() {
        return this.shouldRunInParallel;
    }

    public static class Builder {

        private boolean shouldRunInParallel = true;

        private Builder() {
        }

        public Builder shouldRunInParallel(final boolean flag) {
            this.shouldRunInParallel = flag;
            return this;
        }

        public TopicProperties build() {
            return new TopicProperties(this.shouldRunInParallel);
        }

    }

}
