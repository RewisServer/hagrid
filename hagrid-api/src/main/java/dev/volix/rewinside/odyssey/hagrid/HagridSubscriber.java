package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.protocol.Packet;
import java.util.List;

/**
 * @author Tobias Büser
 */
public interface HagridSubscriber {

    void open();

    void close();

    void subscribe(List<String> topics);

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
