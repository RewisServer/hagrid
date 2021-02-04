package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.listener.Direction;
import dev.volix.rewinside.odyssey.hagrid.listener.HagridListener;
import dev.volix.rewinside.odyssey.hagrid.listener.HagridListenerMethod;
import dev.volix.rewinside.odyssey.hagrid.protocol.StatusCode;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Tobias Büser
 */
public class HagridPacketWizard implements PacketWizard {

    private final HagridService service;

    private final String id = UUID.randomUUID().toString();
    private String topic;
    private String key = "";
    private String requestId = "";
    private Status status = new Status(StatusCode.OK);
    private Object payload;

    private int timeoutInSeconds;

    public HagridPacketWizard(final HagridService service) {
        this.service = service;

        this.timeoutInSeconds = service.getConfiguration().getInt(HagridConfig.LISTENER_DEFAULT_TIMEOUT_IN_SECONDS);
    }

    @Override
    public PacketWizard topic(final String topic) {
        this.topic = topic;
        return this;
    }

    @Override
    public PacketWizard key(final String key) {
        this.key = key;
        return this;
    }

    @Override
    public PacketWizard respondsTo(final String requestId) {
        this.requestId = requestId;
        return this;
    }

    @Override
    public PacketWizard status(final StatusCode code, final String message) {
        this.status = new Status(code, message);
        return this;
    }

    @Override
    public <T> PacketWizard payload(final T payload) {
        this.payload = payload;
        return this;
    }

    @Override
    public PacketWizard timeout(final int timeoutInSeconds) {
        this.timeoutInSeconds = timeoutInSeconds;
        return this;
    }

    @Override
    public void send() {
        final HagridPacket<?> packet = new HagridPacket<>(this.topic, this.id, this.requestId, this.status, this.payload);

        this.service.upstream().send(this.topic, this.key, packet);
    }

    @Override
    public <T> CompletableFuture<HagridPacket<T>> sendAndWait(final Class<T> payloadClass) {
        final CompletableFuture<HagridPacket<T>> future = new CompletableFuture<>();
        final HagridListener listener = HagridListener.builder(
            new HagridListenerMethod() {
                @Override
                public <E> void listen(final E payload, final HagridPacket<E> req, final HagridResponse response) {
                    future.complete((HagridPacket<T>) req);
                }
            }).topic(this.topic)
            .direction(Direction.DOWNSTREAM)
            .payloadClass(payloadClass)
            .listensTo(this.id)
            .timeout(this.timeoutInSeconds)
            .build();
        this.service.communication().registerListener(listener);

        this.send();
        return future;
    }

}