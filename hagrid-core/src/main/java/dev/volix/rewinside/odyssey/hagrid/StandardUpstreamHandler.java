package dev.volix.rewinside.odyssey.hagrid;

import com.google.protobuf.ByteString;
import dev.volix.rewinside.odyssey.hagrid.exception.HagridConnectionException;
import dev.volix.rewinside.odyssey.hagrid.exception.HagridStreamException;
import dev.volix.rewinside.odyssey.hagrid.listener.Direction;
import dev.volix.rewinside.odyssey.hagrid.protocol.Packet;
import dev.volix.rewinside.odyssey.hagrid.protocol.Status;

/**
 * @author Tobias Büser
 */
public class StandardUpstreamHandler implements UpstreamHandler {

    private final HagridService service;
    private HagridPublisher publisher;

    public StandardUpstreamHandler(final HagridService service, final HagridPublisher publisher) {
        this.service = service;
        this.publisher = publisher;
    }

    @Override
    public void connect() throws HagridConnectionException {
        try {
            this.publisher.open();
        } catch (final Exception ex) {
            throw new HagridConnectionException(ex);
        }
    }

    @Override
    public void disconnect() {
        this.publisher.close();
        this.publisher = null;
    }

    @Override
    public <T> void send(final String topic, final String key, final HagridPacket<T> packet) throws HagridStreamException {
        if (this.publisher == null) {
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

        final Packet protoPacket = Packet.newBuilder()
            .setPayload(packetPayload)
            .setId(packet.getId())
            .setRequestId(packet.getRequestId())
            .setStatus(Status.newBuilder()
                .setCode(packet.getStatus().getCode())
                .setMessage(packet.getStatus().getMessage() == null ? "" : packet.getStatus().getMessage())
                .build())
            .build();

        try {
            this.publisher.push(topic, key, protoPacket);
        } catch (final Exception ex) {
            this.service.getConnectionHandler().handleError(ex);
            throw new HagridStreamException(ex);
        }

        // notify listeners
        this.service.executeListeners(topic, Direction.UPSTREAM, packet);
    }

}
