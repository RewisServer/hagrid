package dev.volix.rewinside.odyssey.hagrid;

import dev.volix.rewinside.odyssey.hagrid.listener.Direction;
import dev.volix.rewinside.odyssey.hagrid.listener.HagridListener;
import dev.volix.rewinside.odyssey.hagrid.listener.HagridListenerMethod;
import dev.volix.rewinside.odyssey.hagrid.protocol.StatusCode;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * @author Tobias Büser
 */
public class HagridUpstreamWizard {

    private final HagridService service;

    private final String id = UUID.randomUUID().toString();
    private String topic;
    private String key = "";
    private String requestId = "";
    private Status status = new Status(StatusCode.OK, "");
    private Object payload;

    private int timeoutInSeconds = HagridListener.DEFAULT_TIMEOUT_IN_SECONDS;

    public HagridUpstreamWizard(final HagridService service) {
        this.service = service;
    }

    public HagridUpstreamWizard topic(final String topic) {
        this.topic = topic;
        return this;
    }

    public HagridUpstreamWizard key(final String key) {
        this.key = key;
        return this;
    }

    public HagridUpstreamWizard respondsTo(final String requestId) {
        this.requestId = requestId;
        return this;
    }

    public <T> HagridUpstreamWizard respondsTo(final HagridPacket<T> packet) {
        this.topic(packet.getTopic());
        return this.respondsTo(packet.getId());
    }

    public HagridUpstreamWizard status(final StatusCode code, final String message) {
        this.status = new Status(code, message);
        return this;
    }

    public HagridUpstreamWizard status(final StatusCode code) {
        return this.status(code, "");
    }

    public <T> HagridUpstreamWizard payload(final T payload) {
        this.payload = payload;
        return this;
    }

    public HagridUpstreamWizard timeout(final int timeoutInSeconds) {
        this.timeoutInSeconds = timeoutInSeconds;
        return this;
    }

    public void send() {
        final HagridPacket<?> packet = new HagridPacket<>(this.topic, this.id, this.requestId, this.status, this.payload);

        this.service.upstream().send(this.topic, this.key, packet);
    }

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
        this.service.registerListener(listener);

        this.send();
        return future;
    }

    public <T> CompletableFuture<HagridPacket<T>> sendAndWait() {
        return this.sendAndWait((Class<T>) null);
    }

    public <T> void sendAndWait(final Class<T> payloadClass, final Consumer<HagridPacket<T>> consumer) {
        this.sendAndWait(payloadClass).thenAccept(consumer);
    }

    public void sendAndWait(final Consumer<HagridPacket<?>> consumer) {
        this.sendAndWait().thenAccept(consumer);
    }

}
