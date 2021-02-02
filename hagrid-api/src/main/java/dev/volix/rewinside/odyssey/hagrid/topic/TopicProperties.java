package dev.volix.rewinside.odyssey.hagrid.topic;

/**
 * @author Tobias Büser
 */
public class TopicProperties {

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
