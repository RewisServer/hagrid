package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.protocol.Packet;
import dev.volix.rewinside.odyssey.hagrid.topic.HagridTopic;
import java.util.List;

/**
 * @author Tobias Büser
 */
public interface HagridSubscriber {

    void open();

    void close();

    List<HagridTopic<?>> getTopics();

    void unsubscribe(HagridTopic<?> topic);

    default void unsubscribe(final List<HagridTopic<?>> topics) {
        for (final HagridTopic<?> topic : topics) {
            this.unsubscribe(topic);
        }
    }

    void subscribe(HagridTopic<?> topic);

    default void subscribe(final List<HagridTopic<?>> topics) {
        for (final HagridTopic<?> topic : topics) {
            this.subscribe(topic);
        }
    }

    List<Record> poll();

    class Record {

        private final String topic;
        private final Packet packet;

        public Record(final String topic, final Packet packet) {
            this.topic = topic;
            this.packet = packet;
        }

        public String getTopic() {
            return this.topic;
        }

        public Packet getPacket() {
            return this.packet;
        }

    }

}
