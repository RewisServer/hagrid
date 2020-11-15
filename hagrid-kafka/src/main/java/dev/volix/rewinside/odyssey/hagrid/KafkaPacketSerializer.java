package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.protocol.Packet;
import org.apache.kafka.common.serialization.Serializer;

/**
 * @author Tobias Büser
 */
public class KafkaPacketSerializer implements Serializer<Packet> {

    @Override
    public byte[] serialize(String topic, Packet packet) {
        return packet.toByteArray();
    }

}
