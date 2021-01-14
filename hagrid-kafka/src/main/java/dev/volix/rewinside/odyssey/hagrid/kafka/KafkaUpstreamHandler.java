package dev.volix.rewinside.odyssey.hagrid.kafka;

import com.google.protobuf.ByteString;
import dev.volix.rewinside.odyssey.hagrid.HagridPacket;
import dev.volix.rewinside.odyssey.hagrid.HagridService;
import dev.volix.rewinside.odyssey.hagrid.HagridTopic;
import dev.volix.rewinside.odyssey.hagrid.UpstreamHandler;
import dev.volix.rewinside.odyssey.hagrid.exception.HagridExecutionException;
import dev.volix.rewinside.odyssey.hagrid.listener.Direction;
import dev.volix.rewinside.odyssey.hagrid.protocol.Packet;
import dev.volix.rewinside.odyssey.hagrid.protocol.Status;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

/**
 * @author Tobias Büser
 */
public class KafkaUpstreamHandler implements UpstreamHandler {

    private final HagridService service;

    private final Producer<String, Packet> producer;

    public KafkaUpstreamHandler(HagridService service, Properties properties) {
        this.service = service;

        this.producer = new KafkaProducer<>(properties);
    }

    @Override
    public <T> void send(String topic, String key, HagridPacket<T> packet) throws HagridExecutionException {
        HagridTopic<T> registeredTopic = this.service.getTopic(topic);
        if (registeredTopic == null) {
            throw new IllegalArgumentException("Given topic has to be registered first!");
        }

        T payload = packet.getPayload();
        Packet.Payload packetPayload = payload == null
            ? Packet.Payload.newBuilder().setValue(ByteString.copyFrom(new byte[] {})).build()
            : Packet.Payload.newBuilder()
            .setTypeUrl(payload.getClass().getTypeName())
            .setValue(ByteString.copyFrom(registeredTopic.getSerdes().serialize(payload)))
            .build();

        Future<RecordMetadata> future = producer.send(new ProducerRecord<>(topic, key,
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
        try {
            future.get();
            this.service.getConnectionHandler().handleSuccess();
        } catch (InterruptedException | ExecutionException e) {
            this.service.getConnectionHandler().handleError(e);
            throw new HagridExecutionException(e);
        }

        // notify listeners
        service.executeListeners(topic, Direction.UPSTREAM, packet);
    }

}
