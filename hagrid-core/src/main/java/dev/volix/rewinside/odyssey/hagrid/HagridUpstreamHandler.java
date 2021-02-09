package dev.volix.rewinside.odyssey.hagrid;

import com.google.protobuf.ByteString;
import dev.volix.rewinside.odyssey.hagrid.exception.HagridConnectionException;
import dev.volix.rewinside.odyssey.hagrid.exception.HagridStreamException;
import dev.volix.rewinside.odyssey.hagrid.listener.Direction;
import dev.volix.rewinside.odyssey.hagrid.protocol.Packet;
import dev.volix.rewinside.odyssey.hagrid.protocol.Status;
import dev.volix.rewinside.odyssey.hagrid.topic.HagridTopic;
import dev.volix.rewinside.odyssey.hagrid.util.DaemonThreadFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Tobias Büser
 */
public class HagridUpstreamHandler implements UpstreamHandler {

    private final HagridService service;
    private HagridPublisher publisher;

    private final Map<String, HagridPacket<?>> idlePackets = new HashMap<>();
    private final Map<String, Long> idlePacketsTimestamps = new HashMap<>();

    public HagridUpstreamHandler(final HagridService service, final HagridPublisher publisher) {
        this.service = service;
        this.publisher = publisher;

        final ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(1, new DaemonThreadFactory());

        final int cleanupDelayInSeconds = service.getConfiguration().getInt(HagridConfig.LISTENER_CLEANUP_DELAY_IN_SECONDS);
        threadPool.scheduleAtFixedRate(new IdlingPacketCleanupTask(this.idlePackets, this.idlePacketsTimestamps),
            cleanupDelayInSeconds, cleanupDelayInSeconds, TimeUnit.SECONDS);
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
        if (this.publisher != null) {
            this.publisher.close();
            this.publisher = null;
        }
    }

    @Override
    public <T> void send(final String topic, final String key, final HagridPacket<T> packet) throws HagridStreamException {
        if (this.publisher == null) {
            throw new IllegalStateException("connect() has to be called before sending packets!");
        }

        final HagridTopic<T> registeredTopic = this.service.communication().getTopic(topic);
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

            this.service.getLogger().trace("Sent packet: {}", packet.toString().replaceAll("\n", ""));
            this.idlePackets.put(packet.getId(), packet);
            this.idlePacketsTimestamps.put(packet.getId(), System.currentTimeMillis());
        } catch (final Exception ex) {
            this.service.connection().handleError(ex);
            throw new HagridStreamException(ex);
        }

        // notify listeners
        this.service.communication().executeListeners(topic, Direction.UPSTREAM, packet);
    }

    @Override
    public boolean isIdling(final String packetId) {
        return this.idlePackets.containsKey(packetId);
    }

    private class IdlingPacketCleanupTask implements Runnable {

        private final Map<String, HagridPacket<?>> idlePackets;
        private final Map<String, Long> idlePacketsTimestamps;

        public IdlingPacketCleanupTask(final Map<String, HagridPacket<?>> idlePackets, final Map<String, Long> idlePacketsTimestamps) {
            this.idlePackets = idlePackets;
            this.idlePacketsTimestamps = idlePacketsTimestamps;
        }

        @Override
        public void run() {
            final long current = System.currentTimeMillis();
            final long cleanupDelay = HagridUpstreamHandler.this.service.getConfiguration().getInt(HagridConfig.LISTENER_CLEANUP_DELAY_IN_SECONDS);

            for (final String packetKey : new ArrayList<>(this.idlePacketsTimestamps.keySet())) {
                final long sentAt = this.idlePacketsTimestamps.get(packetKey);
                if (sentAt <= 0) continue;

                final long retentionAt = sentAt + (cleanupDelay * 1000);
                if (current >= retentionAt) {
                    this.idlePacketsTimestamps.remove(packetKey);
                    this.idlePackets.remove(packetKey);
                }
            }
        }

    }

}
