package dev.volix.rewinside.odyssey.hagrid;

import com.google.protobuf.ByteString;
import dev.volix.rewinside.odyssey.hagrid.listener.Direction;
import dev.volix.rewinside.odyssey.hagrid.listener.HagridContext;
import dev.volix.rewinside.odyssey.hagrid.protocol.Packet;
import dev.volix.rewinside.odyssey.hagrid.protocol.Status;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

/**
 * @author Tobias Büser
 */
public class KafkaUpstreamHandler extends UglyHagridListenerRegistry implements UpstreamHandler {

    private final HagridService service;

    private final Producer<String, Packet> producer;

    public KafkaUpstreamHandler(HagridService service, Properties properties) {
        this.service = service;

        this.producer = new KafkaProducer<>(properties);
    }

    @Override
    public <T> void send(String topic, String key, HagridPacket<T> packet) {
        HagridTopic<T> registeredTopic = this.service.getTopic(topic);
        if (registeredTopic == null) {
            throw new IllegalArgumentException("Given topic has to be registered first!");
        }

        T payload = packet.getPayload();
        Packet.Payload packetPayload = Packet.Payload.newBuilder()
            .setTypeUrl(payload.getClass().getTypeName())
            .setValue(ByteString.copyFrom(registeredTopic.getSerdes().serialize(payload)))
            .build();

        producer.send(new ProducerRecord<>(topic, key,
            Packet.newBuilder()
                .setPayload(packetPayload)
                .setId(packet.getId())
                .setRequestId(packet.getRequestId())
                .setStatus(Status.newBuilder()
                    .setCode(packet.getStatus().getCode())
                    .setMessage(packet.getStatus().getMessage())
                    .build())
                .build())
        );
        producer.close();

        // notify listeners
        super.executeListeners(topic, Direction.UPSTREAM, new HagridContext(packet, topic), payload);
    }

}