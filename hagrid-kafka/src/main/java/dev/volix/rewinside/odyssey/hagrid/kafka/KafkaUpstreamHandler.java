package dev.volix.rewinside.odyssey.hagrid.kafka;

import com.google.protobuf.ByteString;
import dev.volix.rewinside.odyssey.hagrid.HagridPacket;
import dev.volix.rewinside.odyssey.hagrid.HagridService;
import dev.volix.rewinside.odyssey.hagrid.HagridTopic;
import dev.volix.rewinside.odyssey.hagrid.UpstreamHandler;
import dev.volix.rewinside.odyssey.hagrid.exception.HagridStreamException;
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
    private final Properties properties;

    private Producer<String, Packet> producer;

    public KafkaUpstreamHandler(final HagridService service, final Properties properties) {
        this.service = service;
        this.properties = properties;
    }

    @Override
    public void connect() {
        this.producer = new KafkaProducer<>(this.properties);
    }

    @Override
    public void disconnect() {
        this.producer.close();
        this.producer = null;
    }

    @Override
    public <T> void send(final String topic, final String key, final HagridPacket<T> packet) throws HagridStreamException {
        if (this.producer == null) {
            throw new IllegalStateException("connect() has to be called before sending packets!");
        }

        final HagridTopic<T> registeredTopic = this.service.getTopic(topic);
        if (registeredTopic == null) {
            throw new IllegalArgumentException("Given topic has to be registered first!");
        }

        final T payload = packet.getPayloadOrNull();
        final Packet.Payload packetPayload = payload == null
            ? Packet.Payload.newBuilder().setValue(ByteString.copyFrom(new byte[] {})).build()
            : Packet.Payload.newBuilder()
            .setTypeUrl(payload.getClass().getTypeName())
            .setValue(ByteString.copyFrom(registeredTopic.getSerdes().serialize(payload)))
            .build();

        final Future<RecordMetadata> future = this.producer.send(new ProducerRecord<>(topic, key,
            Packet.newBuilder()
                .setPayload(packetPayload)
                .setId(packet.getId())
                .setRequestId(packet.getRequestId())
                .setStatus(Status.newBuilder()
                    .setCode(packet.getStatus().getCode())
                    .setMessage(packet.getStatus().getMessage() == null ? "" : packet.getStatus().getMessage())
                    .build())
                .build())
        );
        try {
            future.get();
            this.service.getConnectionHandler().handleSuccess();
        } catch (final InterruptedException | ExecutionException e) {
            this.service.getConnectionHandler().handleError(e);
            throw new HagridStreamException(e);
        }

        // notify listeners
        this.service.executeListeners(topic, Direction.UPSTREAM, packet);
    }

}
