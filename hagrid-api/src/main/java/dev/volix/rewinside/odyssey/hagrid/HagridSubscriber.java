package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.protocol.Packet;
import dev.volix.rewinside.odyssey.hagrid.topic.HagridTopic;
import java.util.List;

/**
 * Represents the instance that pulls in packets from
 * the external service as a subscriber.
 *
 * @author Tobias Büser
 */
public interface HagridSubscriber {

    /**
     * Opens the subscriber to the connection.
     */
    void open();

    /**
     * Closes the subscriber.
     * <p>
     * No packet can be received anymore until {@link #open()} is
     * executed again.
     */
    void close();

    /**
     * Returns a list of topics that this instance subscribes on.
     *
     * @return The list, can be empty.
     */
    List<HagridTopic<?>> getTopics();

    /**
     * Removes given topic from the list
     *
     * @param topic The topic to unsubscribe from
     */
    void unsubscribe(HagridTopic<?> topic);

    default void unsubscribe(final List<HagridTopic<?>> topics) {
        for (final HagridTopic<?> topic : topics) {
            this.unsubscribe(topic);
        }
    }

    /**
     * Adds given topic to the subscription list.
     * If it already exists, nothing happens.
     *
     * @param topic The topic
     */
    void subscribe(HagridTopic<?> topic);

    default void subscribe(final List<HagridTopic<?>> topics) {
        for (final HagridTopic<?> topic : topics) {
            this.subscribe(topic);
        }
    }

    /**
     * This method is blocking as the data could be non-existing
     * at this point, so that this method will wait until something is there.
     * <p>
     * Otherwise this list can also be empty.
     *
     * @return A list of records of data.
     */
    List<Record> poll();

    class Record {

        private final String topic;
        private final Packet packet;

        /**
         * Timestamp not when the record got received, but more
         * when the record got added to the message broker.
         */
        private final long timestamp;

        public Record(final String topic, final Packet packet, final long timestamp) {
            this.topic = topic;
            this.packet = packet;
            this.timestamp = timestamp;
        }

        public String getTopic() {
            return this.topic;
        }

        public Packet getPacket() {
            return this.packet;
        }

        public long getTimestamp() {
            return this.timestamp;
        }
    }

}
