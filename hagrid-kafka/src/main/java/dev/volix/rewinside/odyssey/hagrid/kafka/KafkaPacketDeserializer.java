package dev.volix.rewinside.odyssey.hagrid.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import dev.volix.rewinside.odyssey.hagrid.protocol.Packet;
import org.apache.kafka.common.serialization.Deserializer;

/**
 * @author Tobias Büser
 */
public class KafkaPacketDeserializer implements Deserializer<Packet> {

    @Override
    public Packet deserialize(String topic, byte[] bytes) {
        try {
            return Packet.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return null;
    }

}
