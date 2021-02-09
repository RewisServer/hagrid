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

    /**
     * This flag determines if sent packets from this
     * instance to this topic will also be handled
     * as incoming packet by the topic.
     * <p>
     * So if this flag is {@code false} then the service
     * will not receive packets that have been sent by the
     * service itself.
     */
    private final boolean receiveSentPackets;

    /**
     * If {@code false} packets that are in the topic
     * before the service starts will be discarded and not handled.
     * <p>
     * This will work with a timestamp check on the received packets.
     */
    private final boolean receiveStalePackets;

    private TopicProperties(final boolean shouldRunInParallel, final boolean receiveSentPackets, final boolean receiveStalePackets) {
        this.shouldRunInParallel = shouldRunInParallel;
        this.receiveSentPackets = receiveSentPackets;
        this.receiveStalePackets = receiveStalePackets;
    }

    public static TopicProperties.Builder newBuilder() {
        return new Builder();
    }

    public boolean shouldRunInParallel() {
        return this.shouldRunInParallel;
    }

    public boolean receivesSentPackets() {
        return this.receiveSentPackets;
    }

    public boolean receivesStalePackets() {
        return this.receiveStalePackets;
    }

    public static class Builder {

        private boolean shouldRunInParallel = true;
        private boolean receiveSentPackets = false;
        private boolean receiveStalePackets = true;

        private Builder() {
        }

        public Builder shouldRunInParallel(final boolean flag) {
            this.shouldRunInParallel = flag;
            return this;
        }

        public Builder receiveSentPackets(final boolean flag) {
            this.receiveSentPackets = flag;
            return this;
        }

        public Builder receiveStalePackets(final boolean flag) {
            this.receiveStalePackets = flag;
            return this;
        }

        public TopicProperties build() {
            return new TopicProperties(this.shouldRunInParallel, this.receiveSentPackets, this.receiveStalePackets);
        }

    }

}
